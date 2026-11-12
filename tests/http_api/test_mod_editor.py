#!/usr/bin/env python3
"""Built-in mod editor API test (bd remixed-dungeon-dyp).

Covers the /api/mods/* endpoints end-to-end: create-from-template, read,
save with validation, guards (traversal + main-mod write block), export
zip shape, select-and-restart, and that a mod created by the editor is
actually playable (start_game lands in the template Start level with
the ExampleMob present).

Run: RPD_HEADLESS_JAR=<jar> python3 test_mod_editor.py
"""

import argparse
import io
import os
import subprocess
import sys
import tempfile
import time
import zipfile
from typing import Optional

import requests

# trust_env=False: the dev box runs a corporate/local proxy (tinyproxy,
# ZCODE_HTTP_PROXY) that cannot reach 127.0.0.1 - env proxies must not apply
HTTP = requests.Session()
HTTP.trust_env = False

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from headless_server import server_command

MOD = "TestModEditorQA"


class Fail(Exception):
    pass


def check(cond, msg):
    if not cond:
        raise Fail(msg)
    print("  ok: " + msg)


def build_game_dir(jar, tmp):
    """Scratch rundir mirroring the CI layout (python-tests.yml): rundir/
    with ../assets, ../d_assets and ../l10ns resolving around it."""
    project_root = os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
    game_dir = os.path.join(tmp, "rundir")
    os.makedirs(game_dir, exist_ok=True)

    def link(target, name):
        dst = os.path.join(tmp, name)
        if not os.path.exists(dst):
            os.symlink(target, dst)

    link(os.path.join(project_root, "RemixedDungeon/src/main/assets"), "assets")
    link(os.path.join(project_root, "RemixedDungeonDesktop/src/desktop/d_assets"), "d_assets")

    l10ns = os.path.join(project_root, "RemixedDungeonDesktop/src/desktop/l10ns")
    if not os.path.isfile(os.path.join(l10ns, "strings_en.json")):
        subprocess.run(["python3", "make_json.py"],
                       cwd=os.path.join(project_root, "RemixedDungeonDesktop"), check=True)
    link(l10ns, "l10ns")
    return game_dir


def wait_ready(base, timeout=120):
    deadline = time.time() + timeout
    while time.time() < deadline:
        try:
            r = HTTP.get(base + "/ready", timeout=3)
            if r.ok and r.json().get("ready"):
                return True
        except Exception:
            pass
        time.sleep(2)
    return False


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--port", default="8091")
    parser.add_argument("--keep", action="store_true", help="keep scratch dir")
    args = parser.parse_args()

    jar = os.environ.get("RPD_HEADLESS_JAR")
    if not jar:
        # default to the fresh build
        root = os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
        jar = os.path.join(root, "RemixedDungeonHeadless/build/libs/RemixedDungeon-Headless.jar")
    if not os.path.isfile(jar):
        print("no headless jar at %s - build headlessShadowJar first" % jar)
        return 2

    tmp = tempfile.mkdtemp(prefix="rd_modeditor_test_")
    game_dir = build_game_dir(jar, tmp)
    print("game dir: " + game_dir)

    # -Duser.home: headless prefs persist to ~/.local/share/remixed-dungeon -
    # isolate them so the test never touches the developer's global prefs.
    # No --mod flag: it re-pins the active mod on every boot and would defeat
    # the select-and-restart flow under test (fresh prefs default to Remixed).
    home_dir = os.path.join(tmp, "home")
    os.makedirs(home_dir, exist_ok=True)
    cmd = ["java", "-Duser.home=" + home_dir, "-jar", os.path.abspath(jar),
           "--webserver=" + args.port, "--fps=5"]

    server = [None]

    def launch():
        server[0] = subprocess.Popen(cmd, cwd=game_dir,
                                     stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL)

    def stop():
        if server[0] and server[0].poll() is None:
            server[0].terminate()
            try:
                server[0].wait(timeout=10)
            except Exception:
                server[0].kill()

    # headless doRestart = System.exit(0): select-and-restart kills the
    # process on purpose, the runner (this test / a script) relaunches it
    def select_and_restart(name):
        r = HTTP.post(base + "/api/mods/select", json={"name": name}, timeout=10)
        check(r.ok, "select " + name)
        deadline = time.time() + 30
        while time.time() < deadline and server[0].poll() is None:
            time.sleep(1)
        check(server[0].poll() is not None, "headless restart exits the process")
        launch()
        check(wait_ready(base, timeout=180), "server back after restart")

    launch()
    base = "http://127.0.0.1:" + args.port
    try:
        check(wait_ready(base), "server ready")
        api = base + "/api/mods/"

        # -- create ------------------------------------------------------
        r = HTTP.post(api + "create", json={
            "name": MOD, "title": "QA Mod", "author": "QA",
            "description": "api test"}, timeout=10)
        check(r.ok and r.json().get("success"), "create from template")
        check(r.json().get("files") and "mobsDesc/ExampleMob.json" in r.json()["files"],
              "template mob copied")

        # duplicate name is rejected
        r = HTTP.post(api + "create", json={"name": MOD}, timeout=10)
        check(r.status_code == 409, "duplicate create -> 409")

        # -- files + read ------------------------------------------------
        r = HTTP.get(api + "files", params={"mod": MOD}, timeout=10)
        names = [e["name"] for e in r.json()["entries"]]
        check("version.json" in names and "mobsDesc" in names, "files listing")

        r = HTTP.get(api + "read", params={"mod": MOD, "file": "version.json"}, timeout=10)
        check(r.ok and r.json()["content"].find("QA Mod") > 0, "read version.json")

        # -- raw bytes (builtin fallback must not be a closed-stream 0-byte 200)
        r = HTTP.get(api + "raw", params={"mod": MOD, "file": "rat.png"}, timeout=10)
        check(r.ok and len(r.content) > 8 and r.content[:4] == b"\x89PNG",
              "raw builtin fallback returns png bytes (%d)" % len(r.content))
        r = HTTP.get(api + "raw", params={"mod": MOD, "file": "tilesets/tiles_sewers_xyz.png"},
                     timeout=10)
        check(r.ok and len(r.content) > 8 and r.content[:4] == b"\x89PNG",
              "raw builtin tileset returns png bytes (%d)" % len(r.content))
        r = HTTP.get(api + "raw", params={"mod": MOD, "file": "no_such_file.png"}, timeout=10)
        check(r.status_code == 404, "raw missing file -> 404")

        # -- save + validate ---------------------------------------------
        mob = HTTP.get(api + "read", params={"mod": MOD, "file": "mobsDesc/ExampleMob.json"},
                           timeout=10).json()["content"]
        mob = mob.replace('"ht" :8', '"ht" :42').replace('"ht": 8', '"ht": 42')
        r = HTTP.post(api + "save", json={"mod": MOD, "file": "mobsDesc/ExampleMob.json",
                                              "content": mob}, timeout=10)
        check(r.ok, "save edited mob json")

        r = HTTP.post(api + "validate", json={"name": MOD}, timeout=30)
        check(r.ok and r.json().get("success"), "validate passes: %s"
              % r.json().get("errors"))

        # -- guards ------------------------------------------------------
        r = HTTP.post(api + "save", json={"mod": MOD, "file": "../../etc/passwd",
                                              "content": "x"}, timeout=10)
        check(r.status_code in (400, 403), "traversal save blocked")
        r = HTTP.post(api + "save", json={"mod": MOD, "file": "mobsDesc/Bad.json",
                                              "content": "{oops"}, timeout=10)
        check(r.status_code == 400, "invalid json save -> 400")
        r = HTTP.post(api + "save", json={"mod": "Remixed", "file": "mobsDesc/Rat.json",
                                              "content": "{}"}, timeout=10)
        # 403 on release builds (main-mod read-only); on dev platforms
        # (headless/desktop) mods/Remixed usually has no installed dir -> 404.
        # Either way the write must NOT succeed.
        check(r.status_code in (403, 404), "main-mod write blocked (%d)" % r.status_code)
        # mod-name traversal must not resolve anywhere
        for bad in ["..", "a/b", "../" + MOD]:
            r = HTTP.get(api + "files", params={"mod": bad}, timeout=10)
            check(r.status_code == 404, "bad mod name '%s' -> 404" % bad)

        # -- downloaded-mod dir names (bd b4i) ----------------------------
        # mods from the server install under their packaged root dir name -
        # spaces and dots included. The editor must address them like the
        # game does. Plant one the way the unzip installer would.
        downloaded = "My Downloaded.Mod"
        ddir = os.path.join(game_dir, "mods", downloaded)
        os.makedirs(os.path.join(ddir, "mobsDesc"), exist_ok=True)
        with open(os.path.join(ddir, "version.json"), "w") as f:
            f.write('{"version":1,"name":"Downloaded QA","author":"QA",'
                    '"description":"","hr_version":"1","rpd_version":1}')
        with open(os.path.join(ddir, "mobsDesc", "QaMob.json"), "w") as f:
            f.write('{"ht":10,"name":"QaMob_Name","spriteDesc":"spritesDesc/Rat.json"}')

        r = HTTP.get(api + "list", timeout=10)
        check(any(m["installDir"] == downloaded for m in r.json()["mods"]),
              "downloaded-style mod listed")
        r = HTTP.get(api + "files", params={"mod": downloaded}, timeout=10)
        names = [e["name"] for e in r.json()["entries"]]
        check(r.ok and "mobsDesc" in names, "files listing works for space+dot name")
        r = HTTP.get(api + "read", params={"mod": downloaded, "file": "mobsDesc/QaMob.json"},
                     timeout=10)
        check(r.ok and "QaMob" in r.json()["content"], "read works for space+dot name")
        r = HTTP.post(api + "save", json={"mod": downloaded, "file": "mobsDesc/QaMob.json",
                                          "content": '{"ht":11,"name":"QaMob_Name",'
                                                     '"spriteDesc":"spritesDesc/Rat.json"}'},
                      timeout=10)
        check(r.ok, "save works for space+dot name")
        r = HTTP.post(api + "validate", json={"name": downloaded}, timeout=30)
        check(r.ok, "validate accepts space+dot name")
        r = HTTP.get(api + "export", params={"mod": downloaded}, timeout=30)
        check(r.ok, "export works for space+dot name")
        zf = zipfile.ZipFile(io.BytesIO(r.content))
        check(downloaded + "/version.json" in zf.namelist(), "zip root = space+dot dir name")
        r = HTTP.post(api + "duplicate", json={"from": downloaded, "to": "DownloadedCopy"},
                      timeout=10)
        check(r.ok, "duplicate from space+dot name to strict name")
        r = HTTP.post(api + "delete", json={"name": "DownloadedCopy"}, timeout=10)
        check(r.ok, "delete the duplicate")
        time.sleep(2)

        # -- audio binaries (bd b4i) --------------------------------------
        ogg = b"OggS\x00\x02" + b"\x00" * 64
        import base64
        r = HTTP.post(api + "save_binary", json={"mod": MOD, "file": "sound/test.ogg",
                                                 "data": base64.b64encode(ogg).decode()},
                      timeout=10)
        check(r.ok, "save_binary accepts ogg payload")
        r = HTTP.get(api + "raw", params={"mod": MOD, "file": "sound/test.ogg"}, timeout=10)
        check(r.ok and r.content[:4] == b"OggS"
              and r.headers.get("Content-Type", "").startswith("audio/ogg"),
              "raw serves saved ogg with audio mime")
        r = HTTP.post(api + "save_binary", json={"mod": MOD, "file": "sound/fake.ogg",
                                                 "data": base64.b64encode(b"\x89PNG\r\n\x1a\n" + b"x" * 32).decode()},
                      timeout=10)
        check(r.status_code == 400, "png bytes named .ogg bounce (magic mismatch)")
        r = HTTP.post(api + "save_binary", json={"mod": MOD, "file": "sound/evil.exe",
                                                 "data": base64.b64encode(b"MZ" + b"\x00" * 64).decode()},
                      timeout=10)
        check(r.status_code == 400, "non-asset extension bounces")
        r = HTTP.post(api + "delete_file", json={"mod": MOD, "file": "sound/test.ogg"},
                      timeout=10)
        check(r.ok, "delete saved ogg")

        # -- clone builtin (bd b4i) ----------------------------------------
        # while Remixed is active, so the clone is the pure builtin view
        r = HTTP.post(api + "clone_builtin", json={"to": "FullClone", "title": "Full Clone"},
                      timeout=120)
        check(r.ok and r.json().get("success"), "clone_builtin")
        cloned_files = r.json().get("files", 0)
        check(cloned_files > 100, "clone copied substantial content (%d files)" % cloned_files)
        r = HTTP.get(api + "read", params={"mod": "FullClone", "file": "mobsDesc/Rat.json"},
                     timeout=10)
        check(r.ok and "spriteDesc" in r.json()["content"], "cloned mob json readable")
        r = HTTP.get(api + "files", params={"mod": "FullClone"}, timeout=10)
        top = [e["name"] for e in r.json()["entries"]]
        check("web" not in top and "html" not in top, "clone skips web apps and bootstrap")
        check("scripts" in top and "sound" in top, "clone carries scripts and sound")
        r = HTTP.get(api + "read", params={"mod": "FullClone", "file": "version.json"},
                     timeout=10)
        check(r.ok and "Full Clone" in r.json()["content"], "clone gets a fresh manifest")
        r = HTTP.post(api + "validate", json={"name": "FullClone"}, timeout=120)
        check(r.ok and r.json().get("success"), "cloned mod validates: %s"
              % r.json().get("errors", [])[:3])

        # -- export ------------------------------------------------------
        r = HTTP.get(api + "export", params={"mod": MOD}, timeout=30)
        check(r.ok, "export zip")
        zf = zipfile.ZipFile(io.BytesIO(r.content))
        check(MOD + "/version.json" in zf.namelist(), "zip top-level dir = mod name")

        # -- select + play ------------------------------------------------
        select_and_restart(MOD)
        r = HTTP.get(api + "list", timeout=10)
        mine = [m for m in r.json()["mods"] if m["installDir"] == MOD]
        check(mine and mine[0]["active"], "mod listed as active")

        r = HTTP.get(base + "/debug/start_game", timeout=30)
        check(r.ok, "start_game on editor-created mod")
        time.sleep(4)
        r = HTTP.get(base + "/debug/get_level_info", timeout=10)
        info = r.json()
        # template Start room: 15x15 PredesignedLevel at depth 0
        check(info.get("width") == 15 and info.get("height") == 15
              and info.get("name") == "PredesignedLevel", "hero starts in the Start room: %s" % info)
        r = HTTP.get(base + "/debug/get_mobs", timeout=10)
        check(r.json().get("count", 0) >= 1, "ExampleMob spawned in the Start room")

        # -- switch back, delete -----------------------------------------
        select_and_restart("Remixed")
        r = HTTP.post(api + "delete", json={"name": "FullClone"}, timeout=10)
        check(r.ok, "delete cloned mod")
        r = HTTP.post(api + "delete", json={"name": downloaded}, timeout=10)
        check(r.ok, "delete downloaded-style mod")
        r = HTTP.post(api + "delete", json={"name": MOD}, timeout=10)
        check(r.ok, "delete mod")
        time.sleep(3)
        r = HTTP.get(api + "files", params={"mod": MOD}, timeout=10)
        check(r.status_code == 404, "mod dir gone")

        print("\nALL PASS")
        return 0
    except Fail as e:
        print("\nFAIL: " + str(e))
        return 1
    finally:
        stop()
        if not args.keep:
            import shutil
            shutil.rmtree(tmp, ignore_errors=True)


if __name__ == "__main__":
    sys.exit(main())
