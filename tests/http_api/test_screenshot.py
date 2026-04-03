#!/usr/bin/env python3
"""
Test screenshot functionality in minimized window mode.

Usage:
    python3 test_screenshot.py [--port PORT] [--output PATH]
"""

import argparse
import os
from pathlib import Path

from game_client import GameClient
from PIL import Image


def main():
    parser = argparse.ArgumentParser(description="Test screenshot functionality")
    parser.add_argument("--port", type=int, default=8080, help="WebServer port")
    parser.add_argument("--host", default="localhost", help="WebServer host")
    parser.add_argument(
        "--output", default="/tmp/test_screenshot.png", help="Output file path"
    )
    args = parser.parse_args()

    # Initialize client
    client = GameClient(args.host, args.port)

    # Check server
    print(f"Connecting to {args.host}:{args.port}...")
    if not client.check_server():
        print("✗ Cannot connect to server")
        return False

    print("✓ Server connected")

    # Start game if needed
    print("Starting game...")
    response = client.start_game("ROGUE", 0)
    if not response.get("success", False):
        print(f"✗ Failed to start game: {response.get('error', 'Unknown error')}")
        return False

    print("✓ Game started")

    # Wait a moment for game to be ready
    import time

    time.sleep(1)

    # Take screenshot
    print(f"Taking screenshot to {args.output}...")
    success = client.take_screenshot(args.output)

    if not success:
        print("✗ Failed to take screenshot")
        return False

    print(f"✓ Screenshot saved to {args.output}")

    # Verify screenshot
    try:
        img = Image.open(args.output)
        width, height = img.size
        file_size = os.path.getsize(args.output)

        print(f"\nScreenshot verification:")
        print(f"  Dimensions: {width}x{height} pixels")
        print(f"  Mode: {img.mode}")
        print(f"  File size: {file_size:,} bytes")
        print(f"  Path: {args.output}")

        # Check if dimensions are correct
        if width == 800 and height == 480:
            print("✓ Screenshot dimensions are correct (800x480)")
        else:
            print(f"⚠ Warning: Expected 800x480, got {width}x{height}")

        print("\n✓ Screenshot test PASSED")
        return True
    except Exception as e:
        print(f"✗ Error verifying screenshot: {e}")
        return False


if __name__ == "__main__":
    success = main()
    exit(0 if success else 1)
