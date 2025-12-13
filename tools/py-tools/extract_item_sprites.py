#!/usr/bin/env python3
"""
Script to extract item sprites from items.png spritesheet
and add them to wiki pages based on their image index.
"""

import os
import re
from PIL import Image
import subprocess
from pathlib import Path


def extract_item_sprites():
    """
    Extract item sprites from items.png and add to wiki pages
    """
    # Define directories
    assets_dir = "RemixedDungeon/src/main/assets"
    wiki_dir = "wiki-data/pages/rpd"
    
    # Load the items.png spritesheet
    items_sheet_path = os.path.join(assets_dir, "items.png")
    if not os.path.exists(items_sheet_path):
        print("items.png not found in main assets directory, checking other locations...")
        items_sheet_path = os.path.join(assets_dir, "items", "items.png")
        if not os.path.exists(items_sheet_path):
            print("items.png spritesheet not found!")
            return
        else:
            print(f"Found items.png at: {items_sheet_path}")
    else:
        print(f"Found items.png at: {items_sheet_path}")
    
    # Load the spritesheet
    try:
        spritesheet = Image.open(items_sheet_path)
    except Exception as e:
        print(f"Error loading spritesheet: {e}")
        return
    
    sheet_width, sheet_height = spritesheet.size
    print(f"Items spritesheet size: {sheet_width}x{sheet_height}")
    
    # Items are arranged in 16x16 pixel grid
    item_width = 16
    item_height = 16
    sprites_per_row = sheet_width // item_width
    sprites_per_col = sheet_height // item_height
    
    print(f"Grid: {sprites_per_row} x {sprites_per_col} sprites")
    
    # Create output directory for item images
    images_dir = os.path.join(wiki_dir, 'images')
    os.makedirs(images_dir, exist_ok=True)
    
    # Define a mapping of ItemSpriteSheet constants to item names
    # For this script, I'll extract all possible sprites and name them by index
    # In a real implementation, you'd map these to actual item names
    
    # First, let's find all Java item files to get the mapping
    item_java_files = find_item_java_files()
    
    # Create a mapping of indices to item names by parsing Java files
    index_to_item_name = map_item_indices_to_names(item_java_files)
    
    # Extract sprites based on known indices in ItemSpriteSheet
    extracted_count = 0
    
    # We'll extract sprites for indices that are commonly used for actual items
    # based on the constants in ItemSpriteSheet.java
    used_indices = set(index_to_item_name.keys())
    
    # Also include some common item indices based on the ItemSpriteSheet
    common_item_indices = set()
    
    # Parse ItemSpriteSheet.java to get all constants and their values
    sprite_sheet_file = "RemixedDungeon/src/main/java/com/watabou/pixeldungeon/sprites/ItemSpriteSheet.java"
    if os.path.exists(sprite_sheet_file):
        common_item_indices = get_item_indices_from_spritesheet(sprite_sheet_file)
        used_indices.update(common_item_indices)
    
    for index in sorted(used_indices):
        if index < sprites_per_row * sprites_per_col:  # Make sure index is valid
            # Calculate position in spritesheet
            x = (index % sprites_per_row) * item_width
            y = (index // sprites_per_row) * item_height
            
            # Extract the sprite
            sprite = spritesheet.crop((x, y, x + item_width, y + item_height))
            
            # Scale 8x using nearest neighbor interpolation
            scaled_sprite = sprite.resize((item_width * 8, item_height * 8), Image.NEAREST)
            
            # Determine filename based on item name or index
            if index in index_to_item_name:
                item_name = index_to_item_name[index].lower()
                # Convert camelCase to snake_case
                item_name = re.sub('([a-z0-9])([A-Z])', r'\1_\2', item_name).lower()
            else:
                item_name = f"item_{index:03d}"
            
            # Save the sprite
            output_filename = os.path.join(images_dir, f"{item_name}_sprite.png")
            scaled_sprite.save(output_filename)
            
            print(f"Extracted sprite: {item_name} (index {index}) -> {output_filename}")
            
            # Try to add to wiki page if it exists
            add_sprite_to_wiki_page(item_name, wiki_dir, index)
            
            extracted_count += 1
    
    print(f"Extracted {extracted_count} item sprites")


def find_item_java_files():
    """Find all Java files in the items directory"""
    item_files = []
    items_dir = "RemixedDungeon/src/main/java/com/watabou/pixeldungeon/items"
    
    for root, dirs, files in os.walk(items_dir):
        for file in files:
            if file.endswith('.java') and file != 'ItemSpritesDescription.java' and file != 'ItemSpriteSheet.java':
                item_files.append(os.path.join(root, file))
    
    return item_files


def map_item_indices_to_names(item_java_files):
    """Parse Java files to map image indices to item names"""
    index_to_name = {}
    
    for file_path in item_java_files:
        with open(file_path, 'r', encoding='utf-8') as f:
            content = f.read()
        
        # Look for 'image = ItemSpriteSheet.*' pattern
        import re
        matches = re.findall(r'image\s*=\s*ItemSpriteSheet\.(\w+)', content)
        
        if matches:
            for match in matches:
                # Find the corresponding constant value
                constant_value = get_constant_value(match)
                if constant_value is not None:
                    # Get the class name from the file path
                    class_name = os.path.basename(file_path)[:-5]  # Remove .java extension
                    index_to_name[constant_value] = class_name
    
    return index_to_name


def get_constant_value(constant_name):
    """Get the integer value of an ItemSpriteSheet constant"""
    sprite_sheet_file = "RemixedDungeon/src/main/java/com/watabou/pixeldungeon/sprites/ItemSpriteSheet.java"
    
    if not os.path.exists(sprite_sheet_file):
        return None
    
    with open(sprite_sheet_file, 'r', encoding='utf-8') as f:
        content = f.read()
    
    # Look for the constant declaration
    pattern = rf'public static final int {constant_name}\s*=\s*(\d+)'
    match = re.search(pattern, content)
    
    if match:
        return int(match.group(1))
    
    return None


def get_item_indices_from_spritesheet(sprite_sheet_file):
    """Extract all item indices from ItemSpriteSheet.java"""
    indices = set()
    
    with open(sprite_sheet_file, 'r', encoding='utf-8') as f:
        content = f.read()
    
    # Find all 'public static final int NAME = VALUE' patterns
    matches = re.findall(r'public static final int \w+\s*=\s*(\d+)', content)
    
    for match in matches:
        indices.add(int(match))
    
    return indices


def add_sprite_to_wiki_page(item_name, wiki_dir, index):
    """Add the sprite to the corresponding wiki page if it exists"""
    # Convert item name to snake_case for wiki filename
    wiki_item_name = re.sub('([a-z0-9])([A-Z])', r'\1_\2', item_name).lower()
    
    # Special case: if the name is in the format item_###, try to find a matching page
    if wiki_item_name.startswith("item_"):
        # For generic item indices, we won't try to find a matching page
        return
    
    wiki_page_path = os.path.join(wiki_dir, f"{wiki_item_name}.txt")
    
    # Check if wiki page exists
    if os.path.exists(wiki_page_path):
        # Read existing content
        with open(wiki_page_path, 'r', encoding='utf-8') as f:
            content = f.read()
        
        # Check if image is already added
        image_tag = f"{{:rpd:images:{wiki_item_name}_sprite.png?200|}}"
        if image_tag in content:
            print(f"  Image already exists in wiki page: {wiki_page_path}")
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
            
            print(f"  Added image to wiki page: {wiki_page_path}")


if __name__ == "__main__":
    extract_item_sprites()