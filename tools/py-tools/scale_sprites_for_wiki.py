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

try:
    import numpy as np
    HAS_NUMPY = True
except ImportError:
    HAS_NUMPY = False


def find_bounding_box(img):
    """
    Find the minimal bounding rectangle containing all non-transparent pixels.

    Args:
        img (PIL.Image): Input image with transparency

    Returns:
        tuple: (left, top, right, bottom) coordinates of the bounding box,
               or (0, 0, 0, 0) if no non-transparent pixels are found
    """
    # Convert to RGBA if not already
    if img.mode != 'RGBA':
        img = img.convert('RGBA')

    # Use numpy if available for better performance
    if HAS_NUMPY:
        # Convert image to numpy array
        img_array = np.array(img)

        # Get alpha channel (4th channel in RGBA)
        alpha_channel = img_array[:, :, 3]

        # Find rows and columns that have non-transparent pixels
        non_transparent_rows = np.any(alpha_channel > 0, axis=1)
        non_transparent_cols = np.any(alpha_channel > 0, axis=0)

        # Find the indices of these rows and columns
        row_indices = np.where(non_transparent_rows)[0]
        col_indices = np.where(non_transparent_cols)[0]

        # If no non-transparent pixels are found, return empty bounding box
        if len(row_indices) == 0 or len(col_indices) == 0:
            return (0, 0, 0, 0)

        # Get the bounding box coordinates
        top, bottom = row_indices[0], row_indices[-1] + 1
        left, right = col_indices[0], col_indices[-1] + 1

        return (left, top, right, bottom)
    else:
        # Fallback to original implementation using PIL directly
        # Get the image data
        img_data = img.getdata()
        width, height = img.size

        # Find the minimal and maximal coordinates of non-transparent pixels
        min_x, max_x = width, -1
        min_y, max_y = height, -1

        for y in range(height):
            for x in range(width):
                # Get the alpha value of the pixel
                alpha = img_data[y * width + x][3]

                # If the pixel is not transparent
                if alpha > 0:
                    min_x = min(min_x, x)
                    max_x = max(max_x, x)
                    min_y = min(min_y, y)
                    max_y = max(max_y, y)

        # If no non-transparent pixels were found, return the original dimensions
        if min_x == width:
            return (0, 0, 0, 0)

        return (min_x, min_y, max_x + 1, max_y + 1)


def upscale_and_enhance_sprite(input_path, output_path, scale_factor=8, bg_color=(240, 240, 240), frame_color=(100, 100, 100), canvas_extension=1, clip_to_content=True):
    """
    Upscale a sprite with nearest neighbor interpolation and add background/frame.

    Args:
        input_path (str): Path to the input sprite
        output_path (str): Path where the enhanced sprite will be saved
        scale_factor (int): Factor by which to scale the sprite (default: 8)
        bg_color (tuple): RGB color for the background (default: light gray)
        frame_color (tuple): RGB color for the frame (default: dark gray)
        canvas_extension (int): Number of transparent pixels to add in each direction before scaling (default: 1)
        clip_to_content (bool): Whether to clip to minimal rectangle of non-transparent pixels (default: True)
    """
    # Open the input image
    img = Image.open(input_path)

    # Ensure the image has an alpha channel for transparency
    if img.mode != 'RGBA':
        img = img.convert('RGBA')

    # Get original dimensions
    orig_width, orig_height = img.size

    # Clip to minimal bounding rectangle if requested
    if clip_to_content:
        bbox = find_bounding_box(img)
        if bbox != (0, 0, 0, 0):  # If non-transparent pixels were found
            left, top, right, bottom = bbox
            img = img.crop((left, top, right, bottom))
            # Update dimensions after cropping
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


def convert_camel_case_to_snake_case(name):
    """
    Convert CamelCase name to snake_case.

    Args:
        name (str): The CamelCase name to convert

    Returns:
        str: The snake_case name
    """
    import re
    # Handle special cases where there are multiple uppercase letters together
    s1 = re.sub('(.)([A-Z][a-z]+)', r'\1_\2', name)
    return re.sub('([a-z0-9])([A-Z])', r'\1_\2', s1).lower()


def get_entity_type_and_name(filename):
    """
    Extract entity type and name from the current filename format.

    Args:
        filename (str): The current filename (e.g., 'mob_Tengu.png')

    Returns:
        tuple: (entity_type, entity_name) or (None, None) if format doesn't match
    """
    import re
    # Match patterns like: mob_Tengu.png, item_Ankh.png, spell_Heal.png, etc.
    pattern = r'^(mob|item|spell|buff|hero|npc|level|config|mechanic|skill|talent|trap|script)_([^.]+)\.(.+)$'
    match = re.match(pattern, filename)

    if match:
        entity_type = match.group(1)
        entity_name = match.group(2)
        extension = match.group(3)
        return entity_type, entity_name, extension

    return None, None, None


def get_new_image_name(filename):
    """
    Generate the new image name based on the page naming convention.

    Args:
        filename (str): The current filename (e.g., 'mob_Tengu.png')

    Returns:
        str: The new filename (e.g., 'tengu_mob.png') or None if format doesn't match
    """
    entity_type, entity_name, extension = get_entity_type_and_name(filename)

    if entity_type is None:
        return filename  # Return original name if format doesn't match known patterns

    # Convert the entity name from CamelCase to snake_case
    snake_case_name = convert_camel_case_to_snake_case(entity_name)

    # Create the new filename following the page naming convention
    # For example: tengu_mob.png, ankh_item.png, heal_spell.png
    new_name = f"{snake_case_name}_{entity_type}.{extension}"

    return new_name


def process_sprites_directory(input_dir, output_dir, scale_factor=8, canvas_extension=1, clip_to_content=True):
    """
    Process all sprites in the input directory and save enhanced versions to output directory.

    Args:
        input_dir (str): Directory containing raw sprites
        output_dir (str): Directory where enhanced sprites will be saved
        scale_factor (int): Factor by which to scale the sprites
        canvas_extension (int): Number of transparent pixels to add in each direction before scaling
        clip_to_content (bool): Whether to clip to minimal rectangle of non-transparent pixels (default: True)
    """
    # Create output directory if it doesn't exist
    os.makedirs(output_dir, exist_ok=True)

    # Supported image formats
    supported_formats = ('.png', '.jpg', '.jpeg', '.bmp', '.gif')

    # Process each image file in the input directory
    for filename in os.listdir(input_dir):
        if filename.lower().endswith(supported_formats):
            input_path = os.path.join(input_dir, filename)

            # Generate new filename that matches page naming convention
            new_filename = get_new_image_name(filename)
            output_path = os.path.join(output_dir, new_filename)

            print(f"Processing: {filename} -> {new_filename}")
            upscale_and_enhance_sprite(input_path, output_path, scale_factor, canvas_extension=canvas_extension, clip_to_content=clip_to_content)
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
  %(prog)s -i sprites/ -o wiki-sprites/ --no-clip  # Disable content clipping
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
    parser.add_argument('--no-clip', action='store_false', dest='clip_to_content',
                       help='Disable clipping to minimal rectangle of non-transparent pixels (default: clipping is enabled)')

    args = parser.parse_args()

    # Validate color arguments
    for color_val, arg_name in [(args.bg_color, '--bg-color'), (args.frame_color, '--frame-color')]:
        if not all(0 <= c <= 255 for c in color_val):
            parser.error(f"All values in {arg_name} must be between 0 and 255")

    # Convert to tuple for use in functions
    bg_color_tuple = tuple(args.bg_color)
    frame_color_tuple = tuple(args.frame_color)

    process_sprites_directory(args.input, args.output, args.scale, args.canvas_extension, args.clip_to_content)


if __name__ == "__main__":
    # Check if PIL/Pillow is available
    try:
        from PIL import Image
    except ImportError:
        print("Error: Pillow library is not installed.")
        print("Install it with: pip install Pillow")
        exit(1)

    # Check if NumPy is available
    try:
        import numpy as np
    except ImportError:
        print("Warning: NumPy library is not installed. Performance will be reduced.")
        print("For better performance, install it with: pip install numpy")

    main()