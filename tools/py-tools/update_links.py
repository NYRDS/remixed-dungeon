#!/usr/bin/env python3
"""
Script to update internal wiki links to point to correct lowercase file names.
This script finds links that point to capitalized versions and updates them to lowercase.
"""
import os
import re
from pathlib import Path

def normalize_title_to_filename(title):
    """Convert wiki page title to lowercase filename with underscores"""
    # Convert CamelCase to snake_case
    s1 = re.sub('(.)([A-Z][a-z]+)', r'\1_\2', title)
    filename = re.sub('([a-z0-9])([A-Z])', r'\1_\2', s1).lower()
    # Handle special cases like apostrophes and other special characters
    filename = re.sub(r'[^a-z0-9_\-]', '_', filename)
    return filename + '.txt'

def find_all_wiki_files(directory):
    """Find all wiki files and map their normalized names"""
    file_mapping = {}
    for file_path in Path(directory).glob('**/*.txt'):
        filename = file_path.name
        normalized = normalize_title_to_filename(filename.replace('.txt', ''))
        # Store the mapping from the normalized name to the actual filename
        file_mapping[filename.replace('.txt', '')] = filename.replace('.txt', '')
    return file_mapping

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
            
            # If it contains a namespace separator, process the page part
            if ':' in target:
                namespace, page = target.split(':', 1)
                normalized_page = normalize_title_to_filename(page.replace('.txt', ''))
                # Remove .txt extension from the normalized page name for the link
                normalized_page = normalized_page.replace('.txt', '')
                new_target = f"{namespace}:{normalized_page}"
            else:
                # No namespace, just normalize the page name
                normalized_page = normalize_title_to_filename(target.replace('.txt', ''))
                # Remove .txt extension from the normalized page name for the link
                normalized_page = normalized_page.replace('.txt', '')
                new_target = normalized_page
            
            if new_target != target:
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

def main():
    # Get all wiki directories
    wiki_dirs = [Path('wiki-data/pages/rpd')]
    
    # Also check other language directories
    for lang_dir in Path('wiki-data/pages').glob('*/'):
        if lang_dir.is_dir() and lang_dir.name != 'wiki':
            rpd_subdir = lang_dir / 'rpd'
            if rpd_subdir.exists():
                wiki_dirs.append(rpd_subdir)
    
    updated_file_count = 0
    
    print("Updating internal wiki links to use lowercase format...")
    
    for wiki_dir in wiki_dirs:
        print(f"Processing {wiki_dir}...")
        
        # Find all txt files in this directory
        all_files = list(wiki_dir.glob('*.txt'))
        
        for file_path in all_files:
            # Check if file needs updating
            if update_links_in_file(file_path, {}):
                print(f"  Updated links in {file_path.name}")
                updated_file_count += 1
    
    print(f"\nUpdated links in {updated_file_count} files")
    print("All internal wiki links now use lowercase naming convention.")

if __name__ == "__main__":
    main()