#!/usr/bin/env python3
"""
Script to list missing mr: pages based on actual entities lists.

This script reads entity lists from the entities/ directory and checks which
corresponding mr: namespace pages are missing from the wiki-data repository.
"""

import os
import sys
from pathlib import Path


def read_entity_file(file_path):
    """Read an entity file and return a list of entity names."""
    with open(file_path, 'r', encoding='utf-8') as f:
        entities = [line.strip() for line in f if line.strip() and not line.startswith('#')]
    return entities


def normalize_entity_name(name):
    """Convert entity name to lowercase with underscores (snake_case), handling acronyms properly."""
    import re

    # Handle special case for test path first
    if name.startswith("test/"):
        rest = name[5:]  # Remove "test/"
        normalized_rest = normalize_camel_case(rest)
        result = f"test_{normalized_rest}_item"
        # Remove duplicate entity type if it occurs
        result = result.replace("_item_item", "_item")
        return result

    # Handle special cases with dots (like internal class names)
    if '.' in name:
        parts = name.split('.')
        base_name = parts[0]
        internal_class = parts[1]

        # Process the base name using the standard function
        base_normalized = normalize_camel_case(base_name)

        # For internal class, just convert to snake_case without applying the same processing as for entity names
        # Use a simple camelCase to snake_case conversion
        s1 = re.sub('([a-z0-9])([A-Z])', r'\1_\2', internal_class)
        internal_normalized = s1.lower()

        # Combine base + internal + entity type suffix (e.g., _item)
        result = f"{base_normalized}_{internal_normalized}_item"
        # Remove duplicate entity type if it occurs
        result = result.replace("_item_item", "_item")
        return result

    result = normalize_camel_case(name)
    # Handle various types of duplicate suffixes
    result = result.replace("_buff_buff", "_buff")
    result = result.replace("_spell_spell", "_spell")
    result = result.replace("_item_item", "_item")
    result = result.replace("_mob_mob", "_mob")
    return result

def normalize_camel_case(name):
    """Helper function to convert CamelCase to snake_case."""
    import re

    # Split CamelCase: insert underscore between lowercase and uppercase letters
    s1 = re.sub('([a-z0-9])([A-Z])', r'\1_\2', name)

    # Make everything lowercase
    s2 = s1.lower()

    # Fix specific known patterns that shouldn't be separated
    # Handle common acronyms
    replacements = [
        ('n_p_c', 'npc'),
        ('d_m', 'dm'),
        ('j_a_r_o_f', 'jarof'),  # For "JarOfSouls"
        ('s_h_o_o_t_i_n_e_y_e', 'shoot_in_eye'),  # For "ShootInEye"
        ('o_n_e_w_a_y', 'one_way'),  # For "OneWayCursedLove"
        ('c_h_a_m_p_i_o_n_o_f', 'champion_of'),  # For "ChampionOfAir"
    ]

    for old, new in replacements:
        s2 = s2.replace(old, new)

    return s2


def get_existing_mr_pages(wiki_pages_dir):
    """Get a set of existing mr namespace pages."""
    mr_pages = set()
    mr_dir = os.path.join(wiki_pages_dir, 'mr')
    
    if os.path.exists(mr_dir):
        for filename in os.listdir(mr_dir):
            if filename.endswith('.txt'):
                page_name = filename[:-4]  # Remove .txt extension
                mr_pages.add(page_name)
    
    return mr_pages


def main():
    """Main function to list missing mr: pages."""
    project_root = Path(__file__).parent.parent.parent
    entities_dir = project_root / 'entities'
    wiki_pages_dir = project_root / 'wiki-data' / 'pages'
    
    # Check if entities directory exists
    if not entities_dir.exists():
        print(f"Error: Entities directory does not exist at {entities_dir}")
        return 1
    
    # Define entity file types
    entity_files = {
        'mobs': entities_dir / 'mobs.txt',
        'items': entities_dir / 'items.txt',
        'buffs': entities_dir / 'buffs.txt',
        'spells': entities_dir / 'spells.txt'
    }
    
    # Collect all entities from all entity files
    all_entities = {}
    for entity_type, file_path in entity_files.items():
        if file_path.exists():
            entities = read_entity_file(file_path)
            all_entities[entity_type] = entities
            print(f"Read {len(entities)} {entity_type} from {file_path}")
        else:
            print(f"Warning: {file_path} does not exist")
    
    # Normalize all entity names to lowercase with underscores
    normalized_entities = {}
    for entity_type, entities in all_entities.items():
        normalized_entities[entity_type] = [normalize_entity_name(e) for e in entities]
    
    # Get existing mr: pages
    existing_mr_pages = get_existing_mr_pages(wiki_pages_dir)
    print(f"Found {len(existing_mr_pages)} existing mr: pages")
    
    # Check for mr: directory and create it if it doesn't exist
    mr_dir = wiki_pages_dir / 'mr'
    if not mr_dir.exists():
        print(f"mr: namespace directory does not exist at {mr_dir}")
        print("You may need to create this directory first.")
    else:
        print(f"mr: namespace directory exists at {mr_dir}")
    
    # Find missing mr: pages for each entity type
    missing_pages = {}
    all_missing_count = 0
    
    for entity_type, entities in normalized_entities.items():
        missing_for_type = []
        for entity in entities:
            # For certain types, append the type as suffix to avoid naming conflicts
            if entity_type == 'mobs':
                page_name = f"{entity}_mob"
            elif entity_type == 'items':
                page_name = f"{entity}_item"
            elif entity_type == 'spells':
                page_name = f"{entity}_spell"
            elif entity_type == 'buffs':
                page_name = f"{entity}_buff"
            else:
                page_name = entity
            
            if page_name not in existing_mr_pages:
                missing_for_type.append(page_name)
        
        missing_pages[entity_type] = missing_for_type
        all_missing_count += len(missing_for_type)
        print(f"Missing {entity_type}: {len(missing_for_type)} pages")
    
    print(f"\nTotal missing mr: pages: {all_missing_count}")
    print("\nDetailed breakdown:")
    
    # Print missing pages by type
    for entity_type, missing_list in missing_pages.items():
        if missing_list:
            print(f"\nMissing {entity_type.upper()} mr: pages ({len(missing_list)}):")
            for page in sorted(missing_list):
                print(f"  - mr:{page}")
    
    # Also show them as a simple list for easy processing
    print("\nAll missing mr: pages as a simple list:")
    all_missing = []
    for entity_type, missing_list in missing_pages.items():
        all_missing.extend(missing_list)
    
    for page in sorted(all_missing):
        print(f"mr:{page}")
    
    return 0


if __name__ == "__main__":
    sys.exit(main())