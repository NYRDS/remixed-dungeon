#!/usr/bin/env python3
"""Remixed Dungeon: VK Play purchase entitlement service.

Stateless endpoints behind nginx (proxied at /rpd/vkplay/ on nyrds.net and
ru.nyrds.net):

  GET  /rpd/vkplay/ping    -> liveness probe for host selection
  GET  /rpd/vkplay/cb      -> VK ID OAuth callback: exchanges the auth code
                              (PKCE verifier rides inside the OAuth `state`),
                              checks VK Play ownership via the Steam-emulated
                              GetPublisherAppOwnership endpoint, and renders a
                              page handing a signed entitlement token to the
                              game (deep link + copyable fallback).
  GET/POST /rpd/vkplay/verify -> validates a token (HMAC + expiry) and
                              re-checks ownership (catches refunds).

The service keeps no state: the OAuth `state` parameter carries
base64url(nonce | pkce_verifier) through VK's redirect, and the entitlement
token is self-contained (vk_id + expiry + HMAC). Deploy the same file on both
hosts; secrets come from the environment (see rpd-vkplay.service).
"""

import base64
import hashlib
import hmac
import html
import json
import os
import secrets
import sys
import time
import urllib.parse
import urllib.request
from http.server import BaseHTTPRequestHandler, ThreadingHTTPServer

PORT = int(os.environ.get("RPD_VKPLAY_PORT", "8021"))
CLIENT_ID = os.environ.get("VKID_CLIENT_ID", "")
CLIENT_SECRET = os.environ.get("VKID_CLIENT_SECRET", "")
VKPLAY_GMRID = os.environ.get("VKPLAY_GMRID", "42406")
VKPLAY_API_SECRET = os.environ.get("VKPLAY_API_SECRET", "")
TOKEN_SECRET = os.environ.get("RPD_VKPLAY_TOKEN_SECRET", "")
TOKEN_TTL_DAYS = int(os.environ.get("RPD_VKPLAY_TOKEN_TTL_DAYS", "30"))
GAME_PLATFORM_APPID = 2038660  # VK Play platform app id of Remixed Pixel Dungeon

REDIRECT_PATH = "/rpd/vkplay/cb"
DEEP_LINK = "rpd://vkplay-linked"

# nonce -> (token, expiry); in-memory, single-use, short-lived. The game polls
# /rpd/vkplay/poll with its nonce right after completing the OAuth consent.
# Losing these on restart only means the player taps the button again.
PENDING_GRANTS = {}
PENDING_TTL = 900


def store_grant(nonce: bytes, token: str):
    PENDING_GRANTS[nonce] = (token, int(time.time()) + PENDING_TTL)


def take_grant(nonce: bytes):
    entry = PENDING_GRANTS.pop(nonce, None)
    if entry is None:
        return None
    token, expiry = entry
    if expiry < int(time.time()):
        return None
    return token

if not all([CLIENT_ID, CLIENT_SECRET, VKPLAY_API_SECRET, TOKEN_SECRET]):
    sys.stderr.write(
        "rpd-vkplay: missing VKID_CLIENT_ID / VKID_CLIENT_SECRET / "
        "VKPLAY_API_SECRET / RPD_VKPLAY_TOKEN_SECRET\n"
    )
    sys.exit(1)


def b64url(data: bytes) -> str:
    return base64.urlsafe_b64encode(data).rstrip(b"=").decode("ascii")


def b64url_decode(text: str) -> bytes:
    pad = "=" * (-len(text) % 4)
    return base64.urlsafe_b64decode(text + pad)


def http_get(url: str, timeout: float = 15.0):
    req = urllib.request.Request(url, headers={"User-Agent": "rpd-vkplay/1.0"})
    with urllib.request.urlopen(req, timeout=timeout) as resp:
        return json.loads(resp.read().decode("utf-8"))


def http_post_form(url: str, fields: dict, timeout: float = 15.0):
    body = urllib.parse.urlencode(fields).encode("utf-8")
    req = urllib.request.Request(
        url,
        data=body,
        headers={
            "Content-Type": "application/x-www-form-urlencoded",
            "User-Agent": "rpd-vkplay/1.0",
        },
    )
    with urllib.request.urlopen(req, timeout=timeout) as resp:
        return json.loads(resp.read().decode("utf-8"))


def ownership_key() -> str:
    digest = hashlib.sha256(VKPLAY_API_SECRET.encode("utf-8")).hexdigest()
    return f"{VKPLAY_GMRID}:{digest}"


def owns_game(vk_id: str) -> bool:
    url = (
        "https://api.vkplay.ru/steam/ISteamUser/GetPublisherAppOwnership/v3/"
        + "?key="
        + urllib.parse.quote(ownership_key())
        + "&steamid="
        + urllib.parse.quote(vk_id)
    )
    data = http_get(url)
    apps = (data.get("appownership") or {}).get("apps") or []
    return any(
        app.get("appid") == GAME_PLATFORM_APPID and app.get("ownsapp") for app in apps
    )


def mint_token(vk_id: str, exp: int) -> str:
    payload = f"v1|{vk_id}|{exp}"
    sig = b64url(hmac.new(TOKEN_SECRET.encode("utf-8"), payload.encode("utf-8"), hashlib.sha256).digest())
    return f"v1.{vk_id}.{exp}.{sig}"


def check_token(token: str):
    """Returns vk_id or None."""
    parts = token.split(".")
    if len(parts) != 4 or parts[0] != "v1":
        return None
    _, vk_id, exp_s, sig = parts
    if not vk_id.isdigit() or not exp_s.isdigit():
        return None
    exp = int(exp_s)
    if exp < int(time.time()):
        return None
    payload = f"v1|{vk_id}|{exp}"
    expect = b64url(
        hmac.new(TOKEN_SECRET.encode("utf-8"), payload.encode("utf-8"), hashlib.sha256).digest()
    )
    if not hmac.compare_digest(sig, expect):
        return None
    return vk_id


def exchange_code(code: str, verifier: str, redirect_uri: str):
    """Returns the VK ID user id from the token exchange response."""
    data = http_post_form(
        "https://id.vk.ru/oauth2/auth",
        {
            "grant_type": "authorization_code",
            "code": code,
            "client_id": CLIENT_ID,
            "client_secret": CLIENT_SECRET,
            "code_verifier": verifier,
            "redirect_uri": redirect_uri,
        },
    )
    sys.stderr.write(
        "cb: exchange keys=%s id_token_claims=%s\n"
        % (
            sorted(data.keys()),
            sorted(json.loads(b64url_decode(data["id_token"].split(".")[1])).keys())
            if data.get("id_token", "").count(".") == 2
            else "no id_token",
        )
    )
    if data.get("user_id"):
        return str(data["user_id"])
    id_token = data.get("id_token", "")
    if id_token.count(".") == 2:
        payload = json.loads(b64url_decode(id_token.split(".")[1]))
        sub = payload.get("sub")
        if sub:
            return str(sub)
    raise ValueError("no user id in VK ID token response")


PAGE = """<!DOCTYPE html>
<html lang="ru"><head><meta charset="utf-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<title>Remixed Dungeon</title>
<style>
body {{ font-family: sans-serif; background: #1d1d21; color: #ddd; text-align: center; padding: 2em; }}
a, button {{ color: #7ec850; }}
#token {{ width: 90%; max-width: 34em; background: #2a2a2e; color: #aaa; border: 1px solid #444;
          padding: .6em; font-size: .8em; word-break: break-all; }}
.hidden {{ display: none; }}
</style></head><body>
<h3>{title}</h3>
<p>{status}</p>
<div class="{token_hidden}">
  <p>{hint}</p>
  <p><a id="dl" href="{deeplink}">{open_game}</a></p>
  <p><textarea id="token" rows="3" readonly onclick="this.select()">{token}</textarea></p>
  <p><button onclick="var t=document.getElementById('token');t.select();document.execCommand('copy');this.textContent='{copied}'">{copy}</button></p>
</div>
</body></html>"""


def render_page(lang_ok: bool, title: str, status: str, token: str = ""):
    ru = lang_ok
    ctx = {
        "title": title,
        "status": status,
        "token_hidden": "hidden" if not token else "",
        "deeplink": html.escape(f"{DEEP_LINK}?token={token}") if token else "",
        "token": html.escape(token),
        "hint": (
            "Вернитесь в игру — окно должно заполниться само. Если игра не открылась, скопируйте код ниже и вставьте её в окне игры."
            if ru
            else "Return to the game — the window should fill in automatically. If the game did not open, copy the code below and paste it into the game window."
        ),
        "open_game": "Вернуться в игру" if ru else "Return to the game",
        "copy": "Скопировать" if ru else "Copy",
        "copied": "Скопировано!" if ru else "Copied!",
    }
    return PAGE.format(**ctx).encode("utf-8")


class Handler(BaseHTTPRequestHandler):
    server_version = "rpd-vkplay/1.0"

    def log_message(self, fmt, *args):
        sys.stderr.write("%s - %s\n" % (self.address_string(), fmt % args))

    def _reply(self, code: int, body: bytes, ctype: str = "text/html; charset=utf-8"):
        self.send_response(code)
        self.send_header("Content-Type", ctype)
        self.send_header("Cache-Control", "no-store, max-age=0")
        self.send_header("Content-Length", str(len(body)))
        self.end_headers()
        self.wfile.write(body)

    def _redirect(self, location: str):
        self.send_response(302)
        self.send_header("Location", location)
        self.send_header("Cache-Control", "no-store, max-age=0")
        self.send_header("Content-Length", "0")
        self.end_headers()

    def _json(self, code: int, obj):
        self._reply(code, json.dumps(obj).encode("utf-8"), "application/json")

    def do_GET(self):
        parsed = urllib.parse.urlparse(self.path)
        query = urllib.parse.parse_qs(parsed.query)
        path = parsed.path.rstrip("/") or parsed.path

        if path == "/rpd/vkplay/ping":
            self._json(200, {"ok": True, "host": self.headers.get("Host", "")})
            return

        if path == "/rpd/vkplay/cb":
            self._handle_cb(query)
            return

        if path == "/rpd/vkplay/poll":
            nonce = query.get("nonce", [""])[0]
            try:
                token = take_grant(b64url_decode(nonce)) if nonce else None
            except Exception:
                token = None
            if token:
                self._json(200, {"ok": True, "token": token})
            else:
                self._json(200, {"ok": False})
            return

        if path == "/rpd/vkplay/verify":
            self._handle_verify(query.get("token", [""])[0])
            return

        self._json(404, {"ok": False, "error": "not_found"})

    def do_POST(self):
        parsed = urllib.parse.urlparse(self.path)
        length = int(self.headers.get("Content-Length", "0") or 0)
        raw = self.rfile.read(min(length, 65536)).decode("utf-8", "replace") if length else ""
        token = ""

        if parsed.path.rstrip("/") == "/rpd/vkplay/verify":
            ctype = (self.headers.get("Content-Type") or "").lower()
            if "json" in ctype:
                try:
                    token = json.loads(raw).get("token", "")
                except ValueError:
                    token = ""
            else:
                token = urllib.parse.parse_qs(raw).get("token", [""])[0]
            self._handle_verify(token)
            return

        self._json(404, {"ok": False, "error": "not_found"})

    def _handle_cb(self, query):
        host = (self.headers.get("Host") or "").split(",")[0].strip()
        redirect_uri = f"https://{host}{REDIRECT_PATH}"
        lang_ok = "ru" in (self.headers.get("Accept-Language") or "").lower()

        code = query.get("code", [""])[0]
        state = query.get("state", [""])[0]

        def fail(msg_ru: str, msg_en: str):
            self._reply(
                200,
                render_page(lang_ok, "Remixed Dungeon", msg_ru + " / " + msg_en),
            )

        if not code or not state:
            fail("Не хватает данных авторизации.", "Missing authorization data.")
            return

        try:
            # state = b64url(nonce | pkce_verifier); both generated by the game.
            raw_state = b64url_decode(state)
            verifier = raw_state[12:].decode("ascii")
            if len(verifier) < 43:
                raise ValueError("bad verifier length")
        except Exception:
            fail("Повреждённый параметр состояния.", "Corrupted state parameter.")
            return

        try:
            vk_id = exchange_code(code, verifier, redirect_uri)
        except Exception as exc:
            sys.stderr.write("cb: exchange failed: %r\n" % (exc,))
            fail("Не удалось подтвердить вход через VK ID.", "VK ID sign-in failed.")
            return
        sys.stderr.write("cb: vk_id=%s\n" % vk_id)

        try:
            owned = owns_game(vk_id)
        except Exception as exc:
            sys.stderr.write("cb: ownership check failed: %r\n" % (exc,))
            fail("Сервис VK Play недоступен, попробуйте позже.", "VK Play service unreachable, try later.")
            return

        if not owned:
            fail(
                "Игра Remixed Pixel Dungeon не найдена в вашей библиотеке VK Play.",
                "Remixed Pixel Dungeon was not found in your VK Play library.",
            )
            return

        exp = int(time.time()) + TOKEN_TTL_DAYS * 86400
        token = mint_token(vk_id, exp)
        store_grant(raw_state[:12], token)
        self._reply(
            200,
            render_page(
                lang_ok,
                "Remixed Dungeon",
                "Готово! Реклама в мобильной версии отключена на 30 дней."
                if lang_ok
                else "Done! Mobile ads are disabled for 30 days.",
                token,
            ),
        )

    def _handle_verify(self, token: str):
        if not token:
            self._json(400, {"ok": False, "error": "missing_token"})
            return
        vk_id = check_token(token)
        if vk_id is None:
            self._json(403, {"ok": False, "error": "bad_token"})
            return
        try:
            owned = owns_game(vk_id)
        except Exception as exc:
            sys.stderr.write("verify: ownership check failed: %r\n" % (exc,))
            self._json(503, {"ok": False, "error": "vkplay_unreachable"})
            return
        if not owned:
            self._json(403, {"ok": False, "error": "not_owned"})
            return
        exp = int(token.split(".")[2])
        self._json(200, {"ok": True, "vk_id": vk_id, "exp": exp})


def main():
    server = ThreadingHTTPServer(("127.0.0.1", PORT), Handler)
    sys.stderr.write("rpd-vkplay: listening on 127.0.0.1:%d\n" % PORT)
    server.serve_forever()


if __name__ == "__main__":
    main()
