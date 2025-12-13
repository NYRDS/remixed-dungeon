#!/usr/bin/env python3
"""
Script to extract item sprites from multiple spritesheets
and add them to wiki pages based on their image index and file.
"""

import os
import re
from PIL import Image
import subprocess
from pathlib import Path


def extract_all_item_sprites():
    """
    Extract item sprites from multiple spritesheets and add to wiki pages
    """
    # Define directories
    assets_dir = "RemixedDungeon/src/main/assets"
    wiki_dir = "wiki-data/pages/rpd"
    
    # Define spritesheets to process and their image indices
    spritesheets = {
        "items.png": (256, 256),  # 16x16 grid
        "items/armor.png": (256, 128),  # 16x8 grid
        "items/potions.png": (256, 128),  # 16x8 grid  
        "items/rings.png": (256, 128),  # 16x8 grid
        "items/wands.png": (256, 128),  # 16x8 grid
        "items/swords.png": (256, 128),  # 16x8 grid
        "items/seeds.png": (256, 128),  # 16x8 grid
        "items/shrooms.png": (256, 128),  # 16x8 grid
        "items/food.png": (256, 128),  # 16x8 grid
        "items/artifacts.png": (256, 128),  # 16x8 grid
        "items/daggers.png": (256, 128),  # 16x8 grid
        "items/shields.png": (256, 128),  # 16x8 grid
        "items/ammo.png": (256, 128),  # 16x8 grid
        "items/books.png": (256, 128),  # 16x8 grid
        "items/drinks.png": (256, 128),  # 16x8 grid
        "items/ranged.png": (256, 128),  # 16x8 grid
        "items/polearms.png": (256, 128),  # 16x8 grid
        "items/gnoll_tomahawks.png": (256, 128),  # 16x8 grid
        "items/kusarigama.png": (256, 128),  # 16x8 grid
        "items/chaosArmor.png": (256, 128),  # 16x8 grid
        "items/chaosBow.png": (256, 128),  # 16x8 grid
        "items/chaosShield.png": (256, 128),  # 16x8 grid
        "items/chaosStaff.png": (256, 128),  # 16x8 grid
        "items/chaosSword.png": (256, 128),  # 16x8 grid
        "items/candle.png": (256, 128),  # 16x8 grid
        "items/accessories.png": (256, 128),  # 16x8 grid
        "items/gold.png": (256, 128),  # 16x8 grid
        "items/vials.png": (256, 128),  # 16x8 grid
        "items/materials.png": (256, 128),  # 16x8 grid
        "items/objects.png": (256, 128),  # 16x8 grid
        "items/bags.png": (256, 128),  # 16x8 grid
        "items/scrolls.png": (256, 128),  # 16x8 grid
        "items/scrolls2.png": (256, 128),  # 16x8 grid
        "items/mastery_items.png": (256, 128),  # 16x8 grid
    }
    
    # Create output directory for item images
    images_dir = os.path.join(wiki_dir, 'images')
    os.makedirs(images_dir, exist_ok=True)
    
    # Find all items that use specific spritesheets by parsing Java files
    item_mappings = find_item_mappings()
    
    extracted_count = 0
    
    for spritesheet_name, (width, height) in spritesheets.items():
        spritesheet_path = os.path.join(assets_dir, spritesheet_name)
        
        if not os.path.exists(spritesheet_path):
            print(f"Spritesheet not found: {spritesheet_path}")
            continue
        
        # Load the spritesheet
        try:
            spritesheet = Image.open(spritesheet_path)
            sheet_width, sheet_height = spritesheet.size
            print(f"Processing {spritesheet_name}: {sheet_width}x{sheet_height}")
        except Exception as e:
            print(f"Error loading spritesheet {spritesheet_name}: {e}")
            continue
        
        # Items are arranged in 16x16 pixel grid
        item_width = 16
        item_height = 16
        sprites_per_row = sheet_width // item_width
        sprites_per_col = sheet_height // item_height
        
        print(f"  Grid: {sprites_per_row} x {sprites_per_col} sprites")
        
        # Process items that use this spritesheet
        for item_name, (file_path, image_index) in item_mappings.items():
            if file_path == spritesheet_name:
                if image_index < sprites_per_row * sprites_per_col:  # Make sure index is valid
                    # Calculate position in spritesheet
                    x = (image_index % sprites_per_row) * item_width
                    y = (image_index // sprites_per_row) * item_height
                    
                    # Extract the sprite
                    sprite = spritesheet.crop((x, y, x + item_width, y + item_height))
                    
                    # Scale 8x using nearest neighbor interpolation
                    scaled_sprite = sprite.resize((item_width * 8, item_height * 8), Image.NEAREST)
                    
                    # Convert item name to snake_case for filename
                    safe_item_name = re.sub('([a-z0-9])([A-Z])', r'\1_\2', item_name).lower()
                    
                    # Determine filename based on spritesheet name and item name
                    # Remove extension from spritesheet name for the sprite identifier
                    sheet_name = os.path.splitext(os.path.basename(spritesheet_name))[0]
                    sprite_filename = f"{safe_item_name}_{sheet_name}.png"
                    output_filename = os.path.join(images_dir, sprite_filename)
                    
                    # Save the sprite
                    scaled_sprite.save(output_filename)
                    
                    print(f"  Extracted: {item_name} (index {image_index}) from {spritesheet_name} -> {sprite_filename}")
                    
                    # Try to add to wiki page if it exists
                    add_sprite_to_wiki_page(safe_item_name, wiki_dir, sprite_filename)
                    
                    extracted_count += 1
    
    print(f"Extracted {extracted_count} custom item sprites")


def find_item_mappings():
    """Find all items that use custom spritesheets by parsing Java files"""
    item_mappings = {}
    
    # Define common patterns for finding image settings
    patterns = [
        r'imageFile\s*=\s*["\']([^"\']+)["\']',  # For imageFile assignments
        r'image\s*=\s*(\d+)',  # For image index assignments
    ]
    
    # Look at specific Java files that we know have custom image settings
    java_dir = "RemixedDungeon/src/main/java"
    
    # Find all Java files that might set imageFile or image
    for root, dirs, files in os.walk(java_dir):
        for file in files:
            if file.endswith('.java') and not file.startswith('ItemSprite'):
                file_path = os.path.join(root, file)
                
                with open(file_path, 'r', encoding='utf-8') as f:
                    content = f.read()
                
                # Look for imageFile assignments
                image_file_match = re.search(r'imageFile\s*=\s*["\']([^"\']+)["\']', content)
                if image_file_match:
                    image_file = image_file_match.group(1)
                    
                    # Look for image index assignment in the same file
                    image_index = 0
                    image_match = re.search(r'image\s*=\s*ItemSpriteSheet\.(\w+)', content)
                    if image_match:
                        # Get the constant value
                        constant_name = image_match.group(1)
                        image_index = get_constant_value(constant_name)
                    # Alternative: look for direct assignment like "image = 5;"
                    else:
                        direct_image_match = re.search(r'image\s*=\s*(\d+)', content)
                        if direct_image_match:
                            image_index = int(direct_image_match.group(1))
                    
                    # Extract class name from file
                    class_name = file[:-5]  # Remove .java extension
                    item_mappings[class_name] = (image_file, image_index)
    
    return item_mappings


def get_constant_value(constant_name):
    """Get the integer value of an ItemSpriteSheet constant"""
    sprite_sheet_file = "RemixedDungeon/src/main/java/com/watabou/pixeldungeon/sprites/ItemSpriteSheet.java"
    
    if not os.path.exists(sprite_sheet_file):
        return 0
    
    with open(sprite_sheet_file, 'r', encoding='utf-8') as f:
        content = f.read()
    
    # Look for the constant declaration
    pattern = rf'public static final int {constant_name}\s*=\s*(\d+)'
    match = re.search(pattern, content)
    
    if match:
        return int(match.group(1))
    
    return 0


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