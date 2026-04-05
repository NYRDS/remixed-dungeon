#!/usr/bin/env python3
"""
Generate screenshots at different dungeon depths to find warehouse rooms.

Usage:
    python3 depth_screenshots.py [--output-dir PATH]
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
        description="Generate screenshots at different depths"
    )
    parser.add_argument(
        "--output-dir", default="/tmp/depth_screenshots", help="Output directory"
    )
    parser.add_argument("--port", type=int, default=8080, help="WebServer port")
    parser.add_argument("--host", default="localhost", help="WebServer host")
    args = parser.parse_args()

    output_dir = Path(args.output_dir)
    output_dir.mkdir(parents=True, exist_ok=True)

    client = GameClient(args.host, args.port)
    server = ServerManager(args.host, args.port, "depth_screenshots")

    if not server.start():
        print("✗ Failed to start server")
        return False

    try:
        print("Starting game and exploring different depths...")
        print(f"Screenshots will be saved to: {output_dir}")

        # Start game
        print("\n[Start] Starting game as WARRIOR...")
        response = client.start_game("WARRIOR", 0)
        if not response.get("success", False):
            print(f"✗ Failed to start game: {response.get('error', 'Unknown error')}")
            return False
        print("✓ Game started")

        # Take screenshots at different depths
        depths = [1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15]

        for depth in depths:
            screenshot_path = output_dir / f"depth_{depth:02d}.png"

            print(f"\n[Depth {depth}] Changing to depth {depth}...")
            response = client.change_level(depth)

            if not response.get("success", False):
                print(
                    f"  ⚠ Could not change to depth {depth}: {response.get('error', 'Unknown')}"
                )
                continue

            print(f"  ✓ Changed to depth {depth}")

            # Wait for level to render
            time.sleep(0.5)

            print(f"  Taking screenshot...")
            success = client.take_screenshot(str(screenshot_path))

            if success:
                file_size = os.path.getsize(screenshot_path)
                print(
                    f"  ✓ Screenshot saved: depth_{depth:02d}.png ({file_size:,} bytes)"
                )
            else:
                print(f"  ✗ Failed to take screenshot")

            # Small delay between levels
            time.sleep(0.3)

        print(f"\n✓ Generated {len(depths)} screenshots in {output_dir}")
        print(
            "  Review screenshots for warehouse rooms (look for barrels and pedestals)"
        )
        return True

    finally:
        server.stop()


if __name__ == "__main__":
    success = main()
    sys.exit(0 if success else 1)
