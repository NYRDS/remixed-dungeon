#!/usr/bin/env python3
"""
Test for BloodTransfusion spell crash fix.

Reproduces the original crash: casting BloodTransfusion on an owned mob
called the non-existent MagicMissile:bleeding() method.

Crash: org.luaj.vm2.LuaError: attempt to call nil
       at scripts/spells/BloodTransfusion.lua:36

Usage:
    python3 test_blood_transfusion.py [--port PORT] [--host HOST]
    python3 test_blood_transfusion.py --start-server

Prerequisites (if not using --start-server):
    - Desktop game running with webserver:
      ./gradlew -p RemixedDungeonDesktop runDesktopGameWithWebServer --args="--windowed"
"""

import os
import sys
import time
import signal
import argparse
import subprocess
import tempfile
from typing import Optional

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
        self.errors = []
        self.passed = 0
        self.failed = 0

    def start_server(self) -> bool:
        print("STARTING GAME SERVER")
        script_dir = os.path.dirname(os.path.abspath(__file__))
        project_root = os.path.dirname(os.path.dirname(script_dir))
        self.log_file = tempfile.mktemp(prefix="bt_test_", suffix=".log")
        print(f"Log file: {self.log_file}")

        cmd = [
            "./gradlew", "-p", "RemixedDungeonDesktop",
            "runDesktopGameWithWebServer",
            "--args=--webserver=8080 --windowed"
        ]
        log_handle = open(self.log_file, 'w')
        self.server_process = subprocess.Popen(
            cmd, cwd=project_root,
            stdout=log_handle, stderr=subprocess.STDOUT,
            preexec_fn=os.setsid
        )
        self.log_monitor = LogMonitor(self.log_file)
        self.log_monitor.start()

        print("Waiting for server...", end="", flush=True)
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
        if self.log_monitor:
            self.log_monitor.stop()
        if self.server_process:
            try:
                os.killpg(os.getpgid(self.server_process.pid), signal.SIGTERM)
                self.server_process.wait(timeout=5)
            except (subprocess.TimeoutExpired, ProcessLookupError):
                try:
                    os.killpg(os.getpgid(self.server_process.pid), signal.SIGKILL)
                except ProcessLookupError:
                    pass
            time.sleep(1)

    def _run_test(self, name: str, test_func) -> bool:
        if self.log_monitor:
            self.log_monitor.set_test(name)
        try:
            result = test_func()
            if result:
                self.passed += 1
                print(f"  PASS: {name}")
                return True
            self.failed += 1
            print(f"  FAIL: {name}")
            return False
        except Exception as e:
            self.failed += 1
            print(f"  FAIL: {name} - Exception: {e}")
            return False
        finally:
            if self.log_monitor:
                self.log_monitor.set_test(None)

    def _check_lua_errors(self, test_name: str) -> bool:
        """Check for LuaError in game logs. Returns True if clean."""
        if not self.log_monitor:
            return True
        errors = self.log_monitor.get_test_errors(test_name)
        for err in errors:
            if "LuaError" in err or "attempt to call nil" in err:
                print(f"    LuaError detected: {err[:150]}")
                return False
        return True


def _wait_for_game(runner: TestRunner, timeout: int = 15) -> bool:
    """Wait until game state is fully initialized."""
    start = time.time()
    while time.time() - start < timeout:
        state = runner.client.get_game_state()
        if "hero" in state and "error" not in state:
            return True
        time.sleep(1)
    return False


def test_bt_on_owned_mob(runner: TestRunner) -> bool:
    """Reproduce crash: cast BloodTransfusion on a mob owned by the hero."""
    print("\nTEST: BloodTransfusion on owned mob (crash regression)")
    print("-" * 40)

    result = runner.client.start_game("DOCTOR")
    if not result.get("success"):
        print(f"  Could not start Doctor game: {result.get('error')}")
        return False
    print("  Starting Doctor game...", end="", flush=True)
    if not _wait_for_game(runner):
        print(" game did not initialize")
        return False
    print(" ready")

    # Create an owned mob (pet) - this is the scenario that triggers the crash
    # BloodTransfusion only executes its drain/heal logic when target:getOwnerId() == caster:getId()
    result = runner.client.create_mob("Rat", owned=True)
    if "error" in result:
        print(f"  Could not create owned mob: {result.get('error')}")
        return False
    print(f"  Created owned Rat at ({result.get('x')}, {result.get('y')})")
    time.sleep(1)

    # Cast BloodTransfusion - will target random mob including the owned one
    result = runner.client.cast_spell("BloodTransfusion")
    if not result.get("success"):
        print(f"  Could not cast spell: {result.get('error')}")
        return False
    print("  Cast BloodTransfusion")
    time.sleep(2)

    # Check for Lua crash in logs
    if not runner._check_lua_errors("bt_owned_mob"):
        return False

    # Verify game is still responsive
    state = runner.client.get_game_state()
    if "error" in state:
        print(f"  Game unresponsive after spell: {state['error']}")
        return False

    print("  Game state OK - no crash")
    return True


def test_bt_on_hostile_mob(runner: TestRunner) -> bool:
    """Verify spell rejects hostile (non-owned) mobs gracefully."""
    print("\nTEST: BloodTransfusion on hostile mob (graceful rejection)")
    print("-" * 40)

    result = runner.client.start_game("DOCTOR")
    if not result.get("success"):
        print(f"  Could not start Doctor game: {result.get('error')}")
        return False
    print("  Starting Doctor game...", end="", flush=True)
    if not _wait_for_game(runner):
        print(" game did not initialize")
        return False
    print(" ready")

    # Create a hostile mob
    result = runner.client.create_mob("Rat", owned=False)
    if "error" in result:
        print(f"  Could not create mob: {result.get('error')}")
        return False
    print(f"  Created hostile Rat at ({result.get('x')}, {result.get('y')})")
    time.sleep(1)

    result = runner.client.cast_spell("BloodTransfusion")
    if not result.get("success"):
        print(f"  Could not cast spell: {result.get('error')}")
        return False
    print("  Cast BloodTransfusion")
    time.sleep(2)

    if not runner._check_lua_errors("bt_hostile_mob"):
        return False

    state = runner.client.get_game_state()
    if "error" in state:
        print(f"  Game unresponsive: {state['error']}")
        return False

    print("  Game state OK")
    return True


def run_all(runner: TestRunner) -> int:
    runner._run_test("bt_on_owned_mob", lambda: test_bt_on_owned_mob(runner))
    runner._run_test("bt_on_hostile_mob", lambda: test_bt_on_hostile_mob(runner))

    print()
    print("=" * 60)
    print("SUMMARY")
    print("=" * 60)
    print(f"  Passed: {runner.passed}")
    print(f"  Failed: {runner.failed}")

    # Collect log errors
    if runner.log_monitor:
        all_errors = runner.log_monitor.get_all_errors()
        lua_errors = [e for e in all_errors if "LuaError" in e or "attempt to call nil" in e]
        if lua_errors:
            print(f"\n  Lua errors found in logs:")
            for err in lua_errors[:5]:
                print(f"    {err[:150]}")
            return 1

    if runner.failed > 0:
        return 1
    print("\nAll tests passed!")
    return 0


def main():
    parser = argparse.ArgumentParser(description="Test BloodTransfusion crash fix")
    parser.add_argument("--host", default="localhost")
    parser.add_argument("--port", type=int, default=8080)
    parser.add_argument("--start-server", action="store_true",
                        help="Start game server automatically")
    args = parser.parse_args()

    runner = TestRunner(args.host, args.port)

    if args.start_server:
        try:
            if not runner.start_server():
                return 1
            return run_all(runner)
        finally:
            runner.stop_server()
    else:
        if not runner.client.check_server():
            print("Game server not running.")
            print("Start with: ./gradlew -p RemixedDungeonDesktop runDesktopGameWithWebServer --args=\"--windowed\"")
            print("Or use --start-server flag")
            return 1
        return run_all(runner)


if __name__ == "__main__":
    sys.exit(main())
