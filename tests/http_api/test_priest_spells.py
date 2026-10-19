#!/usr/bin/env python3
"""
Test script for Priest class spells using the WebServer debug endpoints.
Mirrors test_doctor_spells.py (Plague Doctor test set) for the Priest class.

Priest kit (hero/initHeroes.json PRIEST):
    weapon Quarterstaff, magicAffinity Priest, Blessing in quickslot, 12 skill points.
Affinity spells (scripts/spells/SpellsByAffinity.lua):
    Heal, Blessing, HolyWater, Exorcism, LastRites

Usage:
    python3 test_priest_spells.py [--port PORT] [--host HOST] [--no-server]

Prerequisites:
    - Headless jar: java -jar RemixedDungeon-Headless.jar --webserver=PORT --mod=Remixed
    - Or the desktop game with webserver:
      ./gradlew -p RemixedDungeonDesktop runDesktopGameWithWebServer --args="--windowed"
    - Or use --no-server to skip automatic server start

WebServer Endpoints used:
    - /debug/start_game?class=PRIEST - Start a game with Priest class
    - /debug/get_hero_info - Hero details (class/weapon/affinity/buffs)
    - /debug/get_available_spells - List available spells
    - /debug/create_mob?type=X - Create a mob
    - /debug/kill_mob?id=N - Kill mob by id
    - /debug/cast_spell_at_mob?spell=X&mobType=Y - Deterministic cast on a mob
    - /debug/run_lua?code=X - Run lua on the game thread (skill point top-up)
"""

import os
import re
import sys
import time
import argparse
from typing import Dict, Any

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from game_client import GameClient
from test_server import ServerManager


class PriestSpellTester:
    def __init__(self, host: str = "localhost", port: int = 8080):
        self.base_url = f"http://{host}:{port}"
        self.client = GameClient(host, port)

    def start_game(self, hero_class: str = "PRIEST", difficulty: int = 0) -> Dict[str, Any]:
        return self.client.start_game(hero_class, difficulty)

    def get_hero_info(self) -> Dict[str, Any]:
        return self.client.get_hero_info()

    def get_available_spells(self) -> Dict[str, Any]:
        return self.client.get_available_spells()

    def create_mob(self, mob_type: str) -> Dict[str, Any]:
        return self.client.create_mob(mob_type)

    def kill_mob(self, mob_id: int) -> Dict[str, Any]:
        return self.client.kill_mob(mob_id=mob_id)

    def cast_spell_on_mob(self, spell_type: str, mob_type: str) -> Dict[str, Any]:
        return self.client.cast_spell_on_mob(spell_type, mob_type)

    def run_lua(self, code: str) -> str:
        result = self.client.run_lua(code)
        if "error" in result:
            raise RuntimeError(f"run_lua failed: {result['error']}")
        return result.get("result", "")

    def char_status(self, mob_id: int) -> Dict[str, Any]:
        return self.client._get(f"/debug/char_status?id={mob_id}")

    def find_mob(self, mob_type: str) -> Dict[str, Any]:
        mobs = self.client.get_mobs().get("mobs", [])
        for m in mobs:
            if m.get("type") == mob_type:
                return m
        return {}

    def top_up_skill_points(self, amount: int = 60):
        """Spells cost 4+5+7+10 = 26 SP, the priest starts with 12."""
        self.run_lua(
            'local RPD = require "scripts/lib/commonClasses" '
            f"local h = RPD.Dungeon.hero h:setMaxSkillPoints({amount}) "
            f"h:setSkillPoints({amount}) return h:getSkillPoints()"
        )


def _wait_for_game(tester: PriestSpellTester, timeout: int = 20) -> bool:
    start = time.time()
    while time.time() - start < timeout:
        state = tester.client.get_game_state()
        if "hero" in state and "error" not in state:
            time.sleep(3)
            return True
        time.sleep(1)
    return False


def test_server_connection(tester: PriestSpellTester) -> bool:
    print("=" * 60)
    print("TEST: Server Connection")
    print("=" * 60)

    if tester.client.check_server():
        print("✓ Webserver is running and accessible")
        return True
    print("✗ Webserver is not accessible")
    return False


def test_start_priest_game(tester: PriestSpellTester) -> bool:
    print("\n" + "=" * 60)
    print("TEST: Start Priest Game")
    print("=" * 60)

    result = tester.start_game("PRIEST")

    if result.get("success"):
        print(f"✓ Game started successfully")
        print(f"  Hero class: {result.get('heroClass')}")
        return _wait_for_game(tester)
    print(f"✗ Failed to start game: {result.get('error', 'Unknown error')}")
    return False


def test_hero_setup(tester: PriestSpellTester) -> bool:
    print("\n" + "=" * 60)
    print("TEST: Hero Setup")
    print("=" * 60)

    hero = tester.get_hero_info()

    if "error" in hero:
        print(f"✗ Failed to get hero info: {hero['error']}")
        return False

    success = True

    if hero.get("class") == "PRIEST":
        print("✓ Hero class: PRIEST")
    else:
        print(f"✗ Hero class: {hero.get('class')} (expected PRIEST)")
        success = False

    weapon = hero.get("weapon", {})
    if "Quarterstaff" in weapon.get("__className", ""):
        print("✓ Weapon: Quarterstaff equipped")
    else:
        print(f"✗ Weapon: {weapon.get('__className', 'None')} (expected Quarterstaff)")
        success = False

    if hero.get("affinity") == "Priest":
        print("✓ Magic affinity: Priest")
    else:
        print(f"✗ Magic affinity: {hero.get('affinity', 'None')} (expected Priest)")
        success = False

    sp, maxsp = hero.get("sp"), hero.get("maxsp")
    if sp == 12 and maxsp == 12:
        print(f"✓ Skill points: {sp}/{maxsp}")
    else:
        print(f"✗ Skill points: {sp}/{maxsp} (expected 12/12)")
        success = False

    return success


def test_available_spells(tester: PriestSpellTester) -> bool:
    print("\n" + "=" * 60)
    print("TEST: Available Spells")
    print("=" * 60)

    result = tester.get_available_spells()

    if "error" in result:
        print(f"✗ Failed to get spells: {result['error']}")
        return False

    spells = result.get("spells", [])
    print(f"Total spells available: {len(spells)}")

    priest_spells = ["Blessing", "HolyWater", "Exorcism", "LastRites", "Heal"]

    success = True
    for spell in priest_spells:
        if spell in spells:
            print(f"✓ {spell} available")
        else:
            print(f"✗ {spell} NOT available")
            success = False

    return success


def _hero_buffs(tester: PriestSpellTester):
    hero = tester.get_hero_info()
    return [b.get("__className", "") for b in hero.get("buffs", [])]


def test_blessing(tester: PriestSpellTester) -> bool:
    print("\n" + "=" * 60)
    print("TEST: Blessing Spell (self-target, Blessed buff)")
    print("=" * 60)

    tester.top_up_skill_points()

    # self-target spell: the plain cast endpoint routes TARGET_SELF to cast(caster)
    result = tester.client.cast_spell("Blessing")
    time.sleep(1)

    if not result.get("success"):
        print(f"✗ Failed to cast spell: {result.get('message', result.get('error'))}")
        return False
    print("✓ Spell cast executed")

    buffs = _hero_buffs(tester)
    if any("Blessed" in b for b in buffs):
        print("✓ Blessed buff active on hero")
        return True

    print(f"✗ Blessed buff not found on hero (buffs: {buffs})")
    return False


def _damage_a_zombie(tester: PriestSpellTester, spell: str, min_expected: int) -> bool:
    """Spawn a fresh zombie and cast the spell on it - returns True if hp dropped."""
    spawned = tester.create_mob("Zombie")
    if "id" not in spawned:
        print(f"✗ Failed to create Zombie: {spawned.get('error', spawned)}")
        return False

    mob_id = spawned["id"]
    time.sleep(0.5)

    before = tester.char_status(mob_id).get("hp")
    result = tester.cast_spell_on_mob(spell, "Zombie")
    time.sleep(0.5)
    after = tester.char_status(mob_id).get("hp")

    tester.kill_mob(mob_id)

    if not result.get("success"):
        print(f"✗ Failed to cast {spell}: {result.get('message', result.get('error'))}")
        return False
    print(f"✓ Spell cast executed (zombie hp {before} -> {after})")

    if after is not None and before is not None and after < before:
        damage = before - after
        if damage >= min_expected:
            print(f"✓ Damage dealt: {damage} (expected >= {min_expected})")
            return True
        print(f"✗ Damage too low: {damage} (expected >= {min_expected})")
        return False

    print(f"✗ Zombie hp did not decrease ({before} -> {after})")
    return False


def test_holy_water(tester: PriestSpellTester) -> bool:
    print("\n" + "=" * 60)
    print("TEST: HolyWater Spell (undead scald + Slow)")
    print("=" * 60)

    # 4 + 3*skill, skill 1 at start -> at least 5 damage on the undead target
    return _damage_a_zombie(tester, "HolyWater", 4)


def test_exorcism(tester: PriestSpellTester) -> bool:
    print("\n" + "=" * 60)
    print("TEST: Exorcism Spell (holy damage, doubled vs undead)")
    print("=" * 60)

    # (6 + 4*skill) * 2 vs undead, skill 1 -> at least 10 damage
    return _damage_a_zombie(tester, "Exorcism", 10)


def test_last_rites(tester: PriestSpellTester) -> bool:
    print("\n" + "=" * 60)
    print("TEST: LastRites Spell (funeral service AoE)")
    print("=" * 60)

    # 5 + 5*skill damage to every undead within 2 of the cell, skill 1 -> at least 5
    return _damage_a_zombie(tester, "LastRites", 5)


def run_all_tests(tester: PriestSpellTester) -> Dict[str, bool]:
    results = {}

    results["server_connection"] = test_server_connection(tester)
    if not results["server_connection"]:
        print("\nCannot proceed without server connection.")
        return results

    results["start_game"] = test_start_priest_game(tester)
    if not results["start_game"]:
        print("\nCannot proceed without starting game.")
        return results

    results["hero_setup"] = test_hero_setup(tester)
    results["available_spells"] = test_available_spells(tester)
    results["blessing"] = test_blessing(tester)
    results["holy_water"] = test_holy_water(tester)
    results["exorcism"] = test_exorcism(tester)
    results["last_rites"] = test_last_rites(tester)

    return results


def main():
    parser = argparse.ArgumentParser(
        description="Test Priest class spells via WebServer"
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
    print("PRIEST SPELL TEST SUITE")
    print(f"Target: http://{args.host}:{args.port}")
    print("=" * 60)

    tester = PriestSpellTester(args.host, args.port)
    server = None

    if not args.no_server:
        server = ServerManager(args.host, args.port, "priest_spells")
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
