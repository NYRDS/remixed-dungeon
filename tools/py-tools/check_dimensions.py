#!/usr/bin/env python3
"""
Utility to check image dimensions
"""

from PIL import Image
import sys

if len(sys.argv) != 2:
    print("Usage: python check_dimensions.py <image_path>")
    sys.exit(1)

image_path = sys.argv[1]

try:
    with Image.open(image_path) as img:
        width, height = img.size
        print(f"Image: {image_path}")
        print(f"Dimensions: {width}x{height} pixels")
        print(f"Mode: {img.mode}")
except Exception as e:
    print(f"Error opening image: {e}")