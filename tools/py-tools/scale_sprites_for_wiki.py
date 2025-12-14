#!/usr/bin/env python3
"""
Tool to upscale and enhance raw sprites from the sprites/ directory for wiki visualization.

This tool:
1. Scales sprites 8x using nearest neighbor interpolation
2. Adds a background and frame for better visualization
3. Saves the enhanced images to a specified output directory
"""

import argparse
import os
from PIL import Image


def upscale_and_enhance_sprite(input_path, output_path, scale_factor=8, bg_color=(240, 240, 240), frame_color=(100, 100, 100), canvas_extension=1):
    """
    Upscale a sprite with nearest neighbor interpolation and add background/frame.

    Args:
        input_path (str): Path to the input sprite
        output_path (str): Path where the enhanced sprite will be saved
        scale_factor (int): Factor by which to scale the sprite (default: 8)
        bg_color (tuple): RGB color for the background (default: light gray)
        frame_color (tuple): RGB color for the frame (default: dark gray)
        canvas_extension (int): Number of transparent pixels to add in each direction before scaling (default: 1)
    """
    # Open the input image
    img = Image.open(input_path)

    # Ensure the image has an alpha channel for transparency
    if img.mode != 'RGBA':
        img = img.convert('RGBA')

    # Get original dimensions
    orig_width, orig_height = img.size

    # Extend the canvas by adding transparent pixels in each direction before scaling
    if canvas_extension > 0:
        extended_width = orig_width + (2 * canvas_extension)
        extended_height = orig_height + (2 * canvas_extension)

        # Create a new image with transparent background
        extended_img = Image.new('RGBA', (extended_width, extended_height), (0, 0, 0, 0))

        # Paste the original image in the center
        extended_img.paste(img, (canvas_extension, canvas_extension))

        img = extended_img
        orig_width, orig_height = extended_width, extended_height

    # Calculate new dimensions after scaling
    new_width = orig_width * scale_factor
    new_height = orig_height * scale_factor

    # Scale the image using nearest neighbor interpolation
    upscaled_img = img.resize((new_width, new_height), Image.NEAREST)

    # Create a new image with background color and extra space for frame
    frame_size = 2  # Size of the frame in pixels
    final_width = new_width + (2 * frame_size)
    final_height = new_height + (2 * frame_size)

    final_img = Image.new('RGBA', (final_width, final_height), bg_color)

    # Draw the frame
    for x in range(final_width):
        for y in range(frame_size):
            final_img.putpixel((x, y), frame_color)  # Top frame
            final_img.putpixel((x, final_height - y - 1), frame_color)  # Bottom frame
    for y in range(final_height):
        for x in range(frame_size):
            final_img.putpixel((x, y), frame_color)  # Left frame
            final_img.putpixel((final_width - x - 1, y), frame_color)  # Right frame

    # Paste the upscaled image onto the final image with frame offset
    final_img.paste(upscaled_img, (frame_size, frame_size))

    # Save the final image
    final_img.save(output_path)


def process_sprites_directory(input_dir, output_dir, scale_factor=8, canvas_extension=1):
    """
    Process all sprites in the input directory and save enhanced versions to output directory.

    Args:
        input_dir (str): Directory containing raw sprites
        output_dir (str): Directory where enhanced sprites will be saved
        scale_factor (int): Factor by which to scale the sprites
        canvas_extension (int): Number of transparent pixels to add in each direction before scaling
    """
    # Create output directory if it doesn't exist
    os.makedirs(output_dir, exist_ok=True)

    # Supported image formats
    supported_formats = ('.png', '.jpg', '.jpeg', '.bmp', '.gif')

    # Process each image file in the input directory
    for filename in os.listdir(input_dir):
        if filename.lower().endswith(supported_formats):
            input_path = os.path.join(input_dir, filename)
            output_path = os.path.join(output_dir, filename)

            print(f"Processing: {filename}")
            upscale_and_enhance_sprite(input_path, output_path, scale_factor, canvas_extension=canvas_extension)
            print(f"Saved: {output_path}")


def main():
    parser = argparse.ArgumentParser(
        description="Upscale and enhance sprites for wiki visualization",
        formatter_class=argparse.RawDescriptionHelpFormatter,
        epilog="""
Examples:
  %(prog)s -i sprites/ -o wiki-sprites/     # Process all sprites with default settings
  %(prog)s -i sprites/ -o wiki-sprites/ -s 10  # Scale 10x instead of 8x
  %(prog)s -i sprites/ -o wiki-sprites/ -c 2  # Extend canvas by 2 pixels before scaling
  %(prog)s -i sprites/mobs/ -o wiki-mobs/   # Process only mob sprites
        """
    )
    parser.add_argument('-i', '--input', required=True, help='Input directory containing raw sprites')
    parser.add_argument('-o', '--output', required=True, help='Output directory for enhanced sprites')
    parser.add_argument('-s', '--scale', type=int, default=8, help='Scale factor (default: 8)')
    parser.add_argument('-c', '--canvas-extension', type=int, default=1,
                       help='Number of transparent pixels to add in each direction before scaling (default: 1)')
    parser.add_argument('--bg-color', nargs=3, type=int, default=[240, 240, 240],
                       help='Background color as R G B values (0-255 each, default: 240 240 240)')
    parser.add_argument('--frame-color', nargs=3, type=int, default=[100, 100, 100],
                       help='Frame color as R G B values (0-255 each, default: 100 100 100)')

    args = parser.parse_args()

    # Validate color arguments
    for color_val, arg_name in [(args.bg_color, '--bg-color'), (args.frame_color, '--frame-color')]:
        if not all(0 <= c <= 255 for c in color_val):
            parser.error(f"All values in {arg_name} must be between 0 and 255")

    # Convert to tuple for use in functions
    bg_color_tuple = tuple(args.bg_color)
    frame_color_tuple = tuple(args.frame_color)

    process_sprites_directory(args.input, args.output, args.scale, args.canvas_extension)


if __name__ == "__main__":
    # Check if PIL/Pillow is available
    try:
        from PIL import Image
    except ImportError:
        print("Error: Pillow library is not installed.")
        print("Install it with: pip install Pillow")
        exit(1)
    
    main()