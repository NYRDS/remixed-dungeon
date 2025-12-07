#!/usr/bin/env python3
"""
Script to extract the first frame from each mob spritesheet,
scale it 8x with nearest neighbor filtering, and add it to wiki pages.
"""

import json
import os
from PIL import Image
import re

def extract_and_scale_sprites():
    """
    Extract first frame from each mob spritesheet, scale 8x, and add to wiki pages
    """
    # Define the directories
    sprites_desc_dir = "RemixedDungeon/src/main/assets/spritesDesc"
    sprites_dir = "RemixedDungeon/src/main/assets"
    wiki_dir = "wiki-data/pages/rpd"  # Standard location for wiki pages
    
    # Get all JSON files in the spritesDesc directory
    json_files = [f for f in os.listdir(sprites_desc_dir) if f.endswith('.json')]
    
    for json_file in json_files:
        # Skip non-mob files if needed (though we'll process all)
        print(f"Processing: {json_file}")
        
        # Load the JSON file
        with open(os.path.join(sprites_desc_dir, json_file), 'r') as f:
            sprite_data = json.load(f)
        
        # Get the texture filename and dimensions
        texture_filename = sprite_data.get('texture')
        if not texture_filename:
            print(f"  Skipping {json_file} - no texture specified")
            continue
            
        sprite_width = sprite_data.get('width', 0)
        sprite_height = sprite_data.get('height', 0)
        
        if sprite_width <= 0 or sprite_height <= 0:
            print(f"  Skipping {json_file} - invalid dimensions")
            continue
        
        # Extract mob name from JSON filename (remove .json extension)
        mob_name = os.path.splitext(json_file)[0].lower()
        
        # Convert camelCase to snake_case for wiki filenames
        mob_name_snake = re.sub('([a-z0-9])([A-Z])', r'\1_\2', mob_name).lower()
        
        # Check if the sprite texture exists
        texture_path = os.path.join(sprites_dir, texture_filename)
        if not os.path.exists(texture_path):
            print(f"  Texture file {texture_path} not found, checking in mobs/ directory")
            texture_path = os.path.join(sprites_dir, 'mobs', texture_filename)
            if not os.path.exists(texture_path):
                print(f"  Texture file still not found for {json_file}")
                continue
        
        print(f"  Using texture: {texture_path}")
        
        # Load the sprite sheet
        try:
            sprite_sheet = Image.open(texture_path)
        except Exception as e:
            print(f"  Error loading sprite sheet {texture_path}: {e}")
            continue
        
        # Get sprite sheet dimensions
        sheet_width, sheet_height = sprite_sheet.size
        print(f"  Sheet size: {sheet_width}x{sheet_height}, Sprite size: {sprite_width}x{sprite_height}")
        
        # Calculate how many sprites fit in each row
        sprites_per_row = sheet_width // sprite_width
        
        # Get the first frame of the idle animation
        idle_frames = sprite_data.get('idle', {}).get('frames', [])
        if not idle_frames:
            # If no idle animation defined, use frame 0
            first_frame_index = 0
        else:
            first_frame_index = idle_frames[0]
        
        print(f"  First frame index: {first_frame_index}")
        
        # Calculate frame position in the spritesheet
        frame_x = (first_frame_index % sprites_per_row) * sprite_width
        frame_y = (first_frame_index // sprites_per_row) * sprite_height
        
        print(f"  Frame position: ({frame_x}, {frame_y})")
        
        # Extract the first frame
        frame = sprite_sheet.crop((frame_x, frame_y, frame_x + sprite_width, frame_y + sprite_height))
        
        # Scale the frame 8x using nearest neighbor interpolation
        scaled_frame = frame.resize((sprite_width * 8, sprite_height * 8), Image.NEAREST)
        
        # Create output directory for mob images if it doesn't exist
        images_dir = os.path.join(wiki_dir, 'images')
        os.makedirs(images_dir, exist_ok=True)
        
        # Save the scaled frame
        output_filename = os.path.join(images_dir, f"{mob_name_snake}_sprite.png")
        scaled_frame.save(output_filename)
        
        print(f"  Saved scaled sprite to: {output_filename}")
        
        # Add the image to the corresponding wiki page
        wiki_page_path = os.path.join(wiki_dir, f"{mob_name_snake}.txt")
        
        # Check if wiki page exists
        if os.path.exists(wiki_page_path):
            # Read existing content
            with open(wiki_page_path, 'r', encoding='utf-8') as f:
                content = f.read()
            
            # Check if image is already added
            image_tag = f"{{:rpd:images:{mob_name_snake}_sprite.png?200|}}"
            if image_tag in content:
                print(f"  Image already exists in wiki page: {wiki_page_path}")
            else:
                # Add image at the beginning of the content, after the header if there's one
                lines = content.split('\n')
                
                # Look for the first content line (after potential header)
                insert_index = 0
                for i, line in enumerate(lines):
                    if line.strip() and not line.startswith('======'):  # Find first non-header content
                        insert_index = i
                        break
                
                # Insert the image tag
                lines.insert(insert_index, image_tag)
                lines.insert(insert_index + 1, "")  # Add an empty line after the image
                
                # Write back to the file
                with open(wiki_page_path, 'w', encoding='utf-8') as f:
                    f.write('\n'.join(lines))
                
                print(f"  Added image to wiki page: {wiki_page_path}")
        else:
            print(f"  Wiki page does not exist: {wiki_page_path}")
            # Create a basic wiki page with the image
            basic_content = f"====== {mob_name} ======\n\n{{:rpd:images:{mob_name_snake}_sprite.png?200|}}\n\nDescription for {mob_name} goes here."
            with open(wiki_page_path, 'w', encoding='utf-8') as f:
                f.write(basic_content)
            print(f"  Created basic wiki page with image: {wiki_page_path}")


if __name__ == "__main__":
    extract_and_scale_sprites()