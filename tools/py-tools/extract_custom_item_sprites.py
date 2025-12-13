#!/usr/bin/env python3
"""
Script to extract item sprites from multiple spritesheets
by parsing the Java files that explicitly set imageFile and image index.
"""

import os
import re
from PIL import Image


def extract_all_item_sprites():
    """
    Extract item sprites from multiple spritesheets using indices from Java code
    """
    # Define directories
    assets_dir = "RemixedDungeon/src/main/assets"
    wiki_dir = "wiki-data/pages/rpd"
    
    # Create output directory for item images
    images_dir = os.path.join(wiki_dir, 'images')
    os.makedirs(images_dir, exist_ok=True)
    
    # Find all Java files that set both imageFile and image
    java_files = find_java_files_with_image_and_imageFile()
    
    extracted_count = 0
    
    for file_path, item_name, image_file, image_index in java_files:
        spritesheet_path = os.path.join(assets_dir, image_file)
        
        if not os.path.exists(spritesheet_path):
            print(f"Spritesheet not found: {spritesheet_path}")
            continue
        
        # Load the spritesheet
        try:
            spritesheet = Image.open(spritesheet_path)
        except Exception as e:
            print(f"Error loading spritesheet {image_file}: {e}")
            continue
        
        sheet_width, sheet_height = spritesheet.size
        print(f"Processing {image_file} for {item_name} (index {image_index}): {sheet_width}x{sheet_height}")
        
        # Items are arranged in 16x16 pixel grid
        item_width = 16
        item_height = 16
        sprites_per_row = sheet_width // item_width
        sprites_per_col = sheet_height // item_height
        
        # Calculate position in spritesheet
        if image_index >= sprites_per_row * sprites_per_col:
            print(f"  Index {image_index} out of range for spritesheet {image_file}")
            continue
        
        x = (image_index % sprites_per_row) * item_width
        y = (image_index // sprites_per_row) * item_height
        
        # Extract the sprite
        sprite = spritesheet.crop((x, y, x + item_width, y + item_height))
        
        # Scale 8x using nearest neighbor interpolation
        scaled_sprite = sprite.resize((item_width * 8, item_height * 8), Image.NEAREST)
        
        # Convert item name to snake_case for filename
        safe_item_name = re.sub('([a-z0-9])([A-Z])', r'\1_\2', item_name).lower()
        
        # Determine filename based on spritesheet name and item name
        sheet_name = os.path.splitext(os.path.basename(image_file))[0]
        sprite_filename = f"{safe_item_name}_{sheet_name}.png"
        output_filename = os.path.join(images_dir, sprite_filename)
        
        # Save the sprite
        scaled_sprite.save(output_filename)
        
        print(f"  Extracted: {item_name} (index {image_index}) from {image_file} -> {sprite_filename}")
        
        # Try to add to wiki page if it exists
        add_sprite_to_wiki_page(safe_item_name, wiki_dir, sprite_filename)
        
        extracted_count += 1
    
    print(f"Extracted {extracted_count} custom item sprites")


def find_java_files_with_image_and_imageFile():
    """
    Find all Java files that set both imageFile and image properties
    """
    java_dir = "RemixedDungeon/src/main/java"
    matches = []
    
    # Walk through Java directory to find all Java files
    for root, dirs, files in os.walk(java_dir):
        for file in files:
            if file.endswith('.java'):
                file_path = os.path.join(root, file)
                
                with open(file_path, 'r', encoding='utf-8') as f:
                    content = f.read()
                
                # Look for both imageFile and image properties in the same file
                image_file_match = re.search(r'imageFile\s*=\s*["\']([^"\']+)["\']', content)
                image_match = re.search(r'image\s*=\s*(\d+)', content)
                
                if image_file_match and image_match:
                    image_file = image_file_match.group(1)
                    image_index = int(image_match.group(1))
                    
                    # Extract class name from file
                    class_name = file[:-5]  # Remove .java extension
                    
                    matches.append((file_path, class_name, image_file, image_index))
    
    return matches


def add_sprite_to_wiki_page(item_name, wiki_dir, sprite_filename):
    """Add the sprite to the corresponding wiki page if it exists"""
    # Convert sprite filename to just the item name for wiki lookup
    # Remove suffix like "_armor.png", "_potions.png", etc.
    base_name = sprite_filename.replace('_armor.png', '').replace('_potions.png', '') \
        .replace('_rings.png', '').replace('_wands.png', '').replace('_swords.png', '') \
        .replace('_seeds.png', '').replace('_shrooms.png', '').replace('_food.png', '') \
        .replace('_artifacts.png', '').replace('_daggers.png', '').replace('_shields.png', '') \
        .replace('_ammo.png', '').replace('_books.png', '').replace('_drinks.png', '') \
        .replace('_ranged.png', '').replace('_polearms.png', '').replace('_gnoll_tomahawks.png', '') \
        .replace('_kusarigama.png', '').replace('_chaosarmor.png', '').replace('_chaosbow.png', '') \
        .replace('_chaosshield.png', '').replace('_chaosstaff.png', '').replace('_chaossword.png', '') \
        .replace('_candle.png', '').replace('_accessories.png', '').replace('_gold.png', '') \
        .replace('_vials.png', '').replace('_materials.png', '').replace('_objects.png', '') \
        .replace('_bags.png', '').replace('_scrolls.png', '').replace('_scrolls2.png', '') \
        .replace('_mastery_items.png', '').replace('_sprite.png', '')
    
    wiki_page_path = os.path.join(wiki_dir, f"{base_name}.txt")
    
    # Check if wiki page exists
    if os.path.exists(wiki_page_path):
        # Read existing content
        with open(wiki_page_path, 'r', encoding='utf-8') as f:
            content = f.read()
        
        # Check if image is already added
        image_tag = f"{{:rpd:images:{sprite_filename}?200|}}"
        if image_tag in content:
            print(f"    Image already exists in wiki page: {wiki_page_path}")
        else:
            # Add image at the beginning of the content
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
            
            print(f"    Added image to wiki page: {wiki_page_path}")


if __name__ == "__main__":
    extract_all_item_sprites()