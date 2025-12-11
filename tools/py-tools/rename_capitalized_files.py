#!/usr/bin/env python3
"""
Script to rename remaining capitalized wiki files to lowercase,
and update links in all wiki files to point to the new lowercase names.
"""
import os
import re
from pathlib import Path

def camel_to_snake(name):
    """Convert CamelCase to snake_case"""
    import re
    s1 = re.sub('(.)([A-Z][a-z]+)', r'\1_\2', name)
    return re.sub('([a-z0-9])([A-Z])', r'\1_\2', s1).lower()

def main():
    # Get the remaining capitalized files
    wiki_dir = Path("wiki-data/pages/rpd")
    
    capitalized_files = []
    for file_path in wiki_dir.glob('[A-Z]*.txt'):
        capitalized_files.append(file_path.name)
    
    print("Found capitalized files to rename:")
    for file in capitalized_files:
        print(f"  - {file}")
    
    # Create mapping from old names to new names
    rename_mapping = {}
    for file in capitalized_files:
        old_name = file.replace('.txt', '')
        new_name = camel_to_snake(old_name)
        new_name_with_ext = new_name + '.txt'
        rename_mapping[old_name] = new_name
        print(f"Renaming: {file} → {new_name_with_ext}")
        
        # Rename the file
        old_path = wiki_dir / file
        new_path = wiki_dir / new_name_with_ext
        old_path.rename(new_path)
        print(f"  Renamed {old_path.name} to {new_path.name}")
    
    # Now update all links in wiki files to use the new lowercase names
    print("\\nUpdating links in all wiki files...")
    
    # Create full namespace mapping
    full_mapping = {}
    for old, new in rename_mapping.items():
        full_mapping[old] = new
        full_mapping[f"rpd:{old}"] = f"rpd:{new}"
    
    updated_count = 0
    for file_path in wiki_dir.glob('*.txt'):
        updated = update_links_in_file(file_path, full_mapping)
        if updated:
            updated_count += 1
    
    print(f"\\nUpdated links in {updated_count} files")
    print(f"Renamed {len(capitalized_files)} files to lowercase format")

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
        
        return match.group(0)
    
    # Replace links in the content
    updated_content = re.sub(link_pattern, replace_link, content)
    
    # Write back if content changed
    if original_content != updated_content:
        with open(file_path, 'w', encoding='utf-8') as f:
            f.write(updated_content)
        return True
    
    return False

if __name__ == "__main__":
    main()