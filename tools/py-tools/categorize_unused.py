#!/usr/bin/env python3
"""
Script to safely identify and handle unused files in the wiki-data repository.
This script identifies files that can be safely removed vs. those that need review.
"""

import os
from pathlib import Path
import shutil

def categorize_unused_images(unused_images):
    """Categorize unused images into safe to remove vs. needs review."""
    
    safe_to_remove = []
    needs_review = []
    
    for img in unused_images:
        # Generic item sprites with numeric names like item_000_sprite.png
        if img.startswith('item_') and '_sprite.png' in img and any(c.isdigit() for c in img):
            safe_to_remove.append(img)
        # Spell icons that may be duplicates
        elif '_spell_icon.png' in img:
            # Check if there's a corresponding spell page that should reference it
            spell_name = img.replace('_spell_icon.png', '')
            # Check if we have an actual spell page with this name
            if spell_name in ['test', 'obsolete', 'curse_item', 'warrior', 'possession', 'elemental_all', 'light']:
                safe_to_remove.append(img)  # These are likely development/test files
            else:
                needs_review.append(img)
        # Generic assets that are clearly unused
        elif img in ['spell_common.png', 'obsolete_spell_icon.png']:
            safe_to_remove.append(img)
        # Everything else needs review
        else:
            needs_review.append(img)
    
    return safe_to_remove, needs_review

def main():
    # List of unused images from the previous script
    unused_images = [
        'Light_spell_icon.png',
        'accessory_accessories.png',
        'amulet_sprite.png',
        'armor_armor.png',
        'armorkit_sprite.png',
        'battleaxe_sprite.png',
        'black_skull_of_mastery_artifacts.png',
        'blade_of_souls_swords.png',
        'blankscroll_sprite.png',
        'book_books.png',
        'candle_of_mind_vision_candle.png',
        'candy_artifacts.png',
        'candy_of_death_artifacts.png',
        'chaos_armor_chaosArmor.png',
        'chaos_bow_chaosBow.png',
        'chaos_staff_chaosStaff.png',
        'chaos_sword_chaosSword.png',
        'chargrilledmeat_sprite.png',
        'claymore_swords.png',
        'corpsedust_sprite.png',
        'dagger_sprite.png',
        'darkgold_sprite.png',
        'dart_ammo.png',
        'dew_vial_vials.png',
        'dewdrop_sprite.png',
        'driedrose_sprite.png',
        'dwarftoken_sprite.png',
        'elemental_all_spell_icon.png',
        'elven_dagger_swords.png',
        'frozencarpaccio_sprite.png',
        'gnoll_tamahawk_gnoll_tomahawks.png',
        'gold_gold.png',
        'golden_sword_swords.png',
        'goldenkey_sprite.png',
        'halberd_polearms.png',
        'ice_guardian_core_module_materials.png',
        'ironkey_sprite.png',
        'item_000_sprite.png',
        'item_005_sprite.png',
        'item_006_sprite.png',
        'item_007_sprite.png',
        'item_011_sprite.png',
        'item_012_sprite.png',
        'item_013_sprite.png',
        'item_032_sprite.png',
        'item_033_sprite.png',
        'item_034_sprite.png',
        'item_035_sprite.png',
        'item_036_sprite.png',
        'item_037_sprite.png',
        'item_038_sprite.png',
        'item_039_sprite.png',
        'item_040_sprite.png',
        'item_041_sprite.png',
        'item_042_sprite.png',
        'item_043_sprite.png',
        'item_044_sprite.png',
        'item_045_sprite.png',
        'item_046_sprite.png',
        'item_047_sprite.png',
        'item_048_sprite.png',
        'item_049_sprite.png',
        'item_050_sprite.png',
        'item_051_sprite.png',
        'item_052_sprite.png',
        'item_053_sprite.png',
        'item_054_sprite.png',
        'item_055_sprite.png',
        'item_068_sprite.png',
        'item_069_sprite.png',
        'item_070_sprite.png',
        'item_071_sprite.png',
        'item_072_sprite.png',
        'item_073_sprite.png',
        'item_074_sprite.png',
        'item_075_sprite.png',
        'item_076_sprite.png',
        'item_077_sprite.png',
        'item_078_sprite.png',
        'item_079_sprite.png',
        'item_105_sprite.png',
        'item_127_sprite.png',
        'item_128_sprite.png',
        'item_129_sprite.png',
        'item_130_sprite.png',
        'item_131_sprite.png',
        'javelin_sprite.png',
        'keyring_sprite.png',
        'kind_of_bow_ranged.png',
        'knuckles_sprite.png',
        'lloydsbeacon_sprite.png',
        'longsword_sprite.png',
        'mace_sprite.png',
        'mysterymeat_sprite.png',
        'obsolete_spell_icon.png',
        'overpricedration_sprite.png',
        'pickaxe_sprite.png',
        'polearm_polearms.png',
        'possession_spell_icon.png',
        'potionbelt_sprite.png',
        'pseudopasty_sprite.png',
        'pumpkin_pie_food.png',
        'quarterstaff_sprite.png',
        'quiver_sprite.png',
        'rat_hide_artifacts.png',
        'ratskull_sprite.png',
        'ringofstonewalking_sprite.png',
        'rotten_pumpkin_pie_food.png',
        'rottenmeat_sprite.png',
        'rottenpasty_sprite.png',
        'rottenration_sprite.png',
        'scrollholder_sprite.png',
        'seed_seeds.png',
        'seedpouch_sprite.png',
        'shortsword_sprite.png',
        'shuriken_sprite.png',
        'skeletonkey_sprite.png',
        'soul_shard_materials.png',
        'spear_sprite.png',
        'spell_common.png',
        'spidercharm_sprite.png',
        'stylus_sprite.png',
        'sword_sprite.png',
        'tamahawk_sprite.png',
        'titan_sword_swords.png',
        'tomeofmastery_sprite.png',
        'torch_sprite.png',
        'wand_of_icebolt_wands.png',
        'wand_of_shadowbolt_wands.png',
        'wandholster_sprite.png',
        'wandofmagicmissile_sprite.png',
        'warhammer_sprite.png',
        'warrior_spell_icon.png',
        'weightstone_sprite.png'
    ]
    
    safe_to_remove, needs_review = categorize_unused_images(unused_images)
    
    print(f"Safely removeable images ({len(safe_to_remove)}):")
    for img in safe_to_remove:
        print(f"  - {img}")
    
    print(f"\nImages that need review ({len(needs_review)}):")
    for img in needs_review:
        print(f"  - {img}")
    
    return safe_to_remove, needs_review

if __name__ == "__main__":
    main()