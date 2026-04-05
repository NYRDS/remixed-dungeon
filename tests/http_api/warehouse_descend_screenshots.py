#!/usr/bin/env python3
"""
Generate warehouse screenshots by descending to dungeon levels from town.

Usage:
    python3 warehouse_descend_screenshots.py [--count N] [--output-dir PATH]
"""

import argparse
import os
import sys
import time
from pathlib import Path
from game_client import GameClient
from test_server import ServerManager


def main():
    parser = argparse.ArgumentParser(
        description="Generate warehouse screenshots by descending"
    )
    parser.add_argument(
        "--count", type=int, default=10, help="Number of screenshots to generate"
    )
    parser.add_argument(
        "--output-dir", default="/tmp/warehouse_descend", help="Output directory"
    )
    parser.add_argument("--port", type=int, default=8080, help="WebServer port")
    parser.add_argument("--host", default="localhost", help="WebServer host")
    args = parser.parse_args()

    output_dir = Path(args.output_dir)
    output_dir.mkdir(parents=True, exist_ok=True)

    client = GameClient(args.host, args.port)
    server = ServerManager(args.host, args.port, "warehouse_descend")

    if not server.start():
        print("✗ Failed to start server")
        return False

    try:
        print("Starting game and descending to dungeon levels...")
        print(f"Screenshots will be saved to: {output_dir}")

        # Start game
        print("\n[Start] Starting game as WARRIOR...")
        response = client.start_game("WARRIOR", 0)
        if not response.get("success", False):
            print(f"✗ Failed to start game: {response.get('error', 'Unknown error')}")
            return False
        print("✓ Game started (likely in town)")

        # Wait for town to render
        time.sleep(3)

        # Take screenshot of town
        screenshot_path = output_dir / "town.png"
        print(f"\n[Info] Taking screenshot of starting area...")
        success = client.take_screenshot(str(screenshot_path))
        if success:
            file_size = os.path.getsize(screenshot_path)
            print(f"  ✓ Saved: town.png ({file_size:,} bytes)")

        # Get exits to find dungeon entrances
        print("\n[Info] Getting exits from current location...")
        exits_response = client.get_exits()
        if not exits_response.get("success", False):
            print(f"  ⚠ Could not get exits: {exits_response.get('error', 'Unknown')}")
            print("  Trying to navigate by level ID...")

            # Try direct level navigation - these are the actual level IDs from Dungeon.json
            dungeon_levels = [
                "1",  # SewerLevel depth 1
                "2",  # SewerLevel depth 2
                "3",  # SewerLevel depth 3
                "4",  # SewerLevel depth 4
                "6",  # PrisonLevel depth 6
                "7",  # PrisonLevel depth 7
                "8",  # PrisonLevel depth 8
                "9",  # PrisonLevel depth 9
                "11",  # CavesLevel depth 11
                "12",  # CavesLevel depth 12
                "16",  # CityLevel depth 16
                "17",  # CityLevel depth 17
                "22",  # HallsLevel depth 22
                "23",  # HallsLevel depth 23
                "6s",  # SpiderLevel depth 6
                "7s",  # SpiderLevel depth 7
                "8s",  # SpiderLevel depth 8
                "9s",  # SpiderLevel depth 9
            ]

            screenshot_count = 0
            for level_id in dungeon_levels:
                if screenshot_count >= args.count:
                    break

                screenshot_path = output_dir / f"{level_id}.png"

                print(f"\n[{screenshot_count + 1}/{args.count}] Going to {level_id}...")

                response = client.go_to_level(level_id)
                if not response.get("success", False):
                    print(
                        f"  ⚠ Could not go to {level_id}: {response.get('error', 'Unknown')}"
                    )
                    continue

                print(f"  ✓ Arrived at {level_id}")

                # Wait for level to render
                time.sleep(4)

                # Take screenshot
                print(f"  Taking screenshot...")
                success = client.take_screenshot(str(screenshot_path))

                if success:
                    file_size = os.path.getsize(screenshot_path)
                    screenshot_count += 1
                    print(f"  ✓ Saved: {level_id}.png ({file_size:,} bytes)")
                else:
                    print(f"  ✗ Failed to take screenshot")

                # Small delay between levels
                time.sleep(1)

        else:
            exits = exits_response.get("exits", [])
            print(f"  Found {len(exits)} exits")
            for i, exit in enumerate(exits):
                print(
                    f"    {i + 1}. {exit.get('id', 'unknown')} - {exit.get('name', 'unknown')}"
                )

            screenshot_count = 0
            for exit in exits:
                if screenshot_count >= args.count:
                    break

                exit_id = exit.get("id", "")
                screenshot_path = output_dir / f"level_{screenshot_count + 1}.png"

                print(
                    f"\n[{screenshot_count + 1}/{args.count}] Descending to {exit_id}..."
                )

                response = client.descend_to(exit_id)
                if not response.get("success", False):
                    print(f"  ⚠ Could not descend: {response.get('error', 'Unknown')}")
                    continue

                print(f"  ✓ Arrived at {exit_id}")

                # Wait for level to render
                time.sleep(4)

                # Take screenshot
                print(f"  Taking screenshot...")
                success = client.take_screenshot(str(screenshot_path))

                if success:
                    file_size = os.path.getsize(screenshot_path)
                    screenshot_count += 1
                    print(
                        f"  ✓ Saved: level_{screenshot_count}.png ({file_size:,} bytes)"
                    )
                else:
                    print(f"  ✗ Failed to take screenshot")

                # Small delay between levels
                time.sleep(1)

        print(f"\n✓ Generated screenshots in {output_dir}")
        print(
            "  Review screenshots for warehouse rooms (look for barrels and pedestals)"
        )
        return True

    finally:
        server.stop()


if __name__ == "__main__":
    success = main()
    sys.exit(0 if success else 1)
