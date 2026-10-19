#!/usr/bin/env python3
"""
Test script for the Bishop quest line that unlocks the Priest class
(5 stages, Plague Doctor quest-line parity).

Quest line (scripts/npc/Bishop.lua):
    1. mobKill: 10 undead (any of the undead kinds)
    2. item: 3 Sungrass.Seed | 3 Moongrace.Seed (random variant)
    3. mob: tamed Zombie | tamed Bat delivered (random variant)
    4. mobKill: 2 DeathKnight | 4 EnslavedSoul (random variant)
    5. mobKill: 2 Succubus -> PRIEST_QUEST_COMPLETED badge + ceremony

Usage:
    python3 test_priest_quests.py [--port PORT] [--host HOST] [--no-server]

Prerequisites:
    - Headless jar with a CLEAN badge state (no saves/badges.dat - the quest
      line is skipped entirely when PRIEST_QUEST_COMPLETED is already unlocked):
      java -jar RemixedDungeon-Headless.jar --webserver=PORT --mod=Remixed
    - Or use --no-server to target an already running instance.

WebServer Endpoints used:
    - /debug/start_game?class=WARRIOR - quest line driven by a non-priest hero
    - /debug/go_to_level?id=church_2 - the Bishop lives in the church
    - /debug/interact?id=N - drive the real tap-a-mob Interact action on the Bishop
    - /debug/get_mobs - Bishop state via its luaData (serpent-dumped quest data)
    - /debug/create_mob, /debug/kill_mob - real spawn/death chains (kills count
      through quest.mobDied)
    - /debug/give_item?type=Sungrass.Seed - offering stage
    - /debug/run_lua?code=X - hero placement, badge/reward checks
"""

import os
import re
import sys
import time
import argparse
from typing import Dict, Any, Optional, Tuple

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from game_client import GameClient
from test_server import ServerManager

import urllib.parse


class BishopQuestTester:
    def __init__(self, host: str = "localhost", port: int = 8080):
        self.base_url = f"http://{host}:{port}"
        self.client = GameClient(host, port)

    # -- low level helpers ------------------------------------------------

    def run_lua(self, code: str) -> str:
        result = self.client.run_lua(code)
        if "error" in result:
            raise RuntimeError(f"run_lua failed: {result['error']}")
        return result.get("result", "")

    def bishop(self) -> Dict[str, Any]:
        mobs = self.client.get_mobs().get("mobs", [])
        for m in mobs:
            if m.get("type") == "BishopNPC":
                return m
        raise RuntimeError("BishopNPC not found on current level")

    def bishop_state(self) -> Dict[str, float]:
        """Parse the serpent-dumped luaData of the Bishop."""
        raw = self.bishop().get("luaData", "")
        state = {}
        for key in ("questIndex", "questVariant", "questInProgress"):
            m = re.search(rf"{key}=([0-9a-z.]+)", raw)
            if m:
                val = m.group(1)
                state[key] = val == "true" if key == "questInProgress" else float(val)
        return state

    def interact_bishop(self) -> Dict[str, Any]:
        b = self.bishop()
        return self.client._get(f"/debug/interact?id={b['id']}")

    def place_hero_next_to_bishop(self):
        """Bishop pos is known; pick a horizontal/vertical neighbor that works."""
        pos = self.bishop()["pos"]
        info = self.client.get_level_info()
        width = info.get("width", 32)
        for cell in (pos - 1, pos + 1, pos - width, pos + width):
            x, y = cell % width, cell // width
            cell_info = self.client._get(f"/debug/cell_info?x={x}&y={y}")
            if "error" in cell_info or cell_info.get("char") is not None:
                continue
            got = self.run_lua(
                'local RPD = require "scripts/lib/commonClasses" '
                f"RPD.Dungeon.hero:placeTo({cell}) return RPD.Dungeon.hero:getPos()"
            )
            if got == str(cell):
                return cell
        raise RuntimeError(f"no free cell next to Bishop at {pos}")

    def go_to(self, level_id: str):
        r = self.client.go_to_level(level_id)
        if "error" in r:
            raise RuntimeError(f"go_to_level({level_id}) failed: {r['error']}")
        time.sleep(3)

    def spawn_and_kill(self, mob_type: str, count: int) -> int:
        """Real spawn + real death - kills flow into quest.mobDied."""
        killed = 0
        for _ in range(count):
            r = self.client.create_mob(mob_type)
            if "id" not in r:
                print(f"    spawn {mob_type} failed: {r.get('error', r)}")
                break
            time.sleep(0.3)
            k = self.client.kill_mob(mob_id=r["id"])
            if k.get("success"):
                killed += 1
        return killed

    def hero_check_item(self, kind: str) -> str:
        return self.run_lua(
            'local RPD = require "scripts/lib/commonClasses" '
            f'local it = RPD.Dungeon.hero:checkItem("{kind}") '
            'return tostring(it ~= nil and it:quantity() or "none")'
        )

    def hero_gold(self) -> int:
        return int(self.run_lua(
            'local RPD = require "scripts/lib/commonClasses" '
            "return RPD.Dungeon.hero:gold()"))

    def priest_badge(self) -> bool:
        return self.run_lua(
            'local RPD = require "scripts/lib/commonClasses" '
            'return tostring(RPD.checkBadge("PRIEST_QUEST_COMPLETED"))'
        ) == "true"


# -- stage drivers ------------------------------------------------------


def stage1_kill_undead(tester: BishopQuestTester, verbose=True) -> bool:
    """Stage 1: lay 10 undead to rest."""
    r = tester.interact_bishop()
    if not r.get("success"):
        print(f"✗ interact failed: {r}")
        return False
    time.sleep(0.5)
    state = tester.bishop_state()
    if state.get("questIndex") != 1 or not state.get("questInProgress"):
        print(f"✗ stage 1 not given: {state}")
        return False
    print("✓ stage 1 given (10 undead kills)")

    killed = tester.spawn_and_kill("Zombie", 10)
    if killed != 10:
        print(f"✗ killed {killed}/10 zombies")
        return False
    print("✓ 10 undead laid to rest")

    tester.interact_bishop()
    time.sleep(0.5)
    state = tester.bishop_state()
    if state.get("questIndex") != 2:
        print(f"✗ stage 1 not turned in: {state}")
        return False
    reward = tester.hero_check_item("PotionOfHealing")
    if reward == "none":
        print("✗ PotionOfHealing reward missing")
        return False
    print(f"✓ stage 1 complete, PotionOfHealing x{reward}")
    return True


def stage2_offerings(tester: BishopQuestTester) -> bool:
    """Stage 2: 3 Sungrass or Moongrace seeds (random variant)."""
    tester.interact_bishop()
    time.sleep(0.5)
    state = tester.bishop_state()
    if state.get("questIndex") != 2 or not state.get("questInProgress"):
        print(f"✗ stage 2 not given: {state}")
        return False
    variant = state.get("questVariant")
    seed = "Sungrass.Seed" if variant == 1 else "Moongrace.Seed"
    print(f"✓ stage 2 given (variant {variant}: 3 x {seed})")

    for _ in range(3):
        r = tester.client.give_item(seed)
        if not r.get("success"):
            print(f"✗ give_item({seed}) failed: {r}")
            return False

    tester.interact_bishop()
    time.sleep(0.5)
    state = tester.bishop_state()
    if state.get("questIndex") != 3:
        print(f"✗ stage 2 not turned in: {state}")
        return False
    if tester.hero_check_item("PotionOfMana") == "none":
        print("✗ PotionOfMana reward missing")
        return False
    print("✓ stage 2 complete, PotionOfMana awarded")
    return True


def stage3_pet_delivery(tester: BishopQuestTester) -> bool:
    """Stage 3: deliver a tamed Zombie or Bat."""
    tester.interact_bishop()
    time.sleep(0.5)
    state = tester.bishop_state()
    if state.get("questIndex") != 3 or not state.get("questInProgress"):
        print(f"✗ stage 3 not given: {state}")
        return False
    variant = state.get("questVariant")
    pet = "Zombie" if variant == 1 else "Bat"
    print(f"✓ stage 3 given (variant {variant}: tamed {pet})")

    r = tester.client.create_mob(pet, owned=True)
    if "id" not in r:
        print(f"✗ tamed pet creation failed: {r}")
        return False

    tester.interact_bishop()
    time.sleep(0.5)
    state = tester.bishop_state()
    if state.get("questIndex") != 4:
        print(f"✗ stage 3 not turned in: {state}")
        return False
    if tester.hero_check_item("ScrollOfUpgrade") == "none":
        print("✗ ScrollOfUpgrade reward missing")
        return False
    print("✓ stage 3 complete, ScrollOfUpgrade awarded")
    return True


def stage4_strike_the_source(tester: BishopQuestTester) -> bool:
    """Stage 4: 2 DeathKnight or 4 EnslavedSoul kills (java mobs need a dungeon level)."""
    tester.interact_bishop()
    time.sleep(0.5)
    state = tester.bishop_state()
    if state.get("questIndex") != 4 or not state.get("questInProgress"):
        print(f"✗ stage 4 not given: {state}")
        return False
    variant = state.get("questVariant")
    target, qty = ("DeathKnight", 2) if variant == 1 else ("EnslavedSoul", 4)
    print(f"✓ stage 4 given (variant {variant}: {qty} x {target})")

    tester.go_to("1")
    killed = tester.spawn_and_kill(target, qty)
    tester.go_to("church_2")
    tester.place_hero_next_to_bishop()
    if killed != qty:
        print(f"✗ killed {killed}/{qty} {target}")
        return False
    print(f"✓ {qty} x {target} destroyed")

    tester.interact_bishop()
    time.sleep(0.5)
    state = tester.bishop_state()
    if state.get("questIndex") != 5:
        print(f"✗ stage 4 not turned in: {state}")
        return False
    if tester.hero_check_item("PotionOfMight") == "none":
        print("✗ PotionOfMight reward missing")
        return False
    print("✓ stage 4 complete, PotionOfMight awarded")
    return True


def stage5_banish_demons(tester: BishopQuestTester) -> bool:
    """Stage 5: banish 2 Succubus -> badge + ceremony rewards."""
    gold_before = tester.hero_gold()

    tester.interact_bishop()
    time.sleep(0.5)
    state = tester.bishop_state()
    if state.get("questIndex") != 5 or not state.get("questInProgress"):
        print(f"✗ stage 5 not given: {state}")
        return False
    print("✓ stage 5 given (2 Succubus banishments)")

    tester.go_to("1")
    killed = tester.spawn_and_kill("Succubus", 2)
    tester.go_to("church_2")
    tester.place_hero_next_to_bishop()
    if killed != 2:
        print(f"✗ banished {killed}/2 Succubus")
        return False
    print("✓ 2 Succubus banished")

    tester.interact_bishop()
    time.sleep(1)

    if not tester.priest_badge():
        print("✗ PRIEST_QUEST_COMPLETED badge not awarded")
        return False
    print("✓ PRIEST_QUEST_COMPLETED badge awarded")

    state = tester.bishop_state()
    if state.get("questIndex") != 6:
        print(f"✗ quest line not closed: {state}")
        return False

    gold_after = tester.hero_gold()
    if gold_after - gold_before < 300:
        print(f"✗ ceremony alms missing (gold {gold_before} -> {gold_after})")
        return False
    print(f"✓ ceremony rewards: gold {gold_before} -> {gold_after}")

    hero = tester.client.get_hero_info()
    buffs = [b.get("__className", "") for b in hero.get("buffs", [])]
    if not any("Blessed" in b for b in buffs):
        print(f"✗ ceremony Blessed buff missing (buffs: {buffs})")
        return False
    print("✓ ceremony Blessed buff active")
    return True


def test_post_line(tester: BishopQuestTester) -> bool:
    """After the line the Bishop offers his paid services - no quest errors."""
    r = tester.interact_bishop()
    if not r.get("success"):
        print(f"✗ post-line interact failed: {r}")
        return False
    state = tester.bishop_state()
    if state.get("questIndex") != 6:
        print(f"✗ quest state mutated post-line: {state}")
        return False
    print("✓ post-line services interaction clean")
    return True


def test_persistence(tester: BishopQuestTester) -> bool:
    """Quest state survives save + reload (level transition forces a save)."""
    tester.go_to("1")
    r = tester.client.reload_game()
    if "error" in r:
        print(f"✗ reload_game failed: {r['error']}")
        return False
    time.sleep(3)
    tester.go_to("church_2")
    state = tester.bishop_state()
    if state.get("questIndex") != 6:
        print(f"✗ quest state lost after reload: {state}")
        return False
    if not tester.priest_badge():
        print("✗ badge lost after reload (badges are global, should persist)")
        return False
    print("✓ quest state and badge survive save/reload")
    return True


def test_priest_hero_despawns_bishop(tester: BishopQuestTester) -> bool:
    """A priest hero has no need of the initiation trials (Bishop despawns)."""
    r = tester.client.start_game("PRIEST")
    if not r.get("success"):
        print(f"✗ failed to start priest game: {r}")
        return False
    time.sleep(4)
    tester.go_to("church_2")
    mobs = tester.client.get_mobs().get("mobs", [])
    if any(m.get("type") == "BishopNPC" for m in mobs):
        print("✗ BishopNPC still present for a PRIEST hero")
        return False
    print("✓ BishopNPC removed for PRIEST hero")
    return True


# -- suite --------------------------------------------------------------


def run_all_tests(tester: BishopQuestTester) -> Dict[str, bool]:
    results = {}

    if not tester.client.check_server():
        print("✗ Webserver is not accessible")
        return {"server_connection": False}

    print("✓ Webserver is running")
    results["server_connection"] = True

    if tester.priest_badge():
        print("✗ PRIEST_QUEST_COMPLETED badge already unlocked in this game dir -")
        print("  the quest line would be skipped. Run against a clean game dir")
        print("  (no saves/badges.dat) and retry.")
        return results

    r = tester.client.start_game("WARRIOR")
    if not r.get("success"):
        print(f"✗ failed to start warrior game: {r}")
        return results
    time.sleep(4)
    tester.go_to("church_2")
    tester.place_hero_next_to_bishop()

    results["stage1_kill_undead"] = stage1_kill_undead(tester)
    results["stage2_offerings"] = stage2_offerings(tester)
    results["stage3_pet_delivery"] = stage3_pet_delivery(tester)
    results["stage4_strike_the_source"] = stage4_strike_the_source(tester)
    results["stage5_banish_demons"] = stage5_banish_demons(tester)

    if all(results.values()):
        results["post_line_services"] = test_post_line(tester)
        results["persistence"] = test_persistence(tester)
        results["priest_hero_despawn"] = test_priest_hero_despawns_bishop(tester)

    return results


def main():
    parser = argparse.ArgumentParser(
        description="Test the Bishop quest line (Priest unlock) via WebServer"
    )
    parser.add_argument(
        "--host", default="localhost", help="WebServer host (default: localhost)"
    )
    parser.add_argument(
        "--port", type=int, default=8080, help="WebServer port (default: 8080)"
    )
    parser.add_argument(
        "--no-server",
        action="store_true",
        help="Use existing server instead of starting one",
    )
    args = parser.parse_args()

    print("=" * 60)
    print("BISHOP QUEST LINE TEST SUITE (Priest unlock)")
    print(f"Target: http://{args.host}:{args.port}")
    print("=" * 60)

    tester = BishopQuestTester(args.host, args.port)
    server = None

    if not args.no_server:
        server = ServerManager(args.host, args.port, "priest_quests")
        try:
            if not server.start():
                return 1
            results = run_all_tests(tester)
        finally:
            server.stop()
    else:
        results = run_all_tests(tester)

    print("\n" + "=" * 60)
    print("TEST SUMMARY")
    print("=" * 60)

    passed = sum(1 for v in results.values() if v)
    total = len(results)

    for test_name, success in results.items():
        status = "✓ PASS" if success else "✗ FAIL"
        print(f"  {test_name}: {status}")

    if server:
        server.print_log_summary()

    print(f"\nTotal: {passed}/{total} tests passed")

    return 0 if passed == total else 1


if __name__ == "__main__":
    sys.exit(main())
