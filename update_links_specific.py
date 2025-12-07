#!/usr/bin/env python3
"""
Script to update internal wiki links from capitalized to lowercase format.
Based on the specific mapping of files that were merged.
"""
import os
import re
from pathlib import Path

def get_file_mapping():
    """Return a mapping of old capitalized filenames to new lowercase filenames"""
    # This mapping is based on the merges we just performed
    mapping = {
        # The capitalized versions that were removed, mapped to their lowercase equivalents
        'Sacrificial_Sword': 'sacrificial_sword',
        'Priest': 'priest',
        'Huntress': 'huntress',
        'Chaos_Crystal': 'chaos_crystal',
        'Tengu': 'tengu',
        'Gnoll_Hero': 'gnoll_hero',
        'Chaos_Staff': 'chaos_staff',
        'Chaos_Armor': 'chaos_armor',
        'Gnoll_Tomahawk': 'gnoll_tomahawk',
        'Ration': 'ration',
        'Kusarigama': 'kusarigama',
        'Ice_Guardian_Core': 'ice_guardian_core',
        'The_Soulbringer': 'the_soulbringer',
        'Mana': 'mana',
        'Tengu_Liver': 'tengu_liver',
        'King': 'king',
        'Spider_Nest': 'spider_nest',
        'Rotten_Food': 'rotten_food',
        'Necromancer': 'necromancer',
        'Warrior': 'warrior',
        'Giant_Rat_Skull': 'giant_rat_skull',
        'Elf': 'elf',
        'Spider_Queen': 'spider_queen',
        'Spell_Book': 'spell_book',
        'Yog': 'yog',
        'Chaos_Bow': 'chaos_bow',
        'Plague_Doctor': 'plague_doctor',
        'Spider_Egg': 'spider_egg',
        'Air_Elemental': 'air_elemental',
        'Seeds': 'seeds',
        'Spider_Servant': 'spider_servant',
        'Earth_Elemental': 'earth_elemental',
        'Arcane_Stylus': 'arcane_stylus',
        'Potion_of_Paralytic_Gas': 'potion_of_paralytic_gas',
        'Bosses': 'bosses',
        'Mage': 'mage',
        'Heart_of_Darkness': 'heart_of_darkness',
        'Goo': 'goo',
        'DM300': 'dm300',
        'Tome_of_Knowledge': 'tome_of_knowledge',
        'Elementals': 'elementals',
        'Blank_Scroll': 'blank_scroll',
        'Shadow': 'shadow',
        'Spiders': 'spiders',
        'Chaos_Shield': 'chaos_shield',
        'Wraith': 'wraith',
        'Pasty-Mimic': 'pasty-mimic',
        'Skeleton': 'skeleton',
        'Playable_Class': 'playable_class',
        'Mana_Potion': 'mana_potion',
        'Candle_of_Visions': 'candle_of_visions',
        'Chaos_Sword': 'chaos_sword',
        'Ice_Key': 'ice_key',
        'Ring_of_Stone_Blood': 'ring_of_stone_blood',
        'Water_Elemental': 'water_elemental',
        'Potion_of_Toxic_Gas': 'potion_of_toxic_gas',
        'Dried_Rose': 'dried_rose',
        'Lich': 'lich',
        'Rogue': 'rogue',
        'Undead': 'undead',
        'Shadow_Lord': 'shadow_lord',
        'Hedgehog': 'hedgehog',
    }
    
    # Also add reversed namespace versions for rpd namespace
    full_mapping = {}
    for old, new in mapping.items():
        # Add both direct page and namespace:page versions
        full_mapping[old] = new
        full_mapping[f"rpd:{old}"] = f"rpd:{new}"
    
    return full_mapping

def update_links_in_file(file_path, mapping):
    """Update links in a single wiki file"""
    with open(file_path, 'r', encoding='utf-8') as f:
        content = f.read()
    
    original_content = content
    
    # Find all wiki links [[namespace:page|display]] or [[page|display]] or [[page]]
    # This regex matches the wiki link format
    link_pattern = r'\[\[([^\]]+)\]\]'
    
    def replace_link(match):
        link_content = match.group(1)
        if '|' in link_content:
            target, display_text = link_content.split('|', 1)
            target = target.strip()
            display_text = display_text.strip()
        else:
            target = link_content.strip()
            display_text = target
        
        # Check if this is an internal link (doesn't start with http/https/wiki/doku)
        if not (target.lower().startswith('http://') or target.lower().startswith('https://') or 
                target.startswith('wiki:') or target.startswith('doku>') or target.startswith(':') or
                target.startswith('playground:') or target.startswith('some:')):
            
            # Check if this target exists in our mapping
            if target in mapping:
                new_target = mapping[target]
                return f"[[{new_target}|{display_text}]]"
            elif target in mapping.values():
                # Already correct
                pass
        
        return match.group(0)
    
    # Replace links in the content
    updated_content = re.sub(link_pattern, replace_link, content)
    
    # Write back if content changed
    if original_content != updated_content:
        with open(file_path, 'w', encoding='utf-8') as f:
            f.write(updated_content)
        return True
    
    return False

def main():
    mapping = get_file_mapping()
    
    # Get all wiki directories
    wiki_dirs = [Path('wiki-data/pages/rpd')]
    
    # Also check other language directories
    for lang_dir in Path('wiki-data/pages').glob('*/'):
        if lang_dir.is_dir() and lang_dir.name != 'wiki':
            rpd_subdir = lang_dir / 'rpd'
            if rpd_subdir.exists():
                wiki_dirs.append(rpd_subdir)
    
    updated_file_count = 0
    
    print("Updating internal wiki links from capitalized to lowercase format...")
    print(f"Using mapping for {len(mapping)} page name changes")
    
    for wiki_dir in wiki_dirs:
        print(f"Processing {wiki_dir}...")
        
        # Find all txt files in this directory
        all_files = list(wiki_dir.glob('*.txt'))
        
        for file_path in all_files:
            # Check if file needs updating
            if update_links_in_file(file_path, mapping):
                print(f"  Updated links in {file_path.name}")
                updated_file_count += 1
    
    print(f"\nUpdated links in {updated_file_count} files")
    print("All internal wiki links now use lowercase naming convention.")

if __name__ == "__main__":
    main()