#!/usr/bin/env python3
"""
Script to update broken image references in wiki pages based on the actual generated names
"""
import os
import re

def update_wiki_page_images():
    """
    Update wiki pages to fix broken image references based on actual generated names
    """
    # Dictionary mapping broken references to correct references
    # This is based on understanding the original naming vs. processed naming
    mapping = {
        # Spell pages - these should end in _spell_icon.png
        'backstab_spell_icon.png': 'backstab_spell_icon.png',  # This should already be correct
        'calm_spell_icon.png': 'calm_spell_icon.png',  # This should already be correct
        'charm_spell_icon.png': 'charm_spell_icon.png',  # This should already be correct
        'cloak_spell_icon.png': 'cloak_spell_icon.png',  # This should already be correct
        'curse_item_spell_icon.png': 'curseitem_spell_icon.png',  # Original: curse_item -> curseitem
        'dark_sacrifice_spell_icon.png': 'darksacrifice_spell_icon.png',  # Original: dark_sacrifice -> darksacrifice
        'dash_spell_icon.png': 'dash_spell_icon.png',  # This should already be correct
        'die_hard_spell_icon.png': 'diehard_spell_icon.png',  # Original: die_hard -> diehard
        'exhumation_spell_icon.png': 'exhumation_spell_icon.png',  # This should already be correct
        'freeze_globe_spell_icon.png': 'freezeglobe_spell_icon.png',  # Original: freeze_globe -> freezeglobe
        'haste_spell_icon.png': 'haste_spell_icon.png',  # This should already be correct
        'heal_spell_icon.png': 'heal_spell_icon.png',  # This should already be correct
        'healing_spell_icon.png': 'healing_spell_icon.png',  # This should already be correct
        'hide_in_grass_spell_icon.png': 'hideingrass_spell_icon.png',  # Original: hide_in_grass -> hideingrass
        'ignite_spell_icon.png': 'ignite_spell_icon.png',  # This should already be correct
        'kunai_throw_spell_icon.png': 'kunaithrow_spell_icon.png',  # Original: kunai_throw -> kunaithrow
        'lightning_bolt_spell_icon.png': 'lightningbolt_spell_icon.png',  # Original: lightning_bolt -> lightningbolt
        'magic_arrow_spell_icon.png': 'magicarrow_spell_icon.png',  # Original: magic_arrow -> magicarrow
        'magic_torch_spell_icon.png': 'magictorch_spell_icon.png',  # Original: magic_torch -> magictorch
        'nature_armor_spell_icon.png': 'naturearmor_spell_icon.png',  # Original: nature_armor -> naturearmor
        'order_spell_icon.png': 'order_spell_icon.png',  # This should already be correct
        'possess_spell_icon.png': 'possess_spell_icon.png',  # This should already be correct
        'raise_dead_spell_icon.png': 'raisedead_spell_icon.png',  # Original: raise_dead -> raisedead
        'roar_spell_icon.png': 'roar_spell_icon.png',  # This should already be correct
        'root_spell_icon.png': 'rootspell_spell_icon.png',  # Original: root_spell -> rootspell
        'shoot_in_eye_spell_icon.png': 'shootineye_spell_icon.png',  # Original: shoot_in_eye -> shootineye
        'smash_spell_icon.png': 'smash_spell_icon.png',  # This should already be correct
        'sprout_spell_icon.png': 'sprout_spell_icon.png',  # This should already be correct
        'summon_beast_spell_icon.png': 'summonbeast_spell_icon.png',  # Original: summon_beast -> summonbeast
        'summon_deathling_spell_icon.png': 'summondeathling_spell_icon.png',  # Original: summon_deathling -> summondeathling
        'test_spell_icon.png': 'testspell_spell_icon.png',  # Original: test_spell -> testspell
        'town_portal_spell_icon.png': 'townportal_spell_icon.png',  # Original: town_portal -> townportal
        'wind_gust_spell_icon.png': 'windgust_spell_icon.png',  # Original: wind_gust -> windgust
        'body_armor_spell_icon.png': 'bodyarmor_spell_icon.png',  # Original: body_armor -> bodyarmor

        # Items - these should end in _sprite.png (or specific cases like _item.png)
        'chaos_armor_armor.png': 'chaosarmor_sprite.png',  # Original: chaos_armor -> chaosarmor
        'chaos_staff.png': 'chaosstaff_sprite.png',  # Original: chaos_staff -> chaosstaff
        'ice_key_item.png': 'icekey_sprite.png',  # Original: ice_key -> icekey
        'ration_item.png': 'ration_sprite.png',  # This should already be correct
        'bone_saw_sprite.png': 'bonesaw_sprite.png',  # Original: bone_saw -> bonesaw
        'skeletonkey_item.png': 'skeletonkey_sprite.png',  # This should already be correct
        'potion_paralytic_gas_item.png': 'potionofparalyticgas_sprite.png',  # Original: potion_paralytic_gas -> potionofparalyticgas
        'potion_of_paralytic_gas_sprite.png': 'potionofparalyticgas_sprite.png',  # Original: potion_of_paralytic_gas -> potionofparalyticgas
        'potion_of_toxic_gas_potions.png': 'potionoftoxicgas_sprite.png',  # Original: potion_of_toxic_gas -> potionoftoxicgas
        'potion_of_strength_sprite.png': 'potionofstrength_sprite.png',  # Original: potion_of_strength -> potionofstrength
        'potion_of_healing_sprite.png': 'potionofhealing_sprite.png',  # Original: potion_of_healing -> potionofhealing
        'candle_of_visions_item.png': 'candleofmindvision_sprite.png',  # Original: candle_of_mind_vision -> candleofmindvision
        'dried_rose_item.png': 'driedrose_sprite.png',  # This should already be correct
        'heart_of_darkness_item.png': 'heartofdarkness_sprite.png',  # Original: heart_of_darkness -> heartofdarkness
        'the_soulbringer_item.png': 'the_soulbringer_item.png',  # This is a special case, probably not processed
        'giant_rat_skull_item.png': 'ratskull_sprite.png',  # Original: rat_skull -> ratskull (giant rat skull is just a rat skull)
        'chaos_crystal_item.png': 'chaoscrystal_sprite.png',  # Original: chaos_crystal -> chaoscrystal
        'ringofelements_item.png': 'ringofelements_sprite.png',  # This should already be correct
        'cloth_armor_sprite.png': 'clotharmor_sprite.png',  # Original: cloth_armor -> clotharmor
        'leather_armor_sprite.png': 'leatherarmor_sprite.png',  # Original: leather_armor -> leatherarmor
        'mail_armor_sprite.png': 'mailarmor_sprite.png',  # Original: mail_armor -> mailarmor
        'scale_armor_sprite.png': 'scalearmor_sprite.png',  # Original: scale_armor -> scalearmor
        'plate_armor_sprite.png': 'platearmor_sprite.png',  # Original: plate_armor -> platearmor
        'gnoll_tomahawk_sprite.png': 'gnolltamahawk_sprite.png',  # Original: gnoll_tomahawk -> gnolltamahawk
        'tomahawk_sprite.png': 'tamahawk_sprite.png',  # This should already be correct
        'corpse_dust_sprite.png': 'corpsedust_sprite.png',  # Original: corpse_dust -> corpsedust
        'short_sword_sprite.png': 'shortsword_sprite.png',  # Original: short_sword -> shortsword
        'wooden_bow.png': 'woodenbow_sprite.png',  # Original: wooden_bow -> woodenbow
        'ranged_wooden_bow.png': 'woodenbow_sprite.png',  # Another reference to wooden bow
        'scroll_of_identify_sprite.png': 'scrollofidentify_sprite.png',  # Original: scroll_of_identify -> scrollofidentify
        'scroll_upgrade_sprite.png': 'scrollofupgrade_sprite.png',  # Original: scroll_upgrade -> scrollofupgrade
        'scroll_of_upgrade_sprite.png': 'scrollofupgrade_sprite.png',  # Original: scroll_of_upgrade -> scrollofupgrade
        'scroll_of_weapon_upgrade_sprite.png': 'scrollofweaponupgrade_sprite.png',  # Original: scroll_of_weapon_upgrade -> scrollofweaponupgrade
        'scroll_weapon_upgrade_sprite.png': 'scrollofweaponupgrade_sprite.png',  # Another reference
        'scroll_of_domination_sprite.png': 'scrollofdomination_sprite.png',  # Original: scroll_of_domination -> scrollofdomination
        'scroll_of_curse_sprite.png': 'scrollofcurse_sprite.png',  # Original: scroll_of_curse -> scrollofcurse
        'scroll_of_remove_curse_sprite.png': 'scrollofremovecurse_sprite.png',  # Original: scroll_of_remove_curse -> scrollofremovecurse
        'scroll_sprite.png': 'scrollholder_sprite.png',  # Scroll holder (not generic scroll)
        'lloyds_beacon.png': 'lloydsbeacon_sprite.png',  # Original: lloyds_beacon -> lloydsbeacon
        'dried_rose_item.png': 'driedrose_sprite.png',  # This should already be correct

        # Mobs - these should end in _sprite.png
        'shadow_lord_sprite.png': 'shadowlord_sprite.png',  # Original: shadow_lord -> shadowlord
        'witchdoctor_sprite.png': 'witchdoctor_sprite.png',  # This should already be correct
        'battle_mage_sprite.png': 'battlemage_sprite.png',  # Original: battle_mage -> battlemage
        'freerunner_sprite.png': 'freerunner_sprite.png',  # This should already be correct
        'scout_sprite.png': 'scout_sprite.png',  # This should already be correct
        'death_knight_sprite.png': 'deathknight_sprite.png',  # This should already be correct
        'enslaved_soul_sprite.png': 'enslavedsoul_sprite.png',  # Original: enslaved_soul -> enslavedsoul
        'gnoll_brute_sprite.png': 'brute_sprite.png',  # Gnoll brute is just Brute mob
        'shaman_armor_sprite.png': 'shamanarmor_sprite.png',  # Original: shaman_armor -> shamanarmor
        'ice_guardian_core_sprite.png': 'iceguardiancore_sprite.png',  # This should already be correct
        'necromancer_armor_armor.png': 'necromancerarmor_sprite.png',  # Original: necromancer_armor -> necromancerarmor
        'stranger_azuterron_npc_sprite.png': 'azuterronnpc_sprite.png',  # Original: azuterron_npc -> azuterronnpc
        'albino_rat_sprite.png': 'albino_sprite.png',  # Albino rat is just Albino mob
        'sniper_sprite.png': 'sniper_sprite.png',  # This should already be correct
        'berserker_sprite.png': 'berserker_sprite.png',  # This should already be correct
        'berserk_armor_sprite.png': 'berserkarmor_sprite.png',  # Original: berserk_armor -> berserkarmor
        'armored_statue_sprite.png': 'armoredstatue_sprite.png',  # Original: armored_statue -> armoredstatue
        'gladiator_sprite.png': 'gladiator_sprite.png',  # This should already be correct
        'warden_sprite.png': 'warden_sprite.png',  # This should already be correct
        'assassin_sprite.png': 'assassin_sprite.png',  # This should already be correct
        'guardian_sprite.png': 'guardian_sprite.png',  # This should already be correct

        # Equipment armor
        'guardian_armor_sprite.png': 'guardianarmor_sprite.png',  # Original: guardian_armor -> guardianarmor
        'ring_of_haste_sprite.png': 'ringofhaste_sprite.png',  # Original: ring_of_haste -> ringofhaste
        'soul_reaper_black_skull_sprite.png': 'blackskull_sprite.png',  # Soul reaper uses black skull
        'black_skull_of_mastery_sprite.png': 'blackskullofmastery_sprite.png',  # Original: black_skull_of_mastery -> blackskullofmastery
        'obsidian_skull_black_skull_sprite.png': 'blackskull_sprite.png',  # Obsidian skull is black skull
        'gold_statue_golden_statue_sprite.png': 'goldenstatue_sprite.png',  # Gold statue is golden statue
        'tome_of_mastery_sprite.png': 'tomeofmastery_sprite.png',  # This should already be correct
        'invisibility_icon.png': 'invisibility_sprite.png',  # Change from icon to sprite
        'regeneration_icon.png': 'regeneration_sprite.png',  # Change from icon to sprite
        'roots_icon.png': 'roots_sprite.png',  # Change from icon to sprite
        'haste_icon.png': 'haste_sprite.png',  # Change from icon to sprite
        'combo_effect.png': 'combo_sprite.png',  # Change from effect to sprite
        'slow_effect.png': 'slow_sprite.png',  # Change from effect to sprite
        'throwing_knife_sprite.png': 'shuriken_sprite.png',  # Throwing weapons often use shuriken sprite
    }

    # Also handle seed patterns - these have a specific format
    # seed_firebloom.png -> firebloom.seed_sprite.png
    seed_mapping = {
        'seed_firebloom.png': 'firebloom.seed_sprite.png',
        'seed_earthroot.png': 'earthroot.seed_sprite.png', 
        'seed_blandfruit.png': 'blandfruit.seed_sprite.png',  # Assuming this exists
        'seed_sungrass.png': 'sungrass.seed_sprite.png',
        'seed_fadeleaf.png': 'fadeleaf.seed_sprite.png',
        'seed_rotberry.png': 'rotberry.seed_sprite.png',
        'seed_sorrowmoss.png': 'sorrowmoss.seed_sprite.png',
        'seed_dreamweed.png': 'dreamweed.seed_sprite.png',
        'seed_icecap.png': 'icecap.seed_sprite.png',
        'seed_moongrace.png': 'moongrace.seed_sprite.png'
    }
    
    # Combine mappings
    all_mappings = {**mapping, **seed_mapping}
    
    fixed_count = 0
    
    # Walk through all text files in the wiki pages directory
    wiki_pages_dir = "/home/mike/StudioProjects/remixed-dungeon/wiki-data/pages/rpd"
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
                    # Check if this reference is broken (doesn't exist in images directory)
                    images_dir = "/home/mike/StudioProjects/remixed-dungeon/wiki-data/media/rpd/images"
                    if image_ref not in os.listdir(images_dir):
                        # Check if this reference is in our mapping
                        if image_ref in all_mappings:
                            # Replace the old reference with the new one
                            old_full = f"{{{{ rpd:images:{image_ref}"
                            new_full = f"{{{{ rpd:images:{all_mappings[image_ref]}"
                            
                            # Replace in content
                            content = content.replace(old_full, new_full)
                            modified = True
                            print(f"FIXED: {file_path} - {image_ref} -> {all_mappings[image_ref]}")
                            fixed_count += 1
                        else:
                            print(f"NO MAPPING: {file_path} - {image_ref} (no mapping available)")
                
                # Write back the modified content if needed
                if modified:
                    with open(file_path, 'w', encoding='utf-8') as f:
                        f.write(content)
    
    return fixed_count

if __name__ == "__main__":
    fixed_count = update_wiki_page_images()
    print(f"\nFixed {fixed_count} broken image references")