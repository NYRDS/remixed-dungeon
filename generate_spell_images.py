#!/usr/bin/env python3
"""
Script to generate preview images for spells in Remixed Dungeon.
Extracts spell icons from assets and creates preview images for use in the wiki.
"""

import os

def extract_spell_icons_from_source():
    """Extract individual spell icons from sprite sheets based on the actual source code."""
    # Create output directories
    output_dir = "generated_spell_images"
    wiki_output_dir = os.path.join(output_dir, "wiki_images")
    os.makedirs(wiki_output_dir, exist_ok=True)

    # Mapping of spell names to their icon file and index based on actual source code analysis
    spell_icons = [
        # Java spells
        {"spell": "healing_spell", "file": "spell_icons.png", "index": 1},  # image = 1 in Healing.java
        {"spell": "ignite_spell", "file": "spellsIcons/elemental.png", "index": 1},  # image = 1 in Ignite.java
        {"spell": "wind_gust_spell", "file": "spellsIcons/elemental.png", "index": 0},  # From WindGust.java
        {"spell": "magic_torch_spell", "file": "spell_icons.png", "index": 0},  # image = 0 in MagicTorch.java
        {"spell": "root_spell", "file": "spellsIcons/elemental.png", "index": 2},  # From RootSpell.java
        {"spell": "freeze_globe_spell", "file": "spellsIcons/elemental.png", "index": 3},  # From FreezeGlobe.java
        {"spell": "summon_deathling_spell", "file": "spellsIcons/necromancy.png", "index": 0},  # From SummonDeathling.java

        # Lua spells - based on their actual definitions
        {"spell": "backstab_spell", "file": "spellsIcons/rogue.png", "index": 2},  # From Backstab.lua: image = 2
        {"spell": "body_armor_spell", "file": "spellsIcons/warrior.png", "index": 0},  # From BodyArmor.lua: image = 0
        {"spell": "calm_spell", "file": "spellsIcons/hunting.png", "index": 1},  # From Calm.lua: image = 1
        {"spell": "charm_spell", "file": "spellsIcons/hunting.png", "index": 2},  # From Charm.lua: image = 2
        {"spell": "cloak_spell", "file": "spellsIcons/rogue.png", "index": 0},  # From Cloak.lua: image = 0
        {"spell": "curse_item_spell", "file": "spellsIcons/necromancy.png", "index": 0},  # From CurseItem.lua: image = 0
        {"spell": "dark_sacrifice_spell", "file": "spellsIcons/necromancy.png", "index": 1},  # From DarkSacrifice.lua: image = 1
        {"spell": "dash_spell", "file": "spellsIcons/warrior.png", "index": 2},  # From Dash.lua: image = 2
        {"spell": "die_hard_spell", "file": "spellsIcons/warrior.png", "index": 1},  # From DieHard.lua: image = 1
        {"spell": "exhumation_spell", "file": "spellsIcons/necromancy.png", "index": 3},  # From Exhumation.lua: image = 3
        {"spell": "haste_spell", "file": "spellsIcons/rogue.png", "index": 3},  # From Haste.lua: image = 3
        {"spell": "heal_spell", "file": "spellsIcons/witchcraft.png", "index": 2},  # From Heal.lua: image = 2
        {"spell": "hide_in_grass_spell", "file": "spellsIcons/naturegift.png", "index": 2},  # From HideInGrass.lua: image = 2
        {"spell": "kunai_throw_spell", "file": "spellsIcons/rogue.png", "index": 3},  # From KunaiThrow.lua: image = 3
        {"spell": "lightning_bolt_spell", "file": "spellsIcons/witchcraft.png", "index": 1},  # From LightningBolt.lua: image = 1
        {"spell": "magic_arrow_spell", "file": "spellsIcons/naturegift.png", "index": 0},  # From MagicArrow.lua: image = 0
        {"spell": "nature_armor_spell", "file": "spellsIcons/naturegift.png", "index": 3},  # From NatureArmor.lua: image = 3
        {"spell": "order_spell", "file": "spellsIcons/witchcraft.png", "index": 3},  # From Order.lua: image = 3
        {"spell": "possess_spell", "file": "spellsIcons/possession.png", "index": 0},  # From Possess.lua: image = 0
        {"spell": "raise_dead_spell", "file": "spellsIcons/necromancy.png", "index": 2},  # From RaiseDead.lua: image = 2
        {"spell": "roar_spell", "file": "spellsIcons/witchcraft.png", "index": 0},  # From Roar.lua: image = 0
        {"spell": "shoot_in_eye_spell", "file": "spellsIcons/hunting.png", "index": 0},  # From ShootInEye.lua: image = 0
        {"spell": "smash_spell", "file": "spellsIcons/warrior.png", "index": 3},  # From Smash.lua: image = 3
        {"spell": "sprout_spell", "file": "spellsIcons/naturegift.png", "index": 1},  # From Sprout.lua: image = 1
        {"spell": "summon_beast_spell", "file": "spellsIcons/hunting.png", "index": 3},  # From SummonBeast.lua: image = 3
        {"spell": "test_spell", "file": "spellsIcons/common.png", "index": 1},  # From TestSpell.lua: image = 1
        {"spell": "town_portal_spell", "file": "spellsIcons/common.png", "index": 3},  # From TownPortal.lua: image = 3
    ]

    extracted_count = 0

    for spell_data in spell_icons:
        spell_name = spell_data["spell"]
        file_name = spell_data["file"]
        index = spell_data["index"]

        # Path to the sprite sheet
        sheet_path = os.path.join("RemixedDungeon/src/main/assets", file_name)

        if not os.path.exists(sheet_path):
            print(f"Warning: {sheet_path} does not exist, skipping...")
            continue

        # Get image dimensions
        cmd_result = os.popen(f"identify -format '%w:%h' '{sheet_path}' 2>/dev/null").read().strip()
        if ':' in cmd_result:
            width, height = map(int, cmd_result.split(':'))
        else:
            print(f"Error: Could not get dimensions for {sheet_path}, result: {cmd_result}")
            continue

        # Calculate sprite size - assume all sprites are 16x16 for these sheets
        if "spell_icons" in file_name or "spellsIcons" in file_name:
            sprite_width = sprite_height = 16
        else:
            sprite_width = sprite_height = 16  # Standard size

        # Calculate number of sprites in the sheet
        sprites_per_row = width // sprite_width
        sprites_per_col = height // sprite_height

        # Calculate position of the sprite to extract
        col = index % sprites_per_row
        row = index // sprites_per_row

        if row >= sprites_per_col:
            print(f"Warning: Index {index} out of range for {sheet_path}")
            continue

        # Calculate crop position
        x = col * sprite_width
        y = row * sprite_height

        # Define output file name
        output_file = os.path.join(wiki_output_dir, f"{spell_name}_icon.png")

        # Use ImageMagick to crop the individual sprite
        cmd = f"convert '{sheet_path}' -crop {sprite_width}x{sprite_height}+{x}+{y} -define png:color-type=6 '{output_file}'"
        os.system(cmd)

        print(f"Generated: {output_file} (from index {index} in {file_name} at {col},{row})")
        extracted_count += 1

    print(f"Spell preview images generated in: {wiki_output_dir}")
    print(f"Total extracted: {extracted_count} spell icons")

def main():
    print("Generating spell preview images...")
    extract_spell_icons_from_source()
    print("Spell preview image generation complete!")

if __name__ == "__main__":
    main()