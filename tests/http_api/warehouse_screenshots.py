#!/usr/bin/env python3
"""
Generate screenshots of warehouse rooms using WarehouseTestLevel.

Usage:
    python3 warehouse_screenshots.py [--count N] [--output-dir PATH]
"""

import argparse
import os
import sys
import time
from pathlib import Path

from game_client import GameClient
from test_server import ServerManager


def main():
    parser = argparse.ArgumentParser(description="Generate warehouse room screenshots")
    parser.add_argument(
        "--count", type=int, default=10, help="Number of screenshots to generate"
    )
    parser.add_argument(
        "--output-dir",
        default="/tmp/warehouse_screenshots_final",
        help="Output directory",
    )
    parser.add_argument("--port", type=int, default=8080, help="WebServer port")
    parser.add_argument("--host", default="localhost", help="WebServer host")
    args = parser.parse_args()

    output_dir = Path(args.output_dir)
    output_dir.mkdir(parents=True, exist_ok=True)

    client = GameClient(args.host, args.port)
    server = ServerManager(args.host, args.port, "warehouse_final")

    if not server.start():
        print("✗ Failed to start server")
        return False

    try:
        print(f"Generating {args.count} warehouse room screenshots...")
        print(f"Screenshots will be saved to: {output_dir}")

        for i in range(args.count):
            screenshot_path = output_dir / f"warehouse_{i + 1:03d}.png"

            print(f"\n[{i + 1}/{args.count}] Starting game...")

            # Start game as WARRIOR (warehouse generation doesn't depend on hero class)
            response = client.start_game("WARRIOR", 0)
            if not response.get("success", False):
                print(
                    f"  ✗ Failed to start game: {response.get('error', 'Unknown error')}"
                )
                continue

            print(f"  ✓ Game started")

            # Change to WarehouseTestLevel (depth 99 to use custom level)
            # We'll use go_to_level with the WarehouseTestLevel class name
            print(f"  Switching to WarehouseTestLevel...")
            level_response = client.go_to_level("WarehouseTestLevel")

            if not level_response.get("success", False):
                print(
                    f"  ⚠ Could not switch to WarehouseTestLevel, using current level"
                )
                print(f"    Error: {level_response.get('error', 'Unknown')}")

            # Wait for level to render
            time.sleep(1)

            print(f"  Taking screenshot...")
            success = client.take_screenshot(str(screenshot_path))

            if success:
                file_size = os.path.getsize(screenshot_path)
                print(
                    f"  ✓ Screenshot saved: {screenshot_path.name} ({file_size:,} bytes)"
                )
            else:
                print(f"  ✗ Failed to take screenshot")

        print(f"\n✓ Generated screenshots in {output_dir}")
        print(
            "  These screenshots should contain warehouse rooms with barrels and pedestals"
        )
        return True

    finally:
        server.stop()


if __name__ == "__main__":
    success = main()
    sys.exit(0 if success else 1)
