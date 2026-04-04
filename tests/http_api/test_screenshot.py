#!/usr/bin/env python3
"""
Test screenshot functionality in minimized window mode.

Usage:
    python3 test_screenshot.py [--port PORT] [--output PATH]
    python3 test_screenshot.py --start-server
"""

import argparse
import os
import sys
from pathlib import Path

from game_client import GameClient
from test_server import ServerManager
from PIL import Image


def main():
    parser = argparse.ArgumentParser(description="Test screenshot functionality")
    parser.add_argument("--port", type=int, default=8080, help="WebServer port")
    parser.add_argument("--host", default="localhost", help="WebServer host")
    parser.add_argument(
        "--output", default="/tmp/test_screenshot.png", help="Output file path"
    )
    parser.add_argument(
        "--start-server", action="store_true", help="Start game server automatically"
    )
    args = parser.parse_args()

    client = GameClient(args.host, args.port)
    server = None

    if args.start_server:
        server = ServerManager(args.host, args.port, "screenshot")
        if not server.start():
            return False
    else:
        print(f"Connecting to {args.host}:{args.port}...")
        if not client.check_server():
            print("✗ Cannot connect to server")
            return False
        print("✓ Server connected")

    try:
        print("Starting game...")
        response = client.start_game("ROGUE", 0)
        if not response.get("success", False):
            print(f"✗ Failed to start game: {response.get('error', 'Unknown error')}")
            return False

        print("✓ Game started")

        import time

        time.sleep(1)

        print(f"Taking screenshot to {args.output}...")
        success = client.take_screenshot(args.output)

        if not success:
            print("✗ Failed to take screenshot")
            return False

        print(f"✓ Screenshot saved to {args.output}")

        try:
            img = Image.open(args.output)
            width, height = img.size
            file_size = os.path.getsize(args.output)

            print(f"\nScreenshot verification:")
            print(f"  Dimensions: {width}x{height} pixels")
            print(f"  Mode: {img.mode}")
            print(f"  File size: {file_size:,} bytes")
            print(f"  Path: {args.output}")

            if width == 800 and height == 480:
                print("✓ Screenshot dimensions are correct (800x480)")
            else:
                print(f"⚠ Warning: Expected 800x480, got {width}x{height}")

            print("\n✓ Screenshot test PASSED")
            return True
        except Exception as e:
            print(f"✗ Error verifying screenshot: {e}")
            return False
    finally:
        if server:
            server.stop()


if __name__ == "__main__":
    success = main()
    sys.exit(0 if success else 1)
