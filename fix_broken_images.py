#!/usr/bin/env python3
"""
Script to fix broken image references in wiki pages by mapping them to correct names
"""
import os
import re

def create_image_mapping():
    """
    Create a mapping between expected image names and actual image names
    based on the processing rules
    """
    mapping = {}
    
    # Get all image filenames in the images directory
    wiki_images_dir = "/home/mike/StudioProjects/remixed-dungeon/wiki-data/media/rpd/images"
    image_files = set(os.listdir(wiki_images_dir))
    
    # Process each file to build reverse mapping
    for img_file in image_files:
        if not img_file.endswith('.png'):
            continue
            
        # Get the base name without extension
        base_name = img_file[:-4]  # Remove .png
        
        # Try to figure out the original name based on the file structure
        if img_file.endswith('_sprite.png'):
            # This comes from mob_Name, item_Name, buff_Name, hero_Name
            original_name = base_name[:-7]  # Remove _sprite
            
            # Convert back to the expected format
            # For example, firebloom.seed -> seed_firebloom (but this doesn't match our case)
            # Actually, firebloom.seed -> firebloom.seed_sprite
            
            # Try to map from expected broken names to actual names
            # chaos_armor_armor.png (expected) -> chaosarmor_sprite.png (actual)
            if base_name.endswith('_armor'):
                # This is likely an armor item that was processed differently
                expected = base_name + '.png'
                if expected not in mapping:
                    mapping[expected] = img_file
            elif base_name.endswith('_item'):
                # This is likely an item that was processed differently
                expected = base_name + '.png'
                if expected not in mapping:
                    mapping[expected] = img_file
            else:
                # For regular sprites
                expected = original_name + '_sprite.png'  # e.g., "chaos_armor" + "_sprite.png"
                if expected not in mapping:
                    mapping[expected] = img_file                    
        elif img_file.endswith('_spell_icon.png'):
            # This comes from spell_Name
            original_name = base_name[:-11]  # Remove _spell_icon
            expected = original_name + '_spell_icon.png'  # e.g., "heal" + "_spell_icon.png"
            if expected not in mapping:
                mapping[expected] = img_file
        else:
            # For other patterns that might exist
            # Like chaos_armor_chaosArmor.png
            # These follow the pattern <name>_<category>.png
            # e.g., chaos_armor_armor.png (expected) vs <actual>.png
            # We need to handle the special cases individually
            if img_file.endswith('_armor.png'):
                # Items that follow the <itemname>_<category>.png pattern
                # chaos_armor_chaosArmor.png -> chaos_armor_armor.png (expected)
                parts = base_name.split('_')
                if len(parts) >= 2:
                    # This is like "chaos_armor_chaosArmor" -> expect "chaos_armor_armor"
                    main_name = '_'.join(parts[:-1])  # "chaos_armor"
                    expected_name = main_name + '_armor.png'  # "chaos_armor_armor.png"
                    if expected_name not in mapping:
                        mapping[expected_name] = img_file
    
    # Manually add some special mappings based on our naming patterns
    # Add the specific mappings for known problematic images
    mapping.update({
        'chaos_armor_armor.png': 'chaos_armor_chaosArmor.png',  # This already exists
        'chaos_staff.png': 'chaos_staff_chaosStaff.png',  # This already exists  
        'ice_key_item.png': 'ice_key_artifacts.png',  # This already exists
        'chaos_crystal_item.png': 'chaos_crystal_artifacts.png',  # This might exist
        'ringofelements_item.png': 'ring_of_elements_rings.png',  # Hypothetical
        'skeletonkey_item.png': 'skeletonkey_sprite.png',  # Our processed version
        'ration_item.png': 'ration_sprite.png',  # Our processed version
        'potion_paralytic_gas_item.png': 'potion_of_paralytic_gas_sprite.png',  # Our processed version
        'candle_of_visions_item.png': 'candle_of_mind_vision_candle.png',  # Maybe exists
        'dried_rose_item.png': 'driedrose_sprite.png',  # Our processed version
        'heart_of_darkness_item.png': 'heart_of_darkness_artifacts.png',  # Maybe exists
        'the_soulbringer_item.png': 'the_soulbringer_item.png',  # Find actual
        'giant_rat_skull_item.png': 'rat_skull_sprite.png',  # Maybe should be rat_skull
        'potion_of_toxic_gas_potions.png': 'potion_of_toxic_gas_sprite.png',  # Our processed version
    })
    
    # Let's build comprehensive mappings based on the processed sprites
    # First, I need to know what was processed to what
    processed_dir = "/home/mike/StudioProjects/remixed-dungeon/processed_wiki_sprites"
    if os.path.exists(processed_dir):
        for proc_file in os.listdir(processed_dir):
            if proc_file.endswith('.png'):
                # Get the original name (with prefix like mob_, item_, etc.)
                original_base = proc_file[:-4]  # Remove .png
                
                # Apply same transformation as the rename script to get expected name
                if proc_file.startswith('mob_'):
                    name = proc_file[4:-4]  # Remove 'mob_' prefix and '.png' suffix
                    expected_name = name.lower() + '_sprite.png'
                elif proc_file.startswith('item_'):
                    name = proc_file[5:-4]  # Remove 'item_' prefix and '.png' suffix
                    if proc_file == 'item_TenguLiver.png':
                        expected_name = 'tengu_liver_item.png'
                    else:
                        expected_name = name.lower() + '_sprite.png'
                elif proc_file.startswith('spell_'):
                    name = proc_file[6:-4]  # Remove 'spell_' prefix and '.png' suffix
                    expected_name = name.lower() + '_spell_icon.png'
                elif proc_file.startswith('buff_'):
                    name = proc_file[5:-4]  # Remove 'buff_' prefix and '.png' suffix
                    expected_name = name.lower() + '_sprite.png'
                elif proc_file.startswith('hero_'):
                    name = proc_file[5:-4]  # Remove 'hero_' prefix and '.png' suffix
                    import re
                    new_name = re.sub(r'([A-Z])', lambda m: m.group(1).lower(), name) + '_sprite.png'
                    # Handle camel case to snake case
                    new_name = re.sub('([a-z0-9])([A-Z])', r'\1_\2', new_name[:-11]).lower() + '_sprite.png'
                    expected_name = new_name
                else:
                    expected_name = proc_file.lower()
                
                # Handle special case where there are capital letters in the middle
                if '_' not in expected_name and expected_name.endswith('_sprite.png'):
                    name_part = expected_name[:-11]  # Remove '_sprite.png'
                    # Insert underscores before capital letters (except the first one)
                    new_name_part = re.sub('([a-z0-9])([A-Z])', r'\1_\2', name_part).lower()
                    expected_name = new_name_part + '_sprite.png'
                elif '_' not in expected_name and expected_name.endswith('_spell_icon.png'):
                    name_part = expected_name[:-15]  # Remove '_spell_icon.png'
                    # Insert underscores before capital letters (except the first one)
                    new_name_part = re.sub('([a-z0-9])([A-Z])', r'\1_\2', name_part).lower()
                    expected_name = new_name_part + '_spell_icon.png'
                
                # The actual file would be what was renamed to
                # We need to figure out what the actual name is based on the current image directory
                actual_file = None
                for img_file in image_files:
                    if img_file.endswith('.png'):
                        actual_base = img_file[:-4]
                        if expected_name[:-4] == actual_base:  # Base names match
                            actual_file = img_file
                            break
                
                # If we found the actual file, add to mapping
                if actual_file and expected_name not in mapping:
                    mapping[expected_name] = actual_file
    
    # Additional specific manual mappings based on common patterns
    # This might be needed to handle cases like seed_firebloom vs firebloom.seed
    mapping.update({
        'seed_firebloom.png': 'firebloom.seed_sprite.png',  # From our earlier example
        'short_sword_sprite.png': 'shortsword_sprite.png',  # Common weapon pattern
        'ranged_wooden_bow.png': 'wooden_bow_ranged.png',  # Could be in specific format
        'scroll_of_identify_sprite.png': 'scrollofidentify_sprite.png',  # Processed format
        'scroll_upgrade_sprite.png': 'scrollofupgrade_sprite.png',  # Processed format
    })
    
    return mapping

def fix_broken_image_refs(wiki_pages_dir, mapping):
    """
    Fix broken image references in wiki pages using the mapping
    """
    fixed_count = 0
    
    # Walk through all text files in the wiki pages directory
    for root, dirs, files in os.walk(wiki_pages_dir):
        for file in files:
            if file.endswith('.txt'):
                file_path = os.path.join(root, file)
                
                with open(file_path, 'r', encoding='utf-8') as f:
                    content = f.read()
                
                # Find all image references using regex
                image_refs = re.findall(r'\{\{\s*rpd:images:([a-zA-Z0-9_.-]+)\s*(?:\|(.*?))?\s*\}\}', content)
                
                modified = False
                for image_ref, alt_text in image_refs:
                    if image_ref not in [f for f in os.listdir("/home/mike/StudioProjects/remixed-dungeon/wiki-data/media/rpd/images")]:
                        # Check if this reference is in our mapping
                        if image_ref in mapping:
                            old_ref = f"{{{{ rpd:images:{image_ref}"
                            new_ref = f"{{{{ rpd:images:{mapping[image_ref]}"
                            
                            # Replace in content
                            content = content.replace(old_ref, new_ref)
                            modified = True
                            print(f"FIXED: {file_path} - {image_ref} -> {mapping[image_ref]}")
                            fixed_count += 1
                
                # Write back the modified content if needed
                if modified:
                    with open(file_path, 'w', encoding='utf-8') as f:
                        f.write(content)
    
    return fixed_count

if __name__ == "__main__":
    wiki_pages_dir = "/home/mike/StudioProjects/remixed-dungeon/wiki-data/pages/rpd"
    
    # Create the mapping
    mapping = create_image_mapping()
    print(f"Created mapping with {len(mapping)} entries")
    
    # Fix the broken references
    fixed_count = fix_broken_image_refs(wiki_pages_dir, mapping)
    print(f"\nFixed {fixed_count} broken image references")