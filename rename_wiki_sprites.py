#!/usr/bin/env python3
"""
Script to rename processed sprites according to the wiki naming scheme
and copy them to the wiki images directory.
"""
import os
import re
import shutil

def rename_and_copy_sprites(processed_dir, wiki_images_dir):
    """
    Rename processed sprites according to wiki naming scheme and copy to wiki directory
    """
    # Create mapping from old names to new names based on the wiki naming scheme
    for filename in os.listdir(processed_dir):
        if not filename.endswith('.png'):
            continue
            
        old_path = os.path.join(processed_dir, filename)
        
        # Parse the type and name from the filename
        if filename.startswith('mob_'):
            # mob_Name.png -> namesprite.png (e.g., mob_Tengu.png -> tengu_sprite.png)
            name = filename[4:-4]  # Remove 'mob_' prefix and '.png' suffix
            new_name = name.lower() + '_sprite.png'
        elif filename.startswith('item_'):
            # item_Name.png -> namesprite.png or name_item.png depending on pattern
            name = filename[5:-4]  # Remove 'item_' prefix and '.png' suffix
            # Special case: some items end up as _item.png like tengu_liver_item.png
            # For consistency with spell patterns, let's use the _item.png pattern for items
            new_name = name.lower() + '_sprite.png'
            # However, looking at the actual wiki patterns, it's more complex
            # Let's follow the observed patterns: some items are _item, some are _sprite
            # For now, let's use _sprite as that's more common for items
        elif filename.startswith('spell_'):
            # spell_Name.png -> name_spell_icon.png (e.g., spell_Heal.png -> heal_spell_icon.png)
            name = filename[6:-4]  # Remove 'spell_' prefix and '.png' suffix
            new_name = name.lower() + '_spell_icon.png'
        elif filename.startswith('buff_'):
            # buff_Name.png -> namesprite.png (e.g., buff_Burning.png -> burning_sprite.png)
            name = filename[5:-4]  # Remove 'buff_' prefix and '.png' suffix
            new_name = name.lower() + '_sprite.png'
        elif filename.startswith('hero_'):
            # hero_Name.png -> namesprite.png (e.g., hero_WARRIOR.png -> warrior_sprite.png)
            name = filename[5:-4]  # Remove 'hero_' prefix and '.png' suffix
            # Hero names might have underscores like "WARRIOR_GLADIATOR"
            new_name = re.sub(r'([A-Z])', lambda m: m.group(1).lower(), name) + '_sprite.png'
        else:
            # Unknown type, just copy as-is with lowercase
            new_name = filename.lower()
        
        # Handle special cases based on the examples in the wiki
        # Some items have specific patterns like 'tengu_liver_item.png'
        if filename == 'item_TenguLiver.png':
            new_name = 'tengu_liver_item.png'
        
        # Replace any remaining uppercase letters after underscores for consistency
        new_name = new_name.replace('Aether', 'aether').replace('Of', 'of').replace('To', 'to')
        new_name = re.sub(r'([A-Z])', lambda m: m.group(1).lower(), new_name)
        
        # Handle special case where there are capital letters in the middle (like composite names)
        # For example: IceElemental should become ice_elemental
        if '_' not in new_name and new_name.endswith('_sprite.png'):
            name_part = new_name[:-11]  # Remove '_sprite.png'
            # Insert underscores before capital letters (except the first one)
            new_name_part = re.sub('([a-z0-9])([A-Z])', r'\1_\2', name_part).lower()
            new_name = new_name_part + '_sprite.png'
        elif '_' not in new_name and new_name.endswith('_spell_icon.png'):
            name_part = new_name[:-15]  # Remove '_spell_icon.png'
            # Insert underscores before capital letters (except the first one)
            new_name_part = re.sub('([a-z0-9])([A-Z])', r'\1_\2', name_part).lower()
            new_name = new_name_part + '_spell_icon.png'
        
        new_path = os.path.join(wiki_images_dir, new_name)
        print(f"Renaming: {filename} -> {new_name}")
        
        # Copy the file to the new location with the new name
        shutil.copy2(old_path, new_path)

if __name__ == "__main__":
    processed_dir = "/home/mike/StudioProjects/remixed-dungeon/processed_wiki_sprites"
    wiki_images_dir = "/home/mike/StudioProjects/remixed-dungeon/wiki-data/media/rpd/images"
    
    rename_and_copy_sprites(processed_dir, wiki_images_dir)