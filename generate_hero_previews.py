#!/usr/bin/env python3
"""
Script to generate previews for hero classes and subclasses for Remixed Dungeon wiki.

This script extracts information about hero classes and subclasses from the source code
and creates preview data for wiki documentation.
"""

import json
import os
import re
from pathlib import Path

def extract_hero_classes_info(java_file_path):
    """Extract hero class information from HeroClass.java"""
    with open(java_file_path, 'r', encoding='utf-8') as f:
        content = f.read()

    # Find the section between the enum declaration and the first method
    lines = content.split('\n')

    # Find the start of the enum values
    start_idx = -1
    for i, line in enumerate(lines):
        if 'enum HeroClass implements CharModifier {' in line:
            start_idx = i + 1  # Skip the enum declaration line
            break

    if start_idx == -1:
        return []

    # Find the end by looking for the first line that starts with methods (has parameters)
    end_idx = len(lines)
    for i in range(start_idx, len(lines)):
        line = lines[i].strip()
        # Stop when we hit the first method definition (after the enum values)
        if (line.startswith('private') or line.startswith('static') or line.startswith('public') or
            'void ' in line or ' int ' in line or ' String ' in line or ' class ' in line or
            ' private final ' in line or line.startswith('}') or line.startswith('private final')):
            end_idx = i
            break
        # Also stop when we find the closing bracket
        if line.startswith('}'):
            end_idx = i
            break

    # Extract enum values from this range
    hero_classes = []
    for i in range(start_idx, end_idx):
        line = lines[i].strip()
        # Look for lines that look like enum values: NAME(...),
        # These lines typically start with an uppercase word followed by parentheses and end with comma or semicolon
        if '(' in line and (',' in line or line.endswith(';')):
            # Extract the enum name (first word)
            enum_name = line.split('(')[0].strip()
            if enum_name.isupper() and enum_name != 'NONE' and enum_name not in hero_classes:
                hero_classes.append(enum_name)

    return hero_classes

def extract_hero_subclasses_info(java_file_path):
    """Extract hero subclass information from HeroSubClass.java"""
    with open(java_file_path, 'r', encoding='utf-8') as f:
        content = f.read()

    # Find the section between the enum declaration and the first method
    lines = content.split('\n')

    # Find the start of the enum values
    start_idx = -1
    for i, line in enumerate(lines):
        if 'enum HeroSubClass implements CharModifier {' in line:
            start_idx = i + 1  # Skip the enum declaration line
            break

    if start_idx == -1:
        return []

    # Find the end by looking for the first line that starts with methods (has parameters)
    end_idx = len(lines)
    for i in range(start_idx, len(lines)):
        line = lines[i].strip()
        # Stop when we hit the first method definition (after the enum values)
        if line.startswith('private') or line.startswith('static') or line.startswith('public') or 'void' in line or 'int' in line or 'String' in line or 'class' in line or 'private final' in line:
            end_idx = i
            break
        # Also stop when we find the closing bracket
        if line.startswith('}'):
            end_idx = i
            break

    # Extract enum values from this range
    hero_subclasses = []
    for i in range(start_idx, end_idx):
        line = lines[i].strip()
        # Look for lines that look like enum values: NAME(...),
        # These lines typically start with an uppercase word followed by parentheses
        if '(' in line and ',' in line or line.endswith(';'):
            # Extract the enum name (first word)
            enum_name = line.split('(')[0].strip()
            if enum_name.isupper() and enum_name != 'NONE' and enum_name not in hero_subclasses:
                hero_subclasses.append(enum_name)

    return hero_subclasses

def get_hero_class_details(hero_class_name, init_heroes_json):
    """Get details about a specific hero class from initHeroes.json"""
    if hero_class_name in init_heroes_json:
        return init_heroes_json[hero_class_name]
    return {}

def get_hero_subclass_details(hero_subclass_name, init_heroes_json):
    """Get details about a specific hero subclass from initHeroes.json"""
    if hero_subclass_name in init_heroes_json:
        return init_heroes_json[hero_subclass_name]
    return {}

def generate_hero_class_preview(hero_class_name, hero_details):
    """Generate wiki preview for a hero class"""
    preview = f"""====== {hero_class_name.title()} ======

===== Description =====
The {hero_class_name.title()} is one of the starting hero classes in Remixed Dungeon. This class has unique abilities and starting equipment that set it apart from other classes.

===== Starting Equipment =====
"""
    
    # Add starting weapon if available
    if 'weapon' in hero_details:
        weapon = hero_details['weapon']
        preview += f"  * Weapon: {weapon.get('kind', 'Unknown')}"
        if weapon.get('level', 0) > 0:
            preview += f" (+{weapon['level']})"
        if weapon.get('identified', False):
            preview += " [Identified]"
        preview += "\n"
    
    # Add starting armor if available
    if 'armor' in hero_details:
        armor = hero_details['armor']
        preview += f"  * Armor: {armor.get('kind', 'Unknown')}"
        if armor.get('level', 0) > 0:
            preview += f" (+{armor['level']})"
        if armor.get('identified', False):
            preview += " [Identified]"
        preview += "\n"
    
    # Add starting items if available
    if 'items' in hero_details:
        for item in hero_details['items']:
            preview += f"  * Item: {item.get('kind', 'Unknown')}"
            if item.get('quantity', 1) > 1:
                preview += f" x{item['quantity']}"
            if item.get('identified', False):
                preview += " [Identified]"
            preview += "\n"
    
    # Add stats if available
    if 'str' in hero_details:
        preview += f"\n===== Stats =====\n"
        preview += f"  * Starting Strength: {hero_details['str']}\n"
    
    if 'hp' in hero_details:
        preview += f"  * Starting HP: {hero_details['hp']}\n"
    
    if 'magicAffinity' in hero_details:
        preview += f"  * Magic Affinity: {hero_details['magicAffinity']}\n"
    
    # Add quickslot information if available
    if 'quickslot' in hero_details:
        preview += f"\n===== Quickslot =====\n"
        for i, slot in enumerate(hero_details['quickslot'], 1):
            if 'kind' in slot:
                preview += f"  * Quickslot {i}: {slot['kind']}\n"
            elif 'spell' in slot:
                preview += f"  * Quickslot {i}: Spell - {slot['spell']}\n"
    
    # Add immunities and resistances if available
    if 'immunities' in hero_details or 'resistances' in hero_details:
        preview += f"\n===== Resistances/Immunities =====\n"
        if 'immunities' in hero_details:
            preview += f"  * Immunities: {', '.join(hero_details['immunities'])}\n"
        if 'resistances' in hero_details:
            preview += f"  * Resistances: {', '.join(hero_details['resistances'])}\n"
    
    preview += f"\n===== Special Abilities =====\n"
    preview += f"The {hero_class_name.title()} has special abilities and mechanics unique to this class. For more information, see the game mechanics.\n"
    
    preview += f"\n{{{{tag> rpd hero_class {hero_class_name.lower()} }}}}\n"
    
    return preview

def generate_hero_subclass_preview(hero_subclass_name, subclass_details):
    """Generate wiki preview for a hero subclass (mastery)"""
    # Format the subclass name to be more readable (e.g., "Battlemage" instead of "BATTLEMAGE")
    formatted_name = ' '.join(word.capitalize() for word in hero_subclass_name.lower().split('_'))
    
    preview = f"""====== {formatted_name} ======

===== Description =====
The {formatted_name} is a mastery path available for certain hero classes in Remixed Dungeon. This subclass offers specialized abilities and mechanics.

===== Abilities =====
The {formatted_name} subclass provides unique abilities and bonuses:

"""
    
    # Add subclass-specific information if available
    if 'immunities' in subclass_details:
        preview += f"  * Immunities: {', '.join(subclass_details['immunities'])}\n"
    
    if 'resistances' in subclass_details:
        preview += f"  * Resistances: {', '.join(subclass_details['resistances'])}\n"
    
    # Common attack/defense proc descriptions for different subclasses
    subclass_descriptions = {
        'GLADIATOR': "The Gladiator gains combo attacks when using melee weapons, building up a combo that increases damage.",
        'BERSERKER': "The Berserker enters a fury state when below 50% health, gaining increased damage.",
        'WARLOCK': "The Warlock gains health when using wands, with each zap potentially healing the user.",
        'BATTLEMAGE': "The Battle Mage charges their wand with each hit when using a wand and melee weapon together, and gains bonus damage based on wand charges.",
        'ASSASSIN': "The Assassin gains bonus damage for attacks from stealth or invisibility.",
        'FREERUNNER': "The FreeRunner moves faster and gains armor when not starving.",
        'SNIPER': "The Sniper marks enemies hit by ranged weapons, causing them to take more damage from all sources.",
        'WARDEN': "The Warden has increased dew collection and may gain special benefits from nature-based items.",
        'SCOUT': "The Scout moves faster than other subclasses.",
        'SHAMAN': "The Shaman can trigger additional effects when using wands, sometimes causing secondary effects.",
        'LICH': "The Lich transforms from a Necromancer after achieving mastery, gaining powerful undead abilities.",
        'WITCHDOCTOR': "The Witchdoctor creates a mana shield when casting spells.",
        'GUARDIAN': "The Guardian reduces damage when using shields in either hand."
    }
    
    if hero_subclass_name in subclass_descriptions:
        preview += f"\n{subclass_descriptions[hero_subclass_name]}\n"
    else:
        preview += f"The {formatted_name} subclass has special mechanics and abilities that differentiate it from other subclasses.\n"
    
    preview += f"\n===== Unlocking =====\n"
    preview += f"The {formatted_name} subclass is unlocked by achieving Mastery with the appropriate class by using the Tome of Mastery. This is typically done by reaching level 10 with the base class.\n"
    
    preview += f"\n{{{{tag> rpd hero_subclass {hero_subclass_name.lower()} }}}}\n"
    
    return preview

def generate_sprite_info(hero_class_name, hero_subclass_name=None):
    """Generate information about the sprite for a hero class/subclass"""
    # In the actual system, sprites are constructed dynamically in ModernHeroSpriteDef
    # For now, we'll document the expected sprite structure
    class_descriptor = f"{hero_class_name}_{hero_subclass_name if hero_subclass_name else 'NONE'}"
    
    sprite_info = f"""
===== Sprite Information =====
The {hero_class_name} sprite (with {hero_subclass_name or 'no subclass'}) is constructed using the modern sprite system with multiple layers:

  * Head: hero_modern/head/{class_descriptor}.png
  * Body: hero_modern/body/[body_type].png (based on class/subclass)
  * Hair: hero_modern/head/hair/{class_descriptor}_HAIR.png (if applicable)
  * Facial Hair: hero_modern/head/facial_hair/{class_descriptor}_FACIAL_HAIR.png (if applicable)
  * Armor: hero_modern/armor/[armor_visual_name].png
  * Accessories: hero_modern/accessories/[accessory_visual_name].png (if applicable)

The sprite system combines multiple layers to create the final hero appearance, with different layers for body parts, equipment, and accessories.
"""
    
    return sprite_info

def main():
    # Define paths
    base_path = Path('/home/mike/StudioProjects/remixed-dungeon')
    hero_class_file = base_path / 'RemixedDungeon/src/main/java/com/watabou/pixeldungeon/actors/hero/HeroClass.java'
    hero_subclass_file = base_path / 'RemixedDungeon/src/main/java/com/watabou/pixeldungeon/actors/hero/HeroSubClass.java'
    init_heroes_file = base_path / 'RemixedDungeon/src/main/assets/hero/initHeroes.json'
    
    # Extract information
    hero_classes = extract_hero_classes_info(hero_class_file)
    hero_subclasses = extract_hero_subclasses_info(hero_subclass_file)
    
    # Load initHeroes.json for detailed information
    with open(init_heroes_file, 'r', encoding='utf-8') as f:
        init_heroes = json.load(f)
    
    print(f"Found {len(hero_classes)} hero classes and {len(hero_subclasses)} hero subclasses")
    print(f"Hero classes: {hero_classes}")
    print(f"Hero subclasses: {hero_subclasses}")
    
    # Generate previews for hero classes
    for hero_class in hero_classes:
        print(f"Generating preview for hero class: {hero_class}")
        hero_details = get_hero_class_details(hero_class, init_heroes)
        preview_content = generate_hero_class_preview(hero_class, hero_details)
        
        # Add sprite info
        preview_content += generate_sprite_info(hero_class)
        
        # Write to file
        output_file = f"hero_previews/{hero_class.lower()}.txt"
        os.makedirs(os.path.dirname(output_file), exist_ok=True)
        with open(output_file, 'w', encoding='utf-8') as f:
            f.write(preview_content)
        print(f"  - Wrote to {output_file}")
    
    # Generate previews for hero subclasses
    for hero_subclass in hero_subclasses:
        print(f"Generating preview for hero subclass: {hero_subclass}")
        subclass_details = get_hero_subclass_details(hero_subclass, init_heroes)
        preview_content = generate_hero_subclass_preview(hero_subclass, subclass_details)
        
        # For subclasses, we need to know which classes can take them
        # This information isn't directly available in initHeroes.json, so we'll add general info
        subclass_content_classes = {
            'GLADIATOR': ['WARRIOR'],
            'BERSERKER': ['WARRIOR'],
            'WARLOCK': ['MAGE'],
            'BATTLEMAGE': ['MAGE'],
            'ASSASSIN': ['ROGUE'],
            'FREERUNNER': ['ROGUE'],
            'SNIPER': ['HUNTRESS'],
            'WARDEN': ['HUNTRESS'],
            'SCOUT': ['ELF'],
            'SHAMAN': ['ELF'],
            'LICH': ['NECROMANCER'],
            'WITCHDOCTOR': ['GNOLL'],
            'GUARDIAN': ['PRIEST']
        }
        
        if hero_subclass in subclass_content_classes:
            classes = subclass_content_classes[hero_subclass]
            preview_content = f"{{{{tag> rpd hero_subclass {hero_subclass.lower()} }}}}\n\n" + \
                             f"This subclass is available for the following class(es): {', '.join(classes)}.\n\n" + \
                             preview_content
        
        # Add sprite info for subclasses
        # For now, we'll just note that subclasses share the same base sprite system as their class
        preview_content += f"\n===== Sprite Information =====\n"
        preview_content += f"The {hero_subclass} subclass uses the same sprite system as its base class, with visual differences potentially appearing in some custom assets.\n"
        
        # Write to file
        output_file = f"hero_previews/{hero_subclass.lower()}.txt"
        os.makedirs(os.path.dirname(output_file), exist_ok=True)
        with open(output_file, 'w', encoding='utf-8') as f:
            f.write(preview_content)
        print(f"  - Wrote to {output_file}")
    
    print("\nPreviews generated successfully in the 'hero_previews' directory!")

if __name__ == "__main__":
    main()