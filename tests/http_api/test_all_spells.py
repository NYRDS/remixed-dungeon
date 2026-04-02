#!/usr/bin/env python3
"""
Comprehensive test script for all spells of all hero classes.

This script tests every spell available to each hero class via the WebServer debug API.

Usage:
    python3 test_all_spells.py [--port PORT] [--host HOST] [--class CLASS] [--spell SPELL]

Examples:
    python3 test_all_spells.py                          # Test all spells for all classes
    python3 test_all_spells.py --class DOCTOR           # Test only Doctor spells
    python3 test_all_spells.py --spell BloodTransfusion # Test specific spell
    python3 test_all_spells.py --port 8082              # Use different port

Prerequisites:
    - Run the desktop game with webserver:
      ./gradlew -p RemixedDungeonDesktop runDesktopGameWithWebServer
    - Or use the helper script:
      ./tests/http_api/start_game_server.sh
"""

import argparse
import json
import requests
import time
import sys
from typing import Optional, Dict, Any, List, Tuple
from dataclasses import dataclass
from enum import Enum


# Hero classes and their spell affinities (from CustomSpellsList.lua)
HERO_CLASS_SPELLS = {
    "WARRIOR": ["Combat"],
    "MAGE": ["Witchcraft"],
    "ROGUE": ["Rogue"],
    "HUNTRESS": ["Huntress"],
    "ELF": ["Elf"],
    "NECROMANCER": ["Necromancy"],
    "GNOLL": ["Common"],  # Gnoll uses common spells
    "PRIEST": ["Priest"],
    "DOCTOR": ["PlagueDoctor"],
}

# Spell lists by affinity (from CustomSpellsList.lua)
SPELLS_BY_AFFINITY = {
    "Necromancy": ["RaiseDead", "Exhumation", "DarkSacrifice", "Possess"],
    "Common": ["TownPortal", "Heal", "RaiseDead", "Cloak", "Calm", "Charm"],
    "Combat": ["DieHard", "Dash", "BodyArmor", "Smash"],
    "Rogue": ["Cloak", "Backstab", "KunaiThrow", "Haste"],
    "Witchcraft": ["Roar", "LightningBolt", "Heal", "Order"],
    "Huntress": ["Calm", "Charm", "ShootInEye", "SummonBeast"],
    "Elf": ["MagicArrow", "Sprout", "HideInGrass", "NatureArmor"],
    "Priest": ["Heal", "Calm", "Charm", "Order"],
    "PlagueDoctor": ["Anesthesia", "Heal", "BloodTransfusion", "CorpseExplosion"],
    "Dev": ["TestSpell", "CurseItem"],
}

# All hero classes
ALL_HERO_CLASSES = [
    "WARRIOR", "MAGE", "ROGUE", "HUNTRESS", "ELF",
    "NECROMANCER", "GNOLL", "PRIEST", "DOCTOR"
]


@dataclass
class TestResult:
    """Result of a single spell test."""
    hero_class: str
    spell: str
    success: bool
    message: str
    duration: float


class AllSpellsTester:
    """Client for testing all spells across all hero classes."""

    def __init__(self, host: str = "localhost", port: int = 8080, verbose: bool = False):
        self.base_url = f"http://{host}:{port}"
        self.session = requests.Session()
        self.verbose = verbose
        self.results: List[TestResult] = []

    def _get(self, endpoint: str, timeout: int = 10) -> Dict[str, Any]:
        """Make a GET request to an endpoint."""
        url = f"{self.base_url}{endpoint}"
        try:
            response = self.session.get(url, timeout=timeout)
            return response.json()
        except requests.exceptions.Timeout:
            return {"error": "Request timeout"}
        except Exception as e:
            return {"error": str(e)}

    def check_server(self) -> bool:
        """Check if the webserver is running and ready."""
        try:
            response = self.session.get(f"{self.base_url}/ready", timeout=5)
            if response.status_code == 200:
                data = response.json()
                return data.get("ready", False)
            return False
        except:
            return False

    def start_game(self, hero_class: str, difficulty: int = 0) -> Dict[str, Any]:
        """Start a new game with specified hero class."""
        return self._get(f"/debug/start_game?class={hero_class}&difficulty={difficulty}")

    def get_hero_info(self) -> Dict[str, Any]:
        """Get detailed hero information."""
        return self._get("/debug/get_hero_info")

    def get_available_spells(self) -> Dict[str, Any]:
        """Get list of available spells."""
        return self._get("/debug/get_available_spells")

    def cast_spell(self, spell_type: str) -> Dict[str, Any]:
        """Cast a spell."""
        return self._get(f"/debug/cast_spell?type={spell_type}")

    def cast_spell_on_target(self, spell_type: str, x: int, y: int) -> Dict[str, Any]:
        """Cast a spell on a specific target."""
        return self._get(f"/debug/cast_spell_on_target?type={spell_type}&x={x}&y={y}")

    def get_mobs(self) -> Dict[str, Any]:
        """Get mobs on current level."""
        return self._get("/debug/get_mobs")

    def create_mob(self, mob_type: str, owned: bool = False) -> Dict[str, Any]:
        """Create a mob on the level."""
        endpoint = f"/debug/create_mob?type={mob_type}"
        if owned:
            endpoint += "&owned=true"
        return self._get(endpoint)

    def kill_mob(self, x: int, y: int) -> Dict[str, Any]:
        """Kill a mob at specified coordinates."""
        return self._get(f"/debug/kill_mob?x={x}&y={y}")

    def get_recent_logs(self) -> Dict[str, Any]:
        """Get recent log messages."""
        return self._get("/debug/get_recent_logs")

    def get_level_info(self) -> Dict[str, Any]:
        """Get level information."""
        return self._get("/debug/get_level_info")

    def get_hero_position(self) -> Dict[str, Any]:
        """Get hero position."""
        return self._get("/debug/get_hero_position")

    def move_hero(self, x: int, y: int) -> Dict[str, Any]:
        """Move hero to coordinates."""
        return self._get(f"/debug/move_hero?x={x}&y={y}")

    def wait_ticks(self, ticks: int = 10) -> Dict[str, Any]:
        """Wait N game ticks."""
        return self._get(f"/debug/wait_ticks?ticks={ticks}")

    def get_spells_for_class(self, hero_class: str) -> List[str]:
        """Get list of spells for a specific hero class."""
        affinities = HERO_CLASS_SPELLS.get(hero_class, ["Common"])
        spells = []
        for affinity in affinities:
            spells.extend(SPELLS_BY_AFFINITY.get(affinity, []))
        return list(set(spells))  # Remove duplicates

    def test_spell(self, hero_class: str, spell: str) -> TestResult:
        """Test casting a specific spell."""
        start_time = time.time()

        # Cast the spell
        result = self.cast_spell(spell)
        duration = time.time() - start_time

        # Wait for spell to execute
        time.sleep(0.5)

        # Check result
        if result.get("success"):
            # Check logs for spell execution
            logs = self.get_recent_logs()
            log_messages = logs.get("logs", [])

            # Look for spell-related messages
            spell_logged = any(spell in str(log) for log in log_messages)

            if spell_logged:
                return TestResult(
                    hero_class=hero_class,
                    spell=spell,
                    success=True,
                    message="Spell cast successfully",
                    duration=duration
                )
            else:
                # Spell succeeded but no log message (may be normal for some spells)
                return TestResult(
                    hero_class=hero_class,
                    spell=spell,
                    success=True,
                    message="Spell cast (no log confirmation)",
                    duration=duration
                )
        else:
            error = result.get("error", "Unknown error")
            error_msg = result.get("message", "")

            # Some "failures" are expected behavior
            expected_failures = [
                "NoCorpse",  # CorpseExplosion without corpse
                "NotEnoughSP",  # Not enough skill points
                "NotTooFast",  # Cooldown not elapsed
                "NotInOwnBody",  # Control issue
                "resists",  # Target resisted
                "NoValidTarget",  # No valid target
                "OutOfRange",  # Target out of range
            ]

            is_expected = any(exp in error or exp in error_msg for exp in expected_failures)

            if is_expected:
                return TestResult(
                    hero_class=hero_class,
                    spell=spell,
                    success=True,
                    message=f"Expected behavior: {error_msg or error}",
                    duration=duration
                )
            else:
                return TestResult(
                    hero_class=hero_class,
                    spell=spell,
                    success=False,
                    message=f"Error: {error_msg or error}",
                    duration=duration
                )

    def test_class_spells(self, hero_class: str, specific_spell: str = None) -> List[TestResult]:
        """Test all spells for a specific hero class."""
        results = []

        print(f"\n{'='*70}")
        print(f"Testing {hero_class} spells")
        print(f"{'='*70}")

        # Start game with this hero class
        print(f"Starting game with {hero_class}...")
        start_result = self.start_game(hero_class)

        if not start_result.get("success"):
            print(f"  ✗ Failed to start game: {start_result.get('error', 'Unknown error')}")
            return results

        # Wait for game to initialize and verify hero class
        max_retries = 10
        hero_class_verified = False
        for retry in range(max_retries):
            time.sleep(0.5)
            hero_info = self.get_hero_info()
            if "error" not in hero_info:
                current_class = hero_info.get('class', 'Unknown')
                if current_class == hero_class:
                    hero_class_verified = True
                    print(f"  Hero class: {current_class} ✓")
                    print(f"  Magic affinity: {hero_info.get('affinity', 'None')}")
                    break
                elif retry == max_retries - 1:
                    print(f"  ⚠ Hero class mismatch: expected {hero_class}, got {current_class}")
                # Continue waiting if class doesn't match yet

        # Get available spells
        available = self.get_available_spells()
        spell_count = len(available.get("spells", []))
        print(f"  Available spells: {spell_count}")

        # Get spells to test
        spells_to_test = self.get_spells_for_class(hero_class)

        if specific_spell:
            if specific_spell in spells_to_test:
                spells_to_test = [specific_spell]
                print(f"  Testing specific spell: {specific_spell}")
            else:
                print(f"  Warning: {specific_spell} not in {hero_class} spell list")
                return results

        print(f"  Spells to test: {', '.join(spells_to_test)}")
        time.sleep(0.5)

        # Test each spell
        for spell in spells_to_test:
            print(f"\n  Testing {spell}...", end=" ")
            result = self.test_spell(hero_class, spell)
            results.append(result)

            status = "✓" if result.success else "✗"
            print(f"{status} {result.message} ({result.duration:.2f}s)")

            # Small delay between spells
            time.sleep(0.3)

        return results

    def run_all_tests(self, specific_class: str = None, specific_spell: str = None) -> List[TestResult]:
        """Run tests for all classes or a specific class/spell."""
        all_results = []

        if specific_spell:
            # Find which class has this spell and test only that
            for hero_class in ALL_HERO_CLASSES:
                class_spells = self.get_spells_for_class(hero_class)
                if specific_spell in class_spells:
                    results = self.test_class_spells(hero_class, specific_spell)
                    all_results.extend(results)
                    break
            else:
                print(f"Spell '{specific_spell}' not found in any class spell list")
        elif specific_class:
            if specific_class in ALL_HERO_CLASSES:
                all_results = self.test_class_spells(specific_class)
            else:
                print(f"Unknown hero class: {specific_class}")
                print(f"Valid classes: {', '.join(ALL_HERO_CLASSES)}")
        else:
            # Test all classes
            for hero_class in ALL_HERO_CLASSES:
                results = self.test_class_spells(hero_class)
                all_results.extend(results)

        self.results = all_results
        return all_results

    def print_summary(self):
        """Print test summary."""
        if not self.results:
            print("\nNo tests were run.")
            return

        print("\n" + "="*70)
        print("TEST SUMMARY")
        print("="*70)

        # Overall stats
        total = len(self.results)
        passed = sum(1 for r in self.results if r.success)
        failed = total - passed

        print(f"\nTotal: {passed}/{total} tests passed ({passed/total*100:.1f}%)")
        print(f"Failed: {failed}/{total}")

        # Group by class
        print("\nResults by Hero Class:")
        print("-"*70)

        by_class = {}
        for result in self.results:
            if result.hero_class not in by_class:
                by_class[result.hero_class] = {"passed": 0, "total": 0, "spells": []}
            by_class[result.hero_class]["total"] += 1
            if result.success:
                by_class[result.hero_class]["passed"] += 1
            else:
                by_class[result.hero_class]["spells"].append((result.spell, result.message))

        for hero_class in ALL_HERO_CLASSES:
            if hero_class in by_class:
                data = by_class[hero_class]
                pct = data["passed"] / data["total"] * 100
                print(f"  {hero_class:15} {data['passed']:2}/{data['total']:2} ({pct:5.1f}%)")

                # Show failed spells
                for spell, msg in data["spells"]:
                    print(f"    ✗ {spell}: {msg}")

        # Group by spell
        print("\nResults by Spell:")
        print("-"*70)

        by_spell = {}
        for result in self.results:
            if result.spell not in by_spell:
                by_spell[result.spell] = {"passed": 0, "total": 0, "classes": []}
            by_spell[result.spell]["total"] += 1
            if result.success:
                by_spell[result.spell]["passed"] += 1
            else:
                by_spell[result.spell]["classes"].append((result.hero_class, result.message))

        for spell in sorted(by_spell.keys()):
            data = by_spell[spell]
            pct = data["passed"] / data["total"] * 100
            status = "✓" if data["passed"] == data["total"] else "⚠"
            print(f"  {status} {spell:25} {data['passed']:2}/{data['total']:2} ({pct:5.1f}%)")

        # Show completely failed spells
        failed_spells = [(s, d) for s, d in by_spell.items() if d["passed"] == 0]
        if failed_spells:
            print("\nCompletely Failed Spells:")
            for spell, data in failed_spells:
                print(f"  ✗ {spell}")
                for hero_class, msg in data["classes"]:
                    print(f"    - {hero_class}: {msg}")


def main():
    parser = argparse.ArgumentParser(
        description="Test all spells for all hero classes via WebServer",
        formatter_class=argparse.RawDescriptionHelpFormatter,
        epilog="""
Examples:
  %(prog)s                          Test all spells for all classes
  %(prog)s --class DOCTOR           Test only Doctor spells
  %(prog)s --spell BloodTransfusion Test specific spell across classes
  %(prog)s --port 8082              Use different port
        """
    )
    parser.add_argument("--host", default="localhost", help="WebServer host (default: localhost)")
    parser.add_argument("--port", type=int, default=8080, help="WebServer port (default: 8080)")
    parser.add_argument("--class", dest="hero_class", choices=ALL_HERO_CLASSES,
                        help="Test specific hero class only")
    parser.add_argument("--spell", dest="specific_spell",
                        help="Test specific spell only")
    parser.add_argument("--verbose", "-v", action="store_true",
                        help="Verbose output")
    parser.add_argument("--json", action="store_true",
                        help="Output results as JSON")

    args = parser.parse_args()

    print("="*70)
    print("ALL SPELLS TEST SUITE")
    print(f"Target: http://{args.host}:{args.port}")
    print("="*70)

    if args.hero_class:
        print(f"Testing class: {args.hero_class}")
    if args.specific_spell:
        print(f"Testing spell: {args.specific_spell}")

    tester = AllSpellsTester(args.host, args.port, args.verbose)

    # Check server connection
    print("\nChecking server connection...")
    if not tester.check_server():
        print("✗ Webserver is not accessible")
        print("\nPlease start the game with webserver:")
        print("  ./gradlew -p RemixedDungeonDesktop runDesktopGameWithWebServer")
        print("\nOr use the helper script:")
        print("  ./tests/http_api/start_game_server.sh")
        return 1

    print("✓ Webserver is running and ready")

    # Run tests
    tester.run_all_tests(args.hero_class, args.specific_spell)

    # Print summary
    if args.json:
        results_dict = [
            {
                "hero_class": r.hero_class,
                "spell": r.spell,
                "success": r.success,
                "message": r.message,
                "duration": r.duration
            }
            for r in tester.results
        ]
        print(json.dumps(results_dict, indent=2))
    else:
        tester.print_summary()

    # Return exit code
    failed = sum(1 for r in tester.results if not r.success)
    return 0 if failed == 0 else 1


if __name__ == "__main__":
    sys.exit(main())
