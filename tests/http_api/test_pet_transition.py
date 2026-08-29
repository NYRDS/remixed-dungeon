#!/usr/bin/env python3
"""
Pet level-transition regression test (beads snap-2f1).

Exercises the real InterlevelScene transition path and the full save-bundle
load path with a hero pet:

1. create an owned pet on the starting level
2. descend -> pet must follow, exactly ONE copy (dup guard holds)
3. ascend  -> same, exactly ONE copy
4. reload_game (loadGame + level restore = crash+restart equivalent)
   -> pet restored from the follower roster persisted in the game bundle,
      exactly ONE copy

Run: python3 test_pet_transition.py --start-server
"""

import subprocess
import sys
import time
import os
import signal
import argparse
import tempfile
from typing import Optional, List

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from game_client import GameClient
from log_monitor import LogMonitor


class TestRunner:
    def __init__(self, host: str = "localhost", port: int = 8080):
        self.host = host
        self.port = port
        self.client = GameClient(host, port)
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
        self.log_file = tempfile.mktemp(prefix="game_pet_transition_", suffix=".log")
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

    def count_pets(self, mob_type: str):
        mobs = self.client.get_mobs()
        pets = []
        for m in mobs.get("mobs", []):
            if not m.get("owned"):
                continue
            # bundle JSON carries the java class in __className
            cls = m.get("__className", "")
            kind = m.get("type", "")
            if f".{mob_type}" in cls or kind == mob_type:
                pets.append(m)
        return len(pets)

    def step_expect_one_pet(self, label: str, mob_type: str = "Rat"):
        count = self.count_pets(mob_type)
        if count == 1:
            print(f"  PASS: {label}: exactly 1 {mob_type} pet")
            self.passed += 1
        else:
            msg = f"{label}: expected 1 {mob_type} pet, found {count}"
            print(f"  FAIL: {msg}")
            self.errors.append(msg)
            self.failed += 1

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

        created = self.client.create_mob("Rat", owned=True)
        if not created.get("success"):
            print(f"  FAIL: could not create pet: {created}")
            self.failed += 1
            return
        time.sleep(1)
        self.step_expect_one_pet("baseline (level 1)")

        # descend through the REAL transition path (InterlevelScene DESCEND:
        # strip followers -> roster into bundle -> load level -> dup-guarded spawn).
        # new games start in town_2; the dungeon entrance level is "1"
        descend = self.client.descend_to("1")
        if not descend.get("success"):
            print(f"  FAIL: descend_to('2') failed: {descend}")
            self.failed += 1
            return
        time.sleep(1)
        self.step_expect_one_pet("after descend to level 2")

        ascend = self.client.ascend()
        if isinstance(ascend, dict) and not ascend.get("success"):
            print(f"  FAIL: ascend failed: {ascend}")
            self.failed += 1
            return
        time.sleep(1)
        self.step_expect_one_pet("after ascend back to level 1")

        # full load path: loadGame from the bundle + level restore.
        # pet must come back from the persisted follower roster.
        reload = self.client.reload_game()
        if not reload.get("success"):
            print(f"  FAIL: reload_game failed: {reload}")
            self.failed += 1
            return
        time.sleep(2)
        self.step_expect_one_pet("after reload_game (roster restore)")

        state = self.client.get_game_state()
        if "hero" in state:
            print("  PASS: game state healthy after reload")
            self.passed += 1
        else:
            print("  FAIL: no hero in game state after reload")
            self.failed += 1

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
    parser = argparse.ArgumentParser(description="Pet level-transition test")
    parser.add_argument("--start-server", action="store_true",
                        help="Start the game server automatically")
    parser.add_argument("--host", default="localhost")
    parser.add_argument("--port", type=int, default=8080)
    args = parser.parse_args()

    runner = TestRunner(args.host, args.port)

    if args.start_server:
        if not runner.start_server():
            print("FAILED to start server")
            return 1

    try:
        # give gradle/game a moment after /ready
        time.sleep(2)
        runner.run_all()
    finally:
        if args.start_server:
            runner.stop_server()

    ok = runner.print_results()
    return 0 if ok else 1


if __name__ == "__main__":
    sys.exit(main())
