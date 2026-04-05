#!/usr/bin/env python3
"""
Generate clean warehouse screenshots with UI disabled.

Usage:
    python3 warehouse_clean_screenshots.py [--count N] [--output-dir PATH]
"""

import argparse
import os
import sys
import time
from pathlib import Path
from game_client import GameClient
from test_server import ServerManager


def main():
    parser = argparse.ArgumentParser(description="Generate clean warehouse screenshots")
    parser.add_argument(
        "--count", type=int, default=5, help="Number of screenshots to generate"
    )
    parser.add_argument(
        "--output-dir", default="/tmp/warehouse_clean", help="Output directory"
    )
    parser.add_argument("--port", type=int, default=8080, help="WebServer port")
    parser.add_argument("--host", default="localhost", help="WebServer host")
    args = parser.parse_args()

    output_dir = Path(args.output_dir)
    output_dir.mkdir(parents=True, exist_ok=True)

    client = GameClient(args.host, args.port)
    server = ServerManager(args.host, args.port, "warehouse_clean")

    if not server.start():
        print("✗ Failed to start server")
        return False

    try:
        print("Generating clean warehouse screenshots...")
        print(f"Screenshots will be saved to: {output_dir}")

        # Start game
        print("\n[Start] Starting game as WARRIOR...")
        response = client.start_game("WARRIOR", 0)
        if not response.get("success", False):
            print(f"✗ Failed to start game: {response.get('error', 'Unknown error')}")
            return False
        print("✓ Game started")

        # Go to WarehouseDemoLevel
        print("\n[Level] Going to WarehouseDemoLevel...")
        response = client.go_to_level("warehouseDemo")
        if not response.get("success", False):
            print(
                f"⚠ Could not go to WarehouseDemoLevel: {response.get('error', 'Unknown')}"
            )
            print("Trying regular dungeon levels instead...")
            level_ids = ["3", "4", "6", "7", "11", "12"]
        else:
            print("✓ Arrived at WarehouseDemoLevel (with forced warehouse rooms)")
            level_ids = ["warehouseDemo"]

        # Disable UI
        print("\n[UI] Disabling UI for clean screenshots...")
        response = client._get("/debug/toggle_ui")
        if response.get("success", False):
            print(f"  ✓ UI {response.get('message', 'toggled')}")
        else:
            print(f"  ⚠ Could not toggle UI: {response.get('error', 'Unknown')}")

        # Wait for UI to hide
        time.sleep(1)

        # Generate screenshots
        screenshot_count = 0

        for level_id in level_ids:
            if screenshot_count >= args.count:
                break

            if level_id != "WarehouseDemoLevel":
                print(
                    f"\n[{screenshot_count + 1}/{args.count}] Going to level {level_id}..."
                )
                response = client.go_to_level(level_id)
                if not response.get("success", False):
                    print(
                        f"  ⚠ Could not go to {level_id}: {response.get('error', 'Unknown')}"
                    )
                    continue
                print(f"  ✓ Arrived at {level_id}")

                # Reveal map for this level
                print(f"  Revealing map...")
                response = client._get("/debug/reveal_map")
                if response.get("success", False):
                    print(f"  ✓ {response.get('message', 'Map revealed')}")
                else:
                    print(
                        f"  ⚠ Could not reveal map: {response.get('error', 'Unknown')}"
                    )

                time.sleep(2)

            # Get level info
            level_info = client.get_level_info()
            if "width" in level_info and "height" in level_info:
                width = level_info["width"]
                height = level_info["height"]
                print(f"  Level size: {width}x{height}")

                # Move to several positions and take screenshots
                positions = [
                    (width // 4, height // 4),
                    (width // 2, height // 2),
                    (3 * width // 4, 3 * height // 4),
                    (width // 2, height // 4),
                    (width // 4, height // 2),
                ]

                for i, (x, y) in enumerate(positions):
                    if screenshot_count >= args.count:
                        break

                    print(f"  Moving to position {i + 1}/5: ({x}, {y})...")
                    client.move_hero(x, y)
                    time.sleep(1)

                    # Take screenshot
                    screenshot_path = output_dir / f"{level_id}_clean_{i + 1}.png"
                    print(f"  Taking clean screenshot...")
                    success = client.take_screenshot(str(screenshot_path))

                    if success:
                        file_size = os.path.getsize(screenshot_path)
                        screenshot_count += 1
                        print(
                            f"  ✓ Saved: {level_id}_clean_{i + 1}.png ({file_size:,} bytes)"
                        )
                    else:
                        print(f"  ✗ Failed to take screenshot")

                    time.sleep(0.5)

        # Re-enable UI
        print("\n[UI] Re-enabling UI...")
        response = client._get("/debug/toggle_ui")
        if response.get("success", False):
            print(f"  ✓ UI {response.get('message', 'toggled')}")

        print(f"\n✓ Generated {screenshot_count} clean screenshots in {output_dir}")
        print("  Screenshots should show warehouse rooms with no UI overlay")
        return True

    finally:
        server.stop()


if __name__ == "__main__":
    success = main()
    sys.exit(0 if success else 1)
