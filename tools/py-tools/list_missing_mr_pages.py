#!/usr/bin/env python3
"""
Script to identify missing mr: namespace pages based on entity lists in the entities/ directory.
This helps maintain a complete set of machine-readable reference pages.
"""

import os
import glob

def normalize_entity_name(entity_name, entity_type):
    """Convert Java/CamelCase entity names to snake_case formats for wiki pages."""
    # Remove any package prefixes
    if '.' in entity_name:
        entity_name = entity_name.split('.')[-1]

    # Convert CamelCase to snake_case
    result = ""
    for i, c in enumerate(entity_name):
        if c.isupper() and i != 0:
            result += "_"
        result += c.lower()

    # Add appropriate suffix based on entity type
    # But avoid adding duplicate suffixes if the entity name already ends with the suffix
    if entity_type == "mobs":
        if not result.endswith("_mob"):
            result += "_mob"
    elif entity_type == "items":
        if not result.endswith("_item"):
            result += "_item"
    elif entity_type == "spells":
        if not result.endswith("_spell"):
            result += "_spell"
    elif entity_type == "buffs":
        if not result.endswith("_buff"):
            result += "_buff"
    else:
        # For other types, just use the entity name as is
        pass

    return result

def get_existing_mr_pages(wiki_pages_dir):
    """Get list of existing mr: namespace pages."""
    mr_pages_dir = os.path.join(wiki_pages_dir, "mr")
    if not os.path.exists(mr_pages_dir):
        return set()
    
    existing_pages = set()
    for file in os.listdir(mr_pages_dir):
        if file.endswith(".txt"):
            page_name = file[:-4]  # Remove .txt extension
            existing_pages.add(page_name)
    
    return existing_pages

def get_entities_from_file(entities_file):
    """Get list of entities from an entities file."""
    if not os.path.exists(entities_file):
        return []
    
    with open(entities_file, 'r') as f:
        entities = [line.strip() for line in f if line.strip()]
    
    return entities

def main():
    # The script is run from the project root, so we can just use relative paths
    project_root = os.getcwd()
    entities_dir = os.path.join(project_root, "entities")
    wiki_pages_dir = os.path.join(project_root, "wiki-data", "pages")
    
    # Get existing mr: pages
    existing_mr_pages = get_existing_mr_pages(wiki_pages_dir)
    
    print("Missing mr: namespace pages:")
    print("="*50)
    
    missing_pages = []
    
    # Check each entity type
    for entity_file in ["mobs.txt", "items.txt", "spells.txt", "buffs.txt"]:
        entity_path = os.path.join(entities_dir, entity_file)
        if not os.path.exists(entity_path):
            continue

        entity_type = entity_file.split('.')[0]  # mobs, items, spells, buffs
        entities = get_entities_from_file(entity_path)

        print(f"\n{entity_type.upper()}:")
        print("-" * 20)

        for entity in entities:
            normalized_name = normalize_entity_name(entity, entity_type)

            if normalized_name not in existing_mr_pages:
                print(f"  - {normalized_name}")
                missing_pages.append(f"mr:{normalized_name}")
    
    print(f"\nTotal missing pages: {len(missing_pages)}")
    
    # Optionally, also output a simple list for processing
    if missing_pages:
        print("\nSimple list for processing:")
        for page in missing_pages:
            print(page)

if __name__ == "__main__":
    main()