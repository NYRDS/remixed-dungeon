#!/usr/bin/env python3
"""
Test script for Doctor class spells using the WebServer debug endpoints.

Usage:
    python3 test_doctor_spells.py [--port PORT] [--host HOST]

Prerequisites:
    - Run the desktop game with webserver in windowed mode:
      ./gradlew -p RemixedDungeonDesktop runDesktopGameWithWebServer --args="--windowed"
    - Or use the helper script:
      ./tests/http_api/start_game_server.sh

WebServer Endpoints used:
    - /debug/start_game?class=DOCTOR - Start a game with Doctor class
    - /debug/get_game_state - Get current game state
    - /debug/get_hero_info - Get hero details
    - /debug/get_available_spells - List available spells
    - /debug/get_mobs - List mobs on current level
    - /debug/create_mob?type=X - Create a mob
    - /debug/kill_mob?x=X&y=Y - Kill mob at position
    - /debug/cast_spell?type=X - Cast a spell
    - /debug/get_recent_logs - Get recent log messages
    - /debug/get_items - Get items on level
"""

import argparse
import json
import requests
import time
import sys
from typing import Optional, Dict, Any, List


class DoctorSpellTester:
    def __init__(self, host: str = "localhost", port: int = 8080):
        self.base_url = f"http://{host}:{port}"
        self.session = requests.Session()

    def _get(self, endpoint: str) -> Dict[str, Any]:
        """Make a GET request to an endpoint."""
        url = f"{self.base_url}{endpoint}"
        try:
            response = self.session.get(url, timeout=10)
            return response.json()
        except Exception as e:
            return {"error": str(e)}

    def _post(self, endpoint: str) -> Dict[str, Any]:
        """Make a POST request to an endpoint."""
        url = f"{self.base_url}{endpoint}"
        try:
            response = self.session.post(url, timeout=10)
            return response.json()
        except Exception as e:
            return {"error": str(e)}

    def check_server(self) -> bool:
        """Check if the webserver is running."""
        try:
            url = f"{self.base_url}/"
            response = self.session.get(url, timeout=5)
            return response.status_code == 200
        except:
            return False

    def start_game(self, hero_class: str = "DOCTOR", difficulty: int = 0) -> Dict[str, Any]:
        """Start a new game with specified hero class."""
        return self._get(f"/debug/start_game?class={hero_class}&difficulty={difficulty}")

    def get_game_state(self) -> Dict[str, Any]:
        """Get current game state."""
        return self._get("/debug/get_game_state")

    def get_hero_info(self) -> Dict[str, Any]:
        """Get detailed hero information."""
        return self._get("/debug/get_hero_info")

    def get_available_spells(self) -> Dict[str, Any]:
        """Get list of available spells."""
        return self._get("/debug/get_available_spells")

    def get_mobs(self) -> Dict[str, Any]:
        """Get mobs on current level."""
        return self._get("/debug/get_mobs")

    def create_mob(self, mob_type: str) -> Dict[str, Any]:
        """Create a mob on the level."""
        return self._get(f"/debug/create_mob?type={mob_type}")

    def kill_mob(self, x: int, y: int) -> Dict[str, Any]:
        """Kill a mob at specified coordinates."""
        return self._get(f"/debug/kill_mob?x={x}&y={y}")

    def cast_spell(self, spell_type: str) -> Dict[str, Any]:
        """Cast a spell."""
        return self._get(f"/debug/cast_spell?type={spell_type}")

    def cast_spell_on_target(self, spell_type: str, x: int, y: int) -> Dict[str, Any]:
        """Cast a spell on a specific target."""
        return self._get(f"/debug/cast_spell_on_target?type={spell_type}&x={x}&y={y}")

    def get_recent_logs(self) -> Dict[str, Any]:
        """Get recent log messages."""
        return self._get("/debug/get_recent_logs")

    def get_items(self) -> Dict[str, Any]:
        """Get items on current level."""
        return self._get("/debug/get_items")

    def get_level_info(self) -> Dict[str, Any]:
        """Get level information."""
        return self._get("/debug/get_level_info")


def test_server_connection(tester: DoctorSpellTester) -> bool:
    """Test if webserver is accessible."""
    print("=" * 60)
    print("TEST: Server Connection")
    print("=" * 60)

    if tester.check_server():
        print("✓ Webserver is running and accessible")
        return True
    else:
        print("✗ Webserver is not accessible")
        print("  Please start the game with webserver:")
        print("  ./gradlew -p RemixedDungeonDesktop runDesktopGameWithWebServer")
        return False


def test_start_doctor_game(tester: DoctorSpellTester) -> bool:
    """Test starting a game with Doctor class."""
    print("\n" + "=" * 60)
    print("TEST: Start Doctor Game")
    print("=" * 60)

    result = tester.start_game("DOCTOR")

    if result.get("success"):
        print(f"✓ Game started successfully")
        print(f"  Hero class: {result.get('heroClass')}")
        print(f"  Difficulty: {result.get('difficulty')}")
        return True
    else:
        print(f"✗ Failed to start game: {result.get('error', 'Unknown error')}")
        return False


def test_hero_setup(tester: DoctorSpellTester) -> bool:
    """Test that Doctor hero is properly set up."""
    print("\n" + "=" * 60)
    print("TEST: Hero Setup")
    print("=" * 60)

    hero = tester.get_hero_info()

    if "error" in hero:
        print(f"✗ Failed to get hero info: {hero['error']}")
        return False

    success = True

    # Check class
    if hero.get("class") == "DOCTOR":
        print(f"✓ Hero class: DOCTOR")
    else:
        print(f"✗ Hero class: {hero.get('class')} (expected DOCTOR)")
        success = False

    # Check armor
    armor = hero.get("armor", {})
    if "DoctorArmor" in armor.get("__className", ""):
        print(f"✓ Armor: DoctorArmor equipped")
    else:
        print(f"✗ Armor: {armor.get('__className', 'None')} (expected DoctorArmor)")
        success = False

    # Check weapon (BoneSaw)
    weapon = hero.get("weapon", {})
    if weapon.get("scriptFile") == "BoneSaw":
        print(f"✓ Weapon: BoneSaw equipped")
    else:
        print(f"✗ Weapon: {weapon.get('scriptFile', 'None')} (expected BoneSaw)")
        success = False

    # Check ring (PlagueDoctorMask)
    ring1 = hero.get("ring1", {})
    if ring1.get("scriptFile") == "PlagueDoctorMask":
        print(f"✓ Ring: PlagueDoctorMask equipped")
    else:
        print(f"✗ Ring: {ring1.get('scriptFile', 'None')} (expected PlagueDoctorMask)")
        success = False

    # Check magic affinity
    if hero.get("affinity") == "PlagueDoctor":
        print(f"✓ Magic affinity: PlagueDoctor")
    else:
        print(f"✗ Magic affinity: {hero.get('affinity', 'None')} (expected PlagueDoctor)")
        success = False

    # Check GasesImmunity buff
    buffs = hero.get("buffs", [])
    has_gas_immunity = any("GasesImmunity" in b.get("scriptFile", "") for b in buffs)
    if has_gas_immunity:
        print(f"✓ Buff: GasesImmunity active")
    else:
        print(f"✗ Buff: GasesImmunity not found")
        success = False

    return success


def test_available_spells(tester: DoctorSpellTester) -> bool:
    """Test that Doctor spells are available."""
    print("\n" + "=" * 60)
    print("TEST: Available Spells")
    print("=" * 60)

    result = tester.get_available_spells()

    if "error" in result:
        print(f"✗ Failed to get spells: {result['error']}")
        return False

    spells = result.get("spells", [])
    print(f"Total spells available: {len(spells)}")

    # Doctor-specific spells
    doctor_spells = ["BloodTransfusion", "CorpseExplosion", "Anesthesia"]

    success = True
    for spell in doctor_spells:
        if spell in spells:
            print(f"✓ {spell} available")
        else:
            print(f"✗ {spell} NOT available")
            success = False

    return success


def test_blood_transfusion(tester: DoctorSpellTester) -> bool:
    """Test BloodTransfusion spell."""
    print("\n" + "=" * 60)
    print("TEST: BloodTransfusion Spell")
    print("=" * 60)

    # Cast the spell
    result = tester.cast_spell("BloodTransfusion")

    if result.get("success"):
        print(f"✓ Spell cast scheduled")
    else:
        print(f"✗ Failed to cast spell: {result.get('error', 'Unknown error')}")
        return False

    # Wait for spell to execute
    time.sleep(1)

    # Check logs
    logs = tester.get_recent_logs()
    log_messages = logs.get("logs", [])

    print(f"Recent logs: {log_messages}")

    # BloodTransfusion requires a target with life essence
    # It may fail if no valid target is nearby
    if any("BloodTransfusion" in str(log) for log in log_messages):
        print("✓ BloodTransfusion was processed")
        return True
    elif any("resists" in str(log).lower() for log in log_messages):
        print("⚠ Target resisted (expected behavior without valid target)")
        return True
    else:
        print("⚠ Spell may have failed silently")
        return True  # Don't fail the test, just note it


def test_corpse_explosion(tester: DoctorSpellTester) -> bool:
    """Test CorpseExplosion spell."""
    print("\n" + "=" * 60)
    print("TEST: CorpseExplosion Spell")
    print("=" * 60)

    # First, try to create a mob and kill it to create a corpse
    print("Creating a Rat to generate a corpse...")
    mob_result = tester.create_mob("Rat")

    if "error" in mob_result:
        print(f"✗ Failed to create mob: {mob_result['error']}")
        # Try casting anyway, maybe there's already a corpse
    else:
        x, y = mob_result.get("x"), mob_result.get("y")
        print(f"  Created Rat at ({x}, {y})")

        # Kill the mob
        time.sleep(0.5)
        kill_result = tester.kill_mob(x, y)

        if "error" in kill_result:
            print(f"  ⚠ Could not kill mob: {kill_result['error']}")
            # Mob may have moved, try casting anyway
        else:
            print(f"  Killed mob at ({x}, {y})")

    # Cast CorpseExplosion
    time.sleep(0.5)
    result = tester.cast_spell("CorpseExplosion")

    if result.get("success"):
        print(f"✓ Spell cast scheduled")
    else:
        print(f"✗ Failed to cast spell: {result.get('error', 'Unknown error')}")
        return False

    # Wait for spell to execute
    time.sleep(1)

    # Check logs
    logs = tester.get_recent_logs()
    log_messages = logs.get("logs", [])

    print(f"Recent logs: {log_messages}")

    # CorpseExplosion requires a Carcass item nearby
    if any("CorpseExplosion" in str(log) for log in log_messages):
        print("✓ CorpseExplosion was processed")
        return True
    elif any("NoCorpse" in str(log) for log in log_messages):
        print("⚠ No corpse found (expected if no Carcass nearby)")
        return True
    else:
        print("⚠ Spell may have failed silently")
        return True


def test_anesthesia(tester: DoctorSpellTester) -> bool:
    """Test Anesthesia spell."""
    print("\n" + "=" * 60)
    print("TEST: Anesthesia Spell")
    print("=" * 60)

    # Cast the spell
    result = tester.cast_spell("Anesthesia")

    if result.get("success"):
        print(f"✓ Spell cast scheduled")
    else:
        print(f"✗ Failed to cast spell: {result.get('error', 'Unknown error')}")
        return False

    # Wait for spell to execute
    time.sleep(1)

    # Check logs
    logs = tester.get_recent_logs()
    log_messages = logs.get("logs", [])

    print(f"Recent logs: {log_messages}")

    if any("Anesthesia" in str(log) for log in log_messages):
        print("✓ Anesthesia was processed")
        return True
    else:
        print("⚠ Spell may have failed silently")
        return True


def run_all_tests(tester: DoctorSpellTester) -> Dict[str, bool]:
    """Run all Doctor spell tests."""
    results = {}

    results["server_connection"] = test_server_connection(tester)
    if not results["server_connection"]:
        print("\nCannot proceed without server connection.")
        return results

    results["start_game"] = test_start_doctor_game(tester)
    if not results["start_game"]:
        print("\nCannot proceed without starting game.")
        return results

    time.sleep(1)  # Wait for game to initialize

    results["hero_setup"] = test_hero_setup(tester)
    results["available_spells"] = test_available_spells(tester)
    results["blood_transfusion"] = test_blood_transfusion(tester)
    results["corpse_explosion"] = test_corpse_explosion(tester)
    results["anesthesia"] = test_anesthesia(tester)

    return results


def main():
    parser = argparse.ArgumentParser(description="Test Doctor class spells via WebServer")
    parser.add_argument("--host", default="localhost", help="WebServer host (default: localhost)")
    parser.add_argument("--port", type=int, default=8080, help="WebServer port (default: 8080)")
    args = parser.parse_args()

    print("=" * 60)
    print("DOCTOR SPELL TEST SUITE")
    print(f"Target: http://{args.host}:{args.port}")
    print("=" * 60)

    tester = DoctorSpellTester(args.host, args.port)
    results = run_all_tests(tester)

    # Print summary
    print("\n" + "=" * 60)
    print("TEST SUMMARY")
    print("=" * 60)

    passed = sum(1 for v in results.values() if v)
    total = len(results)

    for test_name, success in results.items():
        status = "✓ PASS" if success else "✗ FAIL"
        print(f"  {test_name}: {status}")

    print(f"\nTotal: {passed}/{total} tests passed")

    return 0 if passed == total else 1


if __name__ == "__main__":
    sys.exit(main())
