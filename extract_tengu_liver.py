#!/usr/bin/env python3
"""
Script to extract the Tengu Liver sprite from the mastery_items.png file
"""

from PIL import Image
import os

# Define the source and destination paths
source_path = "/home/mike/StudioProjects/remixed-dungeon/RemixedDungeon/src/main/assets/items/mastery_items.png"
destination_dir = "/home/mike/StudioProjects/remixed-dungeon/wiki-data/media/rpd/images"

# Create destination directory if it doesn't exist
os.makedirs(destination_dir, exist_ok=True)

# Path for the extracted image
output_path = os.path.join(destination_dir, "tengu_liver_item.png")

# Open the sprite sheet
with Image.open(source_path) as img:
    # The sprite sheet is organized in a grid, each sprite 16x16 pixels
    sprite_width = 16
    sprite_height = 16
    
    # Calculate the number of columns in the sprite sheet (assuming it's square)
    columns = img.width // sprite_width
    
    # The index in the sheet is 1 (0-based would be 0)
    index = 1 - 1  # Convert to 0-based index
    
    # Calculate the position of the sprite
    row = index // columns
    col = index % columns
    
    # Calculate the coordinates for cropping
    left = col * sprite_width
    top = row * sprite_height
    right = left + sprite_width
    bottom = top + sprite_height
    
    # Crop the specific sprite
    sprite = img.crop((left, top, right, bottom))
    
    # Scale it up 8x for wiki display (nearest neighbor to preserve pixel art quality)
    scaled_sprite = sprite.resize((sprite_width * 8, sprite_height * 8), Image.NEAREST)
    
    # Save the extracted sprite
    scaled_sprite.save(output_path)
    print(f"Extracted Tengu Liver sprite to {output_path}")

print("Tengu Liver sprite extraction completed.")