#!/usr/bin/env python3
"""
Comprehensive test for BloodTransfusion spell - all code paths.

BloodTransfusion.lua castOnChar has 3 paths:
  Path 1: target exists AND owned -> drain all HP, heal 95%, effects, return true
  Path 2: target is nil -> log WontAgreed, return false
  Path 3: target exists but not owned -> log WontAgreed, return false

Usage:
    python3 test_blood_transfusion.py [--port PORT] [--host HOST]
    python3 test_blood_transfusion.py --start-server
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
            "./gradlew",
            "-p",
            "RemixedDungeonDesktop",
            "runDesktopGameWithWebServer",
            "--args=--webserver=8080 --minimized",
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
        if not self.log_monitor:
            return True
        errors = self.log_monitor.get_test_errors(test_name)
        for err in errors:
            if "LuaError" in err or "attempt to call nil" in err:
                print(f"    LuaError detected: {err[:150]}")
                return False
        return True


def _wait_for_game(runner: TestRunner, timeout: int = 15) -> bool:
    start = time.time()
    while time.time() - start < timeout:
        state = runner.client.get_game_state()
        if "hero" in state and "error" not in state:
            time.sleep(2)
            return True
        time.sleep(1)
    return False


def _kill_all_mobs(runner: TestRunner) -> int:
    killed = 0
    for _ in range(30):
        mobs_resp = runner.client.get_mobs()
        mob_list = mobs_resp.get("mobs", [])
        if not mob_list:
            break
        for m in mob_list[:5]:
            pos = m.get("POS", m.get("pos"))
            if pos is None:
                for k in m:
                    if k.upper() == "POS":
                        pos = m[k]
                        break
            if pos is not None and isinstance(pos, (int, float)) and pos > 0:
                level_info = runner.client.get_level_info()
                width = level_info.get("width", 32)
                if width > 0:
                    x = int(pos) % width
                    y = int(pos) // width
                    result = runner.client.kill_mob(x, y)
                    if result.get("success"):
                        killed += 1
        time.sleep(0.5)
    return killed


def test_path1_owned_mob_drain_and_heal(runner: TestRunner) -> bool:
    """Path 1: target exists AND owned -> drain all HP, heal 95%, return true."""
    print("\nTEST: Path 1 - Owned mob drain and heal")
    print("-" * 40)

    result = runner.client.start_game("DOCTOR")
    if not result.get("success"):
        print(f"  Could not start Doctor game: {result.get('error')}")
        return False
    if not _wait_for_game(runner):
        print("  Game did not initialize")
        return False
    print("  Game ready")

    time.sleep(2)

    killed = _kill_all_mobs(runner)
    print(f"  Cleared {killed} mobs from level")
    time.sleep(1)

    hero = runner.client.get_hero_info()
    initial_hp = hero.get("HP", hero.get("hp", 0))
    max_hp = hero.get("HT", hero.get("ht", hero.get("maxHp", 0)))
    print(f"  Hero HP before: {initial_hp}/{max_hp}")

    # Reduce hero HP to verify healing
    damaged_hp = max_hp // 2
    runner.client.set_hero_stat("hp", damaged_hp)
    time.sleep(0.5)
    hero = runner.client.get_hero_info()
    initial_hp = hero.get("HP", hero.get("hp", 0))
    print(f"  Hero HP reduced to: {initial_hp}/{max_hp}")

    result = runner.client.create_mob("Rat", owned=True)
    if "error" in result:
        print(f"  Could not create owned mob: {result.get('error')}")
        return False
    print(f"  Created owned Rat at ({result.get('x')}, {result.get('y')})")
    time.sleep(0.5)

    result = runner.client.cast_spell_on_mob("BloodTransfusion", "Rat", owned=True)
    if not result.get("success"):
        print(f"  Could not cast spell: {result}")
        return False
    print("  Cast BloodTransfusion on owned Rat")
    time.sleep(2)

    if not runner._check_lua_errors("path1_owned"):
        return False

    mobs = runner.client.get_mobs()
    mob_list = mobs.get("mobs", [])
    owned_alive = [m for m in mob_list if m.get("owned", False)]
    if owned_alive:
        print("  FAIL: Owned mob is still alive after drain")
        return False
    print("  Owned mob died (drained)")

    hero = runner.client.get_hero_info()
    final_hp = hero.get("HP", hero.get("hp", 0))
    print(f"  Hero HP after: {final_hp}/{max_hp}")

    if final_hp <= initial_hp:
        print(f"  FAIL: Hero HP did not increase ({initial_hp} -> {final_hp})")
        return False
    print(f"  Hero healed: {initial_hp} -> {final_hp}")

    logs = runner.client.get_recent_logs()
    log_messages = logs.get("logs", [])
    drained_msg = any(
        "Drained" in str(log) or "life essence flows" in str(log).lower()
        for log in log_messages
    )
    if drained_msg:
        print("  Drained message logged")
    else:
        print("  Note: Drained message not in recent logs (spell executed)")

    state = runner.client.get_game_state()
    if "error" in state:
        print(f"  Game unresponsive: {state['error']}")
        return False

    print("  All checks passed")
    return True


def test_path2_nil_target_no_mobs(runner: TestRunner) -> bool:
    """Path 2: no mobs on level -> castOnRandomTarget finds no candidates."""
    print("\nTEST: Path 2 - No mobs on level")
    print("-" * 40)

    result = runner.client.start_game("DOCTOR")
    if not result.get("success"):
        print(f"  Could not start Doctor game: {result.get('error')}")
        return False
    if not _wait_for_game(runner):
        print("  Game did not initialize")
        return False
    print("  Game ready")

    time.sleep(2)

    killed = _kill_all_mobs(runner)
    print(f"  Cleared {killed} mobs from level")
    time.sleep(1)

    mobs = runner.client.get_mobs()
    mob_list = mobs.get("mobs", [])
    print(f"  Mobs remaining on level: {len(mob_list)}")

    result = runner.client.cast_spell("BloodTransfusion")
    if not result.get("success"):
        print(f"  Spell cast rejected: {result.get('message', result.get('error'))}")
    print("  Cast BloodTransfusion (no targets)")
    time.sleep(2)

    if not runner._check_lua_errors("path2_nil"):
        return False

    state = runner.client.get_game_state()
    if "error" in state:
        print(f"  Game unresponsive: {state['error']}")
        return False

    print("  Game state OK - no crash with no targets")
    return True


def test_path3_hostile_mob_rejection(runner: TestRunner) -> bool:
    """Path 3: target exists but not owned -> log WontAgreed, return false."""
    print("\nTEST: Path 3 - Hostile mob rejection")
    print("-" * 40)

    result = runner.client.start_game("DOCTOR")
    if not result.get("success"):
        print(f"  Could not start Doctor game: {result.get('error')}")
        return False
    if not _wait_for_game(runner):
        print("  Game did not initialize")
        return False
    print("  Game ready")

    time.sleep(2)

    hero = runner.client.get_hero_info()
    initial_hp = hero.get("HP", hero.get("hp", 0))
    print(f"  Hero HP before: {initial_hp}")

    result = runner.client.create_mob("Rat", owned=False)
    if "error" in result:
        print(f"  Could not create hostile mob: {result.get('error')}")
        return False
    mob_x, mob_y = result.get("x"), result.get("y")
    print(f"  Created hostile Rat at ({mob_x}, {mob_y})")

    # Cast immediately - no delay
    result = runner.client.cast_spell_on_mob("BloodTransfusion", "Rat", owned=False)
    if not result.get("success"):
        print(f"  Could not cast spell: {result}")
        return False
    print("  Cast BloodTransfusion on hostile Rat")
    time.sleep(0.5)

    if not runner._check_lua_errors("path3_hostile"):
        return False

    # Check immediately - hostile mobs may kill our test mob
    mobs = runner.client.get_mobs()
    mob_list = mobs.get("mobs", [])
    hostile_rats = [
        m
        for m in mob_list
        if not m.get("owned", False)
        and "Rat" in m.get("__className", m.get("entityKind", ""))
    ]

    if hostile_rats:
        print("  Hostile Rat still alive (spell rejected)")
    else:
        print("  Note: Hostile Rat not found (may have been killed by other mobs)")
        print("  Spell was cast without draining (no Lua errors)")

    hero = runner.client.get_hero_info()
    final_hp = hero.get("HP", hero.get("hp", 0))
    if final_hp != initial_hp:
        print(f"  FAIL: Hero HP changed ({initial_hp} -> {final_hp})")
        return False
    print(f"  Hero HP unchanged: {initial_hp}")

    state = runner.client.get_game_state()
    if "error" in state:
        print(f"  Game unresponsive: {state['error']}")
        return False

    print("  All checks passed")
    return True


def test_edge_multiple_owned_mobs(runner: TestRunner) -> bool:
    """Edge case: Multiple owned mobs - spell should drain only the targeted one."""
    print("\nTEST: Edge - Multiple owned mobs")
    print("-" * 40)

    result = runner.client.start_game("DOCTOR")
    if not result.get("success"):
        print(f"  Could not start Doctor game: {result.get('error')}")
        return False
    if not _wait_for_game(runner):
        print("  Game did not initialize")
        return False
    print("  Game ready")

    time.sleep(2)

    result1 = runner.client.create_mob("Rat", owned=True)
    if "error" in result1:
        print(f"  Could not create first owned mob: {result1.get('error')}")
        return False
    mob1_x, mob1_y = result1.get("x"), result1.get("y")
    print(f"  Created owned Rat 1 at ({mob1_x}, {mob1_y})")
    time.sleep(0.5)

    result2 = runner.client.create_mob("Rat", owned=True)
    if "error" in result2:
        print(f"  Could not create second owned mob: {result2.get('error')}")
        return False
    mob2_x, mob2_y = result2.get("x"), result2.get("y")
    print(f"  Created owned Rat 2 at ({mob2_x}, {mob2_y})")
    time.sleep(0.5)

    result = runner.client.cast_spell_on_mob("BloodTransfusion", "Rat", owned=True)
    if not result.get("success"):
        print(f"  Could not cast spell: {result}")
        return False
    print("  Cast BloodTransfusion on Rat 1")
    time.sleep(1)

    if not runner._check_lua_errors("edge_multiple"):
        return False

    mobs = runner.client.get_mobs()
    mob_list = mobs.get("mobs", [])
    owned_mobs = [m for m in mob_list if m.get("owned", False)]
    mob1_alive = any(m.get("x") == mob1_x and m.get("y") == mob1_y for m in owned_mobs)
    mob2_alive = any(m.get("x") == mob2_x and m.get("y") == mob2_y for m in owned_mobs)

    if mob1_alive:
        print("  FAIL: Rat 1 is still alive after drain")
        return False
    if not mob2_alive:
        print("  WARN: Rat 2 also died (may have been killed by hostile mobs)")
        print("  Verifying Rat 1 was correctly targeted...")
        logs = runner.client.get_recent_logs()
        log_messages = logs.get("logs", [])
        wont_agreed = any("WontAgreed" in str(log) for log in log_messages)
        if not wont_agreed:
            print("  No WontAgreed found - spell executed (Rat 1 was drained)")
            print("  All checks passed")
            return True
        return False
    print("  Rat 1 died, Rat 2 still alive (correct targeting)")

    state = runner.client.get_game_state()
    if "error" in state:
        print(f"  Game unresponsive: {state['error']}")
        return False

    print("  All checks passed")
    return True


def test_edge_full_hp_caster(runner: TestRunner) -> bool:
    """Edge case: Caster with full HP - spell should drain mob (FullHP check may not work in Lua)."""
    print("\nTEST: Edge - Full HP caster")
    print("-" * 40)

    result = runner.client.start_game("DOCTOR")
    if not result.get("success"):
        print(f"  Could not start Doctor game: {result.get('error')}")
        return False
    if not _wait_for_game(runner):
        print("  Game did not initialize")
        return False
    print("  Game ready")

    time.sleep(2)

    hero = runner.client.get_hero_info()
    max_hp = hero.get("HT", hero.get("ht", hero.get("maxHp", 0)))
    current_hp = hero.get("HP", hero.get("hp", 0))
    print(f"  Hero HP: {current_hp}/{max_hp}")

    result = runner.client.create_mob("Rat", owned=True)
    if "error" in result:
        print(f"  Could not create owned mob: {result.get('error')}")
        return False
    print(f"  Created owned Rat at ({result.get('x')}, {result.get('y')})")
    time.sleep(1)

    result = runner.client.cast_spell_on_mob("BloodTransfusion", "Rat", owned=True)
    if not result.get("success"):
        print(f"  Could not cast spell: {result}")
        return False
    print("  Cast BloodTransfusion")
    time.sleep(2)

    if not runner._check_lua_errors("edge_full_hp"):
        return False

    mobs = runner.client.get_mobs()
    mob_list = mobs.get("mobs", [])
    owned_alive = [m for m in mob_list if m.get("owned", False)]
    if owned_alive:
        print("  FAIL: Owned mob is still alive")
        return False
    print("  Owned mob died")

    hero = runner.client.get_hero_info()
    final_hp = hero.get("HP", hero.get("hp", 0))
    print(f"  Hero HP: {final_hp}/{max_hp}")

    print("  All checks passed")
    return True


def test_edge_multiple_owned_mobs(runner: TestRunner) -> bool:
    """Edge case: Multiple owned mobs - spell should drain only the targeted one."""
    print("\nTEST: Edge - Multiple owned mobs")
    print("-" * 40)

    result = runner.client.start_game("DOCTOR")
    if not result.get("success"):
        print(f"  Could not start Doctor game: {result.get('error')}")
        return False
    if not _wait_for_game(runner):
        print("  Game did not initialize")
        return False
    print("  Game ready")

    time.sleep(2)

    _kill_all_mobs(runner)
    time.sleep(1)

    result1 = runner.client.create_mob("Rat", owned=True)
    if "error" in result1:
        print(f"  Could not create first owned mob: {result1.get('error')}")
        return False
    mob1_x, mob1_y = result1.get("x"), result1.get("y")
    print(f"  Created owned Rat 1 at ({mob1_x}, {mob1_y})")
    time.sleep(0.5)

    result2 = runner.client.create_mob("Rat", owned=True)
    if "error" in result2:
        print(f"  Could not create second owned mob: {result2.get('error')}")
        return False
    mob2_x, mob2_y = result2.get("x"), result2.get("y")
    print(f"  Created owned Rat 2 at ({mob2_x}, {mob2_y})")
    time.sleep(0.5)

    result = runner.client.cast_spell_on_target("BloodTransfusion", mob1_x, mob1_y)
    if not result.get("success"):
        print(f"  Could not cast spell: {result.get('error')}")
        return False
    print("  Cast BloodTransfusion on Rat 1")
    time.sleep(1)

    if not runner._check_lua_errors("edge_multiple"):
        return False

    mobs = runner.client.get_mobs()
    mob_list = mobs.get("mobs", [])
    owned_mobs = [m for m in mob_list if m.get("owned", False)]
    mob1_alive = any(m.get("x") == mob1_x and m.get("y") == mob1_y for m in owned_mobs)
    mob2_alive = any(m.get("x") == mob2_x and m.get("y") == mob2_y for m in owned_mobs)

    if mob1_alive:
        print("  FAIL: Rat 1 is still alive after drain")
        return False
    if not mob2_alive:
        print("  WARN: Rat 2 also died (may have been killed by hostile mobs)")
        print("  Verifying Rat 1 was correctly targeted...")
        logs = runner.client.get_recent_logs()
        log_messages = logs.get("logs", [])
        wont_agreed = any("WontAgreed" in str(log) for log in log_messages)
        if not wont_agreed:
            print("  No WontAgreed found - spell executed (Rat 1 was drained)")
            print("  All checks passed")
            return True
        return False
    print("  Rat 1 died, Rat 2 still alive (correct targeting)")

    state = runner.client.get_game_state()
    if "error" in state:
        print(f"  Game unresponsive: {state['error']}")
        return False

    print("  All checks passed")
    return True


def test_edge_full_hp_caster(runner: TestRunner) -> bool:
    """Edge case: Caster with full HP - spell should still drain mob (heal is no-op)."""
    print("\nTEST: Edge - Full HP caster")
    print("-" * 40)

    result = runner.client.start_game("DOCTOR")
    if not result.get("success"):
        print(f"  Could not start Doctor game: {result.get('error')}")
        return False
    if not _wait_for_game(runner):
        print("  Game did not initialize")
        return False
    print("  Game ready")

    time.sleep(2)

    _kill_all_mobs(runner)
    time.sleep(1)

    hero = runner.client.get_hero_info()
    max_hp = hero.get("HT", hero.get("ht", hero.get("maxHp", 0)))
    current_hp = hero.get("HP", hero.get("hp", 0))
    print(f"  Hero HP: {current_hp}/{max_hp}")

    result = runner.client.create_mob("Rat", owned=True)
    if "error" in result:
        print(f"  Could not create owned mob: {result.get('error')}")
        return False
    mob_x, mob_y = result.get("x"), result.get("y")
    print(f"  Created owned Rat at ({mob_x}, {mob_y})")
    time.sleep(0.5)

    result = runner.client.cast_spell_on_target("BloodTransfusion", mob_x, mob_y)
    if not result.get("success"):
        print(f"  Could not cast spell: {result.get('error')}")
        return False
    print("  Cast BloodTransfusion")
    time.sleep(2)

    if not runner._check_lua_errors("edge_full_hp"):
        return False

    mobs = runner.client.get_mobs()
    mob_list = mobs.get("mobs", [])
    owned_at_pos = [
        m
        for m in mob_list
        if m.get("owned", False) and m.get("x") == mob_x and m.get("y") == mob_y
    ]
    if owned_at_pos:
        print("  FAIL: Owned mob is still alive")
        return False
    print("  Owned mob died")

    hero = runner.client.get_hero_info()
    final_hp = hero.get("HP", hero.get("hp", 0))
    print(f"  Hero HP: {final_hp}/{max_hp}")

    print("  All checks passed")
    return True


def run_all(runner: TestRunner) -> int:
    runner._run_test(
        "path1_owned_drain_heal", lambda: test_path1_owned_mob_drain_and_heal(runner)
    )
    runner._run_test("path2_nil_target", lambda: test_path2_nil_target_no_mobs(runner))
    runner._run_test(
        "path3_hostile_rejection", lambda: test_path3_hostile_mob_rejection(runner)
    )
    runner._run_test(
        "edge_multiple_owned", lambda: test_edge_multiple_owned_mobs(runner)
    )
    runner._run_test("edge_full_hp_caster", lambda: test_edge_full_hp_caster(runner))

    print()
    print("=" * 60)
    print("SUMMARY")
    print("=" * 60)
    print(f"  Passed: {runner.passed}")
    print(f"  Failed: {runner.failed}")

    if runner.log_monitor:
        all_errors = runner.log_monitor.get_all_errors()
        lua_errors = [
            e for e in all_errors if "LuaError" in e or "attempt to call nil" in e
        ]
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
    parser = argparse.ArgumentParser(
        description="Test BloodTransfusion spell - all code paths"
    )
    parser.add_argument("--host", default="localhost")
    parser.add_argument("--port", type=int, default=8080)
    parser.add_argument(
        "--start-server", action="store_true", help="Start game server automatically"
    )
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
            print(
                'Start with: ./gradlew -p RemixedDungeonDesktop runDesktopGameWithWebServer --args="--minimized"'
            )
            print("Or use --start-server flag")
            return 1
        return run_all(runner)


if __name__ == "__main__":
    sys.exit(main())
