#!/usr/bin/env python3
"""
Generate warehouse screenshots with revealed map and many levels.

Usage:
    python3 warehouse_reveal_screenshots.py [--count N] [--output-dir PATH]
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
        description="Generate warehouse screenshots with revealed map"
    )
    parser.add_argument("--count", type=int, default=50, help="Number of levels to try")
    parser.add_argument(
        "--output-dir", default="/tmp/warehouse_reveal", help="Output directory"
    )
    parser.add_argument("--port", type=int, default=8080, help="WebServer port")
    parser.add_argument("--host", default="localhost", help="WebServer host")
    args = parser.parse_args()

    output_dir = Path(args.output_dir)
    output_dir.mkdir(parents=True, exist_ok=True)

    client = GameClient(args.host, args.port)
    server = ServerManager(args.host, args.port, "warehouse_reveal")

    if not server.start():
        print("✗ Failed to start server")
        return False

    try:
        print("Generating many levels with revealed map to find warehouse rooms...")
        print(f"Screenshots will be saved to: {output_dir}")

        # Start game
        print("\n[Start] Starting game as WARRIOR...")
        response = client.start_game("WARRIOR", 0)
        if not response.get("success", False):
            print(f"✗ Failed to start game: {response.get('error', 'Unknown error')}")
            return False
        print("✓ Game started")

        # Level IDs to try (more chances to find warehouse)
        level_ids = [
            # Sewer levels
            "1",
            "2",
            "3",
            "4",
            # Prison levels
            "6",
            "7",
            "8",
            "9",
            # Caves levels
            "11",
            "12",
            "13",
            "14",
            # City levels
            "16",
            "17",
            "18",
            "19",
            # Halls levels
            "22",
            "23",
            "24",
            # Spider levels
            "6s",
            "7s",
            "8s",
            "9s",
            # Necro levels
            "necro1",
            "necro2",
            "necro3",
            "necro4",
            # Ice levels
            "ice1",
            "ice2",
            "ice3",
            "ice4",
        ]

        screenshots_taken = 0

        for level_id in level_ids:
            if screenshots_taken >= args.count:
                break

            print(
                f"\n[{screenshots_taken + 1}/{args.count}] Exploring level {level_id}..."
            )

            # Go to level
            response = client.go_to_level(level_id)
            if not response.get("success", False):
                print(
                    f"  ⚠ Could not go to {level_id}: {response.get('error', 'Unknown')}"
                )
                continue

            print(f"  ✓ Arrived at {level_id}")

            # Wait for level to load
            time.sleep(2)

            # Try to reveal map by moving hero around
            level_info = client.get_level_info()
            if "width" in level_info and "height" in level_info:
                width = level_info["width"]
                height = level_info["height"]
                print(f"  Level size: {width}x{height}")

                # Move hero to several positions to reveal more of the map
                positions = [
                    (width // 4, height // 4),
                    (width // 2, height // 2),
                    (3 * width // 4, 3 * height // 4),
                    (width // 2, height // 4),
                    (width // 4, height // 2),
                ]

                for i, (x, y) in enumerate(positions):
                    if screenshots_taken >= args.count:
                        break

                    print(f"  Moving to position {i + 1}/5: ({x}, {y})...")
                    client.move_hero(x, y)
                    time.sleep(1)

                    # Take screenshot at this position
                    screenshot_path = output_dir / f"{level_id}_pos{i + 1}.png"
                    print(f"  Taking screenshot...")
                    success = client.take_screenshot(str(screenshot_path))

                    if success:
                        file_size = os.path.getsize(screenshot_path)
                        screenshots_taken += 1
                        print(
                            f"  ✓ Saved: {level_id}_pos{i + 1}.png ({file_size:,} bytes)"
                        )
                    else:
                        print(f"  ✗ Failed to take screenshot")

                    # Small delay
                    time.sleep(0.5)

            else:
                # Just take one screenshot if we can't get level info
                screenshot_path = output_dir / f"{level_id}.png"
                print(f"  Taking screenshot...")
                success = client.take_screenshot(str(screenshot_path))

                if success:
                    file_size = os.path.getsize(screenshot_path)
                    screenshots_taken += 1
                    print(f"  ✓ Saved: {level_id}.png ({file_size:,} bytes)")
                else:
                    print(f"  ✗ Failed to take screenshot")

        print(f"\n✓ Generated {screenshots_taken} screenshots in {output_dir}")
        print(
            "  Review screenshots for warehouse rooms (look for barrels and pedestals)"
        )
        print("  Warehouse rooms are rare special rooms - may require many attempts")
        return True

    finally:
        server.stop()


if __name__ == "__main__":
    success = main()
    sys.exit(0 if success else 1)
