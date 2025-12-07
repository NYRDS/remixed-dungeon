#!/usr/bin/env python3
"""
Script to generate preview images for spells in Remixed Dungeon.
Extracts spell icons from assets and creates preview images for use in the wiki.
"""

import os
import shutil
from typing import List

def find_spell_icon_files() -> List[str]:
    """Find all spell icon files in the assets directory."""
    icons_dir = "RemixedDungeon/src/main/assets/spellsIcons"
    icon_files = []
    
    if os.path.exists(icons_dir):
        for file in os.listdir(icons_dir):
            if file.endswith(('.png', '.jpg', '.jpeg')):
                icon_files.append(os.path.join(icons_dir, file))
    
    return icon_files

def generate_spell_preview_images():
    """Generate preview images for spells using available spell icons."""
    # Find all spell icon files
    icon_files = find_spell_icon_files()

    # Create output directories
    output_dir = "generated_spell_images"
    wiki_output_dir = os.path.join(output_dir, "wiki_images")
    os.makedirs(wiki_output_dir, exist_ok=True)

    print(f"Found {len(icon_files)} spell icon files")

    # Define mappings of icons to spells
    icon_spell_mappings = {
        "common.png": [
            "magic_torch_spell", "town_portal_spell",
            "calm_spell", "charm_spell", "cloak_spell", "order_spell",
            "curse_item_spell", "test_spell", "healing_spell"
        ],
        "witchcraft.png": [
            "heal_spell", "lightning_bolt_spell", "roar_spell", "order_spell",
            "magic_arrow_spell"
        ],
        "elemental.png": [
            "ignite_spell", "wind_gust_spell", "root_spell", "freeze_globe_spell"
        ],
        "necromancy.png": [
            "raise_dead_spell", "exhumation_spell", "dark_sacrifice_spell", "possess_spell"
        ],
        "rogue.png": [
            "backstab_spell", "cloak_spell", "kunai_throw_spell", "haste_spell"
        ],
        "hunting.png": [
            "calm_spell", "charm_spell", "shoot_in_eye_spell", "summon_beast_spell"
        ],
        "naturegift.png": [
            "sprout_spell", "hide_in_grass_spell", "nature_armor_spell"
        ],
        "ninja.png": [
            "kunai_throw_spell", "backstab_spell"
        ],
        "berserkcy.png": [
            "die_hard_spell", "smash_spell", "dash_spell"
        ],
        "warrior.png": [
            "body_armor_spell", "smash_spell"
        ],
        "possession.png": [
            "possess_spell"
        ],
        "Light.png": [
            "magic_torch_spell"
        ],
        "elemental_all.png": [
            "ignite_spell", "wind_gust_spell", "root_spell", "freeze_globe_spell"
        ]
    }

    # Track already generated images to avoid duplicates
    generated_images = set()

    # Process each icon file
    for icon_path in icon_files:
        file_name = os.path.basename(icon_path)

        # Check if we have a specific mapping for this icon
        if file_name in icon_spell_mappings:
            spell_names = icon_spell_mappings[file_name]
        else:
            # For icons not specifically mapped, use a general mapping
            file_name_no_ext = os.path.splitext(file_name)[0]
            spell_names = [f"{file_name_no_ext}_spell"]

        # Copy and rename the icon for each associated spell
        for spell_name in spell_names:
            output_file = os.path.join(wiki_output_dir, f"{spell_name}_icon.png")

            # Skip if this image was already generated from another icon
            if output_file in generated_images:
                continue

            # Use ImageMagick to copy and potentially resize/scale the image
            cmd = f"convert '{icon_path}' -resize 32x32 '{output_file}'"
            os.system(cmd)

            print(f"Generated preview image: {output_file} (from {file_name})")
            generated_images.add(output_file)

    print(f"Spell preview images generated in: {wiki_output_dir}")

def main():
    print("Generating spell preview images...")
    generate_spell_preview_images()
    print("Spell preview image generation complete!")

if __name__ == "__main__":
    main()