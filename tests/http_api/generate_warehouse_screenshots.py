#!/usr/bin/env python3
"""
Generate screenshots of warehouse rooms by starting multiple games and taking screenshots.

Usage:
    python3 generate_warehouse_screenshots.py [--count N] [--output-dir PATH]
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
        description="Generate screenshots to find warehouse rooms"
    )
    parser.add_argument(
        "--count", type=int, default=20, help="Number of games to start"
    )
    parser.add_argument(
        "--output-dir", default="/tmp/warehouse_screenshots", help="Output directory"
    )
    parser.add_argument("--port", type=int, default=8080, help="WebServer port")
    parser.add_argument("--host", default="localhost", help="WebServer host")
    args = parser.parse_args()

    output_dir = Path(args.output_dir)
    output_dir.mkdir(parents=True, exist_ok=True)

    client = GameClient(args.host, args.port)
    server = ServerManager(args.host, args.port, "warehouse_screenshots")

    if not server.start():
        print("✗ Failed to start server")
        return False

    try:
        print(f"Starting {args.count} games to find warehouse rooms...")
        print(f"Screenshots will be saved to: {output_dir}")

        hero_classes = ["WARRIOR", "ROGUE", "MAGE", "HUNTRESS", "ELF", "DOCTOR"]

        for i in range(args.count):
            hero_class = hero_classes[i % len(hero_classes)]
            screenshot_path = output_dir / f"game_{i + 1:03d}_{hero_class}.png"

            print(f"\n[{i + 1}/{args.count}] Starting game as {hero_class}...")

            response = client.start_game(hero_class, 0)
            if not response.get("success", False):
                print(
                    f"  ✗ Failed to start game: {response.get('error', 'Unknown error')}"
                )
                continue

            print(f"  ✓ Game started, taking screenshot...")
            time.sleep(0.5)  # Wait for level to render

            success = client.take_screenshot(str(screenshot_path))
            if success:
                file_size = os.path.getsize(screenshot_path)
                print(
                    f"  ✓ Screenshot saved: {screenshot_path.name} ({file_size:,} bytes)"
                )
            else:
                print(f"  ✗ Failed to take screenshot")

        print(f"\n✓ Generated {args.count} screenshots in {output_dir}")
        print(
            "  Manually review screenshots to identify warehouse rooms (look for barrels and pedestals)"
        )
        return True

    finally:
        server.stop()


if __name__ == "__main__":
    success = main()
    sys.exit(0 if success else 1)
