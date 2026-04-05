#!/usr/bin/env python3
"""
Generate screenshots by starting ONE game and exploring multiple levels.

Usage:
    python3 single_game_screenshots.py [--count N] [--output-dir PATH]
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
        description="Generate screenshots from single game"
    )
    parser.add_argument(
        "--count", type=int, default=10, help="Number of screenshots to generate"
    )
    parser.add_argument(
        "--output-dir", default="/tmp/single_game_screenshots", help="Output directory"
    )
    parser.add_argument("--port", type=int, default=8080, help="WebServer port")
    parser.add_argument("--host", default="localhost", help="WebServer host")
    args = parser.parse_args()

    output_dir = Path(args.output_dir)
    output_dir.mkdir(parents=True, exist_ok=True)

    client = GameClient(args.host, args.port)
    server = ServerManager(args.host, args.port, "single_game_screens")

    if not server.start():
        print("✗ Failed to start server")
        return False

    try:
        print("Starting single game and exploring multiple levels...")
        print(f"Screenshots will be saved to: {output_dir}")

        # Start ONE game
        print("\n[Start] Starting game as WARRIOR...")
        response = client.start_game("WARRIOR", 0)
        if not response.get("success", False):
            print(f"✗ Failed to start game: {response.get('error', 'Unknown error')}")
            return False
        print("✓ Game started")

        # Wait for initial level to fully render
        print("Waiting for initial level to render...")
        time.sleep(5)

        # Take first screenshot
        screenshot_path = output_dir / f"level_001.png"
        print(f"\n[1/{args.count}] Taking screenshot of starting level...")
        success = client.take_screenshot(str(screenshot_path))
        if success:
            file_size = os.path.getsize(screenshot_path)
            print(f"  ✓ Saved: level_001.png ({file_size:,} bytes)")
        else:
            print(f"  ✗ Failed to take screenshot")

        # Generate more screenshots by changing levels
        for i in range(2, args.count + 1):
            screenshot_path = output_dir / f"level_{i:003}.png"

            # Change to a random level
            level_id = str(i + 2)  # Start from level 3 (after initial 2)
            print(f"\n[{i}/{args.count}] Changing to level {level_id}...")

            response = client.change_level(i + 2)
            if not response.get("success", False):
                print(
                    f"  ⚠ Could not change to level {level_id}: {response.get('error', 'Unknown')}"
                )
                # Try taking screenshot anyway
                print(f"  Taking screenshot of current level...")
            else:
                print(f"  ✓ Changed to level {level_id}")

            # Wait for level to render
            time.sleep(3)

            print(f"  Taking screenshot...")
            success = client.take_screenshot(str(screenshot_path))

            if success:
                file_size = os.path.getsize(screenshot_path)
                print(f"  ✓ Saved: level_{i:003}.png ({file_size:,} bytes)")
            else:
                print(f"  ✗ Failed to take screenshot")

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
