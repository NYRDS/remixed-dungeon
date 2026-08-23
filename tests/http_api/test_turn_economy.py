#!/usr/bin/env python3
"""
Turn economy regression test (beads snap-8ok).

Beta 32.4.beta.1+ regression: after every hero action the hero burned an extra
full TICK (unconditional spend in Hero.act idle branch + auto-spend in
Char.readyAndIdle). Symptoms reported by players: attacks take 2 turns, gas
stunlock, pets land on clicked tiles, turn skip after giving an order.

This test verifies one melee attack costs the hero exactly one attack delay
(~1 tick), not ~2 ticks.

Setup: create a mob next to the hero, attack it repeatedly via /debug/hero_attack,
sample hero actor_time via /debug/get_game_state (actor_time field) before and
after each attack, and check the delta.

Run: python3 test_turn_economy.py --start-server
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
        self.warnings: List[str] = []
        self.passed = 0
        self.failed = 0

    def start_server(self) -> bool:
        print("STARTING GAME SERVER")
        script_dir = os.path.dirname(os.path.abspath(__file__))
        project_root = os.path.dirname(os.path.dirname(script_dir))
        self.log_file = tempfile.mktemp(prefix="game_turn_economy_", suffix=".log")
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
        """start_game is async; poll until hero appears in game state."""
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

    def _wait_hero_ready(self, timeout: float = 15.0) -> bool:
        """Wait until hero actor_time stops advancing (turn-based game idle)."""
        start = time.time()
        last_time = None
        stable_count = 0
        while time.time() - start < timeout:
            state = self.client.get_game_state()
            t = state.get("hero", {}).get("actor_time")
            if t is not None and t == last_time:
                stable_count += 1
                if stable_count >= 3:
                    return True
            else:
                stable_count = 0
                last_time = t
            time.sleep(0.3)
        return False

    def hero_actor_time(self) -> Optional[float]:
        state = self.client.get_game_state()
        try:
            return state["hero"]["actor_time"]
        except (KeyError, TypeError):
            return None

    def test_attack_cost(self):
        """One melee attack must advance hero time by ~1 tick, not ~2."""
        result = self.client.start_game("WARRIOR", 0)
        if "error" in result:
            print(f"  FAIL: could not start game: {result['error']}")
            self.failed += 1
            return
        if not self._wait_for_game():
            print("  FAIL: game did not initialize")
            self.failed += 1
            return

        t = self.hero_actor_time()
        if t is None:
            print("  FAIL: actor_time missing from get_game_state")
            self.failed += 1
            return

        # spawn a rat directly adjacent to the hero
        pos = self.client.get_hero_position()
        hx, hy = pos.get("x", -1), pos.get("y", -1)
        if hx < 0:
            print(f"  FAIL: no hero position: {pos}")
            self.failed += 1
            return

        rx, ry = -1, -1
        rat_id = -1
        for cand in [(hx + 1, hy), (hx - 1, hy), (hx, hy + 1), (hx, hy - 1)]:
            created = self.client._get(f"/debug/create_mob?type=Rat&x={cand[0]}&y={cand[1]}")
            if created.get("success"):
                rx, ry = cand
                rat_id = created.get("id", -1)
                break
        if rx < 0:
            print("  FAIL: could not spawn rat adjacent to hero")
            self.failed += 1
            return

        deltas = []
        for i in range(3):
            if not self._wait_hero_ready():
                print("  WARN: hero time still advancing before attack sample")

            # rat may move between attacks; re-locate it by id each round
            mobs = self.client.get_mobs()
            target = None
            for m in mobs.get("mobs", []):
                if m.get("id") == rat_id:
                    target = m
                    break
            if target is None:
                print(f"  note: rat gone before attack {i}")
                break
            rx, ry = target.get("x", rx), target.get("y", ry)

            before = self.hero_actor_time()
            r = self.client.hero_attack(rx, ry)
            if "error" in r:
                # rat may have died or moved; stop sampling
                print(f"  note: attack {i} error: {r['error']}")
                break
            # wait for attack animation + turn processing
            if not self._wait_hero_ready():
                print("  WARN: hero not idle after attack")
            after = self.hero_actor_time()
            if before is None or after is None:
                print("  FAIL: actor_time disappeared")
                self.failed += 1
                return
            deltas.append(after - before)

        if not deltas:
            print("  SKIP: no attack samples collected")
            return

        print(f"  attack cost samples: {[round(d, 2) for d in deltas]}")
        # regression: double-spend made attacks cost ~2 ticks.
        # one attack should cost ~1 tick (attackDelay ~1.0).
        max_cost = max(deltas)
        if max_cost > 1.6:
            print(f"  FAIL: attack cost {max_cost:.2f} ticks (expected <= 1.6) - double-spend regression present")
            self.failed += 1
        else:
            print(f"  PASS: attack cost {max_cost:.2f} ticks")
            self.passed += 1

    def run(self) -> int:
        if not self.start_server():
            print("FATAL: could not start server")
            return 1
        try:
            self.test_attack_cost()
        finally:
            self.stop_server()

        if self.log_monitor:
            self.errors = self.log_monitor.get_all_errors()
            self.warnings = self.log_monitor.get_all_warnings()

        print("\n" + "=" * 60)
        print("RESULTS")
        print("=" * 60)
        print(f"Passed: {self.passed}")
        print(f"Failed: {self.failed}")
        if self.errors:
            print(f"Game errors ({len(self.errors)}):")
            for e in self.errors[:10]:
                print(f"  {e}")
        return 1 if self.failed else 0


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--start-server", action="store_true")
    args = parser.parse_args()

    runner = TestRunner()
    if not args.start_server:
        # assume server already running
        runner.server_process = None
        runner.log_monitor = None
        runner.start_server = lambda: True
        if not runner.client.check_server():
            print("Server not running; use --start-server")
            return 1
        runner.client.start_game("WARRIOR", 0)
        runner._wait_for_game()
        runner.test_attack_cost()
        return 0

    return runner.run()


if __name__ == "__main__":
    sys.exit(main())
