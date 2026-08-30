#!/usr/bin/env python3
"""
Remote-controlled chars suite (beads snap-ps4, spec
docs/superpowers/specs/2026-08-30-remote-controlled-chars-design.md).

Covers slice 1 of the puppet API:
- /debug/remote/spawn (stance friend|foe) + /debug/remote/list
- /debug/char_status?id=
- /debug/move_to?char=<id> - walk / attack through the puppet
- watchdog revert after revertAfter idle world turns
- persistence across reload_game
- friend puppet follows the hero through stairs (pet-transfer path)

Run: python3 test_remote_chars.py --start-server
"""

import subprocess
import sys
import time
import os
import signal
import argparse
import tempfile
from typing import Optional, List

import requests

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from game_client import GameClient
from log_monitor import LogMonitor


class TestRunner:
    def __init__(self, host: str = "localhost", port: int = 8080):
        self.puppets: List[int] = []
        self.host = host
        self.port = port
        self.client = GameClient(host, port)
        self.base_url = f"http://{host}:{port}"
        self.server_process: Optional[subprocess.Popen] = None
        self.log_file: Optional[str] = None
        self.log_monitor: Optional[LogMonitor] = None
        self.errors: List[str] = []
        self.passed = 0
        self.failed = 0

    def start_server(self) -> bool:
        print("STARTING GAME SERVER")
        script_dir = os.path.dirname(os.path.abspath(__file__))
        project_root = os.path.dirname(os.path.dirname(script_dir))
        self.log_file = tempfile.mktemp(prefix="game_llm_control_", suffix=".log")
        cmd = [
            "./gradlew",
            "-p",
            "RemixedDungeonDesktop",
            "runDesktopGameWithWebServer",
            "--args=--webserver=8080 --windowed",
        ]
        log_handle = open(self.log_file, "w")
        self.server_process = subprocess.Popen(
            cmd,
            cwd=project_root,
            stdout=log_handle,
            stderr=subprocess.STDOUT,
            preexec_fn=os.setsid,
        )
        self.log_monitor = LogMonitor(self.log_file)
        self.log_monitor.start()
        print("Waiting for server to start...", end="", flush=True)
        max_wait = 240
        start_time = time.time()
        while time.time() - start_time < max_wait:
            if self.client.check_server():
                print(" READY!")
                return True
            print(".", end="", flush=True)
            time.sleep(2)
        print(" TIMEOUT!")
        return False

    def stop_server(self):
        print("\nSTOPPING GAME SERVER")
        if self.log_monitor:
            self.log_monitor.stop()
            self.log_monitor = None
        if self.server_process:
            try:
                os.killpg(os.getpgid(self.server_process.pid), signal.SIGTERM)
                self.server_process.wait(timeout=10)
            except Exception:
                try:
                    os.killpg(os.getpgid(self.server_process.pid), signal.SIGKILL)
                except Exception:
                    pass
            self.server_process = None

    def _wait_for_game(self, timeout: int = 60) -> bool:
        print("Waiting for game init...", end="", flush=True)
        start = time.time()
        while time.time() - start < timeout:
            state = self.client.get_game_state()
            if "hero" in state:
                print(" READY!")
                return True
            print(".", end="", flush=True)
            time.sleep(1)
        print(" TIMEOUT!")
        return False

    def check(self, label: str, ok: bool, detail: str = ""):
        if ok:
            print(f"  PASS: {label}")
            self.passed += 1
        else:
            msg = f"{label} {detail}".rstrip()
            print(f"  FAIL: {msg}")
            self.errors.append(msg)
            self.failed += 1

    def get(self, path: str):
        try:
            return requests.get(self.base_url + path, timeout=10).json()
        except Exception as e:
            return {"error": f"request failed: {e}"}

    def raw_get(self, path: str):
        try:
            return requests.get(self.base_url + path, timeout=10).json()
        except Exception as e:
            return {"error": f"request failed: {e}"}

    def poll_char(self, pid: int, timeout=30):
        deadline = time.time() + timeout
        final = None
        while time.time() < deadline:
            final = self.get(f"/debug/char_status?id={pid}")
            if final.get("action") == "idle" or not final.get("alive"):
                break
            time.sleep(0.2)
        return final

    def test_friend_puppet_walk(self):
        r = self.raw_get("/debug/remote/spawn?type=Rat&stance=friend&revertAfter=0")
        self.check("friend: spawn", r.get("success") is True and r.get("id", 0) > 0, str(r))
        pid = r.get("id")
        if not pid:
            return
        self.puppets.append(pid)
        st = self.get(f"/debug/char_status?id={pid}")
        self.check("friend: char_status remote", st.get("remote") is True, str(st))
        self.check("friend: fraction HEROES", st.get("fraction") == "HEROES", str(st))
        self.check("friend: idle", st.get("action") == "idle", str(st))

        m = self.get("/debug/get_map")
        w = m["width"]
        x, y = st["x"], st["y"]
        target = None
        for d in (3, 2):
            for dx, dy in ((d, 0), (-d, 0), (0, d), (0, -d)):
                tx, ty = x + dx, y + dy
                if 0 <= tx < w and 0 <= ty < m["height"] and m["passable"][ty][tx] == "1":
                    target = (tx, ty)
                    break
            if target:
                break
        self.check("friend: target found", target is not None)
        if not target:
            return
        r = self.raw_get(f"/debug/move_to?char={pid}&x={target[0]}&y={target[1]}")
        self.check("friend: move accepted", r.get("success") is True, str(r))
        self.check("friend: resolved Move", "Move" in str(r.get("resolved", "")),
                   str(r.get("resolved")))

        # like a player: re-tap if the walk got interrupted by someone
        # standing in the way
        deadline = time.time() + 30
        final = None
        retaps = 0
        while time.time() < deadline and retaps < 5:
            final = self.poll_char(pid, 10)
            if final and (final["x"], final["y"]) == target:
                break
            if final and final.get("action") == "idle":
                self.raw_get(f"/debug/move_to?char={pid}&x={target[0]}&y={target[1]}")
                retaps += 1
        self.check("friend: reached target",
                   final and (final["x"], final["y"]) == target,
                   f"want {target} got {(final.get('x'), final.get('y')) if final else None} after {retaps} retaps")

    def test_foe_puppet_attacks_hero(self):
        hs = self.get("/debug/hero_status")
        r = self.raw_get(
            f"/debug/remote/spawn?type=Rat&stance=foe&x={hs['x'] + 1}&y={hs['y']}&revertAfter=0")
        pid = r.get("id")
        self.check("foe: spawn adjacent", r.get("success") is True and pid, str(r))
        if not pid:
            return
        self.puppets.append(pid)
        st = self.get(f"/debug/char_status?id={pid}")
        self.check("foe: fraction DUNGEON", st.get("fraction") == "DUNGEON", str(st))

        before = self.get("/debug/hero_status")["hp"]
        r = self.raw_get(f"/debug/move_to?char={pid}&x={hs['x']}&y={hs['y']}")
        self.check("foe: resolved Attack", "Attack" in str(r.get("resolved", "")),
                   str(r.get("resolved")))

        deadline = time.time() + 30
        after = before
        while time.time() < deadline:
            after = self.get("/debug/hero_status")["hp"]
            if after < before:
                break
            time.sleep(0.5)
        self.check("foe: hero took damage", after < before, f"hp {before} -> {after}")

    def test_multi_puppet_interleave(self):
        ids = []
        for i in range(2):
            r = self.raw_get("/debug/remote/spawn?type=Rat&stance=friend&revertAfter=0")
            if r.get("success"):
                ids.append(r["id"])
                self.puppets.append(r["id"])
        self.check("multi: two puppets spawned", len(ids) == 2, str(ids))
        if len(ids) < 2:
            return
        for pid in ids:
            st = self.get(f"/debug/char_status?id={pid}")
            self.check(f"multi: puppet {pid} status",
                       "error" not in st and st.get("remote") is True, str(st))
        lst = self.get("/debug/remote/list")
        listed = {e["id"] for e in lst.get("remote", [])}
        self.check("multi: both in remote/list", set(ids) <= listed, str(lst))

    def test_reload_persistence(self):
        # reload_game = CONTINUE = loads the last save. a fresh game has no
        # save yet, so create one the way real play does: walk through stairs.
        r = self.raw_get("/debug/remote/spawn?type=Rat&stance=friend&revertAfter=0")
        pid = r.get("id")
        self.check("reload: spawned", bool(pid), str(r))
        if not pid:
            return
        self.puppets.append(pid)
        before = self.get("/debug/hero_status").get("levelId")
        d = self.client.descend_to("1")
        if not d.get("success"):
            self.check("reload: descended to create save", False, str(d))
            return
        deadline = time.time() + 20
        while time.time() < deadline:
            if self.get("/debug/hero_status").get("levelId") != before:
                break
            time.sleep(0.5)
        time.sleep(2)
        rl = self.get("/debug/reload_game")
        if not rl.get("success"):
            self.check("reload: reload_game ok", False, str(rl))
            return
        time.sleep(3)
        st = self.get(f"/debug/char_status?id={pid}")
        self.check("reload: puppet alive after reload", st.get("alive") is True, str(st))
        self.check("reload: still remote", st.get("remote") is True, str(st))

    def test_descend_follow(self):
        # friend puppet follows the hero through stairs (pet-transfer path)
        r = self.raw_get("/debug/remote/spawn?type=Rat&stance=friend&revertAfter=0")
        pid = r.get("id")
        if not pid:
            self.check("descend: spawned", False, str(r))
            return
        self.puppets.append(pid)
        before = self.get("/debug/hero_status").get("levelId")
        d = self.client.descend_to("1")
        if not d.get("success"):
            self.check("descend: hero descended", False, str(d))
            return
        deadline = time.time() + 20
        lvl = before
        while time.time() < deadline:
            lvl = self.get("/debug/hero_status").get("levelId")
            if lvl != before:
                break
            time.sleep(0.5)
        self.check("descend: hero on new level", lvl != before, f"{before} -> {lvl}")
        st = self.get(f"/debug/char_status?id={pid}")
        self.check("descend: puppet followed",
                   st.get("alive") is True and st.get("levelId") == lvl,
                   f"puppet lvl={st.get('levelId')} hero lvl={lvl}")

    def test_watchdog_revert(self):
        r = self.raw_get("/debug/remote/spawn?type=Rat&stance=friend&revertAfter=3")
        pid = r.get("id")
        if not pid:
            self.check("watchdog: spawned", False, str(r))
            return
        self.puppets.append(pid)
        # burn world turns so the puppet's idle counter elapses
        deadline = time.time() + 45
        st = {}
        while time.time() < deadline:
            self.raw_get("/debug/wait_ticks?ticks=3")
            time.sleep(0.3)
            st = self.get(f"/debug/char_status?id={pid}")
            if st.get("reverted") or st.get("remote") is False:
                break
        self.check("watchdog: reverted flag", st.get("reverted") is True, str(st))
        self.check("watchdog: no longer remote", st.get("remote") is False, str(st))

    def run_test(self):
        result = self.client.start_game("WARRIOR", 0)
        if "error" in result:
            print(f"  FAIL: could not start game: {result.get('error')}")
            self.failed += 1
            return
        if not self._wait_for_game():
            print("  FAIL: game did not initialize")
            self.failed += 1
            return
        time.sleep(1)

        self.puppets = []
        self.test_friend_puppet_walk()
        self.test_foe_puppet_attacks_hero()
        self.test_multi_puppet_interleave()
        self.test_reload_persistence()
        self.test_descend_follow()
        self.test_watchdog_revert()


    def run_all(self):
        self.run_test()

    def print_results(self):
        print("\n" + "=" * 60)
        print("RESULTS")
        print("=" * 60)
        for err in self.errors:
            print(f"ERROR: {err}")
        print(f"Passed: {self.passed}")
        print(f"Failed: {self.failed}")
        return self.failed == 0


def main():
    parser = argparse.ArgumentParser(description="Remote-controlled chars test")
    parser.add_argument("--start-server", action="store_true",
                        help="Start the game server automatically")
    parser.add_argument("--host", default="localhost")
    parser.add_argument("--port", type=int, default=8080)
    args = parser.parse_args()

    runner = TestRunner(host=args.host, port=args.port)

    if args.start_server:
        if not runner.start_server():
            runner.stop_server()
            print("FAIL: server did not start")
            return 1

    try:
        runner.run_all()
    finally:
        if args.start_server:
            runner.stop_server()

    ok = runner.print_results()
    return 0 if ok else 1


if __name__ == "__main__":
    sys.exit(main())
