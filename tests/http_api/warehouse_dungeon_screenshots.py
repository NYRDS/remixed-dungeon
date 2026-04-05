#!/usr/bin/env python3
"""
Generate warehouse screenshots by navigating to dungeon levels.

Usage:
    python3 warehouse_dungeon_screenshots.py [--count N] [--output-dir PATH]
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
        description="Generate warehouse screenshots from dungeon levels"
    )
    parser.add_argument(
        "--count", type=int, default=10, help="Number of screenshots to generate"
    )
    parser.add_argument(
        "--output-dir", default="/tmp/warehouse_dungeon", help="Output directory"
    )
    parser.add_argument("--port", type=int, default=8080, help="WebServer port")
    parser.add_argument("--host", default="localhost", help="WebServer host")
    args = parser.parse_args()

    output_dir = Path(args.output_dir)
    output_dir.mkdir(parents=True, exist_ok=True)

    client = GameClient(args.host, args.port)
    server = ServerManager(args.host, args.port, "warehouse_dungeon")

    if not server.start():
        print("✗ Failed to start server")
        return False

    try:
        print("Starting game and exploring dungeon levels for warehouse rooms...")
        print(f"Screenshots will be saved to: {output_dir}")

        # Start ONE game
        print("\n[Start] Starting game as WARRIOR...")
        response = client.start_game("WARRIOR", 0)
        if not response.get("success", False):
            print(f"✗ Failed to start game: {response.get('error', 'Unknown error')}")
            return False
        print("✓ Game started")

        # Get available levels
        print("\n[Info] Getting available levels...")
        levels_response = client.list_levels()
        if not levels_response.get("success", False):
            print(f"⚠ Could not get levels: {levels_response.get('error', 'Unknown')}")
            print("Proceeding with depth-based exploration...")
            levels = []
        else:
            levels = levels_response.get("levels", [])
            print(f"  Found {len(levels)} levels")
            for level in levels[:5]:
                print(
                    f"    - {level.get('id', 'unknown')}: {level.get('name', 'unknown')} (depth {level.get('depth', 'unknown')})"
                )

        screenshot_count = 0
        depths_to_try = [
            1,
            2,
            3,
            4,
            5,
            6,
            7,
            8,
            9,
            10,
            11,
            12,
            13,
            14,
            15,
            16,
            17,
            18,
            19,
            20,
        ]

        for depth in depths_to_try:
            if screenshot_count >= args.count:
                break

            screenshot_path = output_dir / f"depth_{depth:02d}.png"

            print(f"\n[{screenshot_count + 1}/{args.count}] Exploring depth {depth}...")

            # Try to change to this depth
            response = client.change_level(depth)

            if not response.get("success", False):
                error = response.get("error", "Unknown")
                print(f"  ⚠ Could not change to depth {depth}: {error}")
                continue

            print(f"  ✓ Changed to depth {depth}")

            # Wait for level to fully render
            time.sleep(4)

            # Take screenshot
            print(f"  Taking screenshot...")
            success = client.take_screenshot(str(screenshot_path))

            if success:
                file_size = os.path.getsize(screenshot_path)
                screenshot_count += 1
                print(f"  ✓ Saved: depth_{depth:02d}.png ({file_size:,} bytes)")
            else:
                print(f"  ✗ Failed to take screenshot")

            # Small delay between levels
            time.sleep(1)

        print(f"\n✓ Generated {screenshot_count} screenshots in {output_dir}")
        print(
            "  Review screenshots for warehouse rooms (look for barrels and pedestals)"
        )
        print("  Warehouse rooms appear as special rooms with sokoban-like layouts")
        return True

    finally:
        server.stop()


if __name__ == "__main__":
    success = main()
    sys.exit(0 if success else 1)
