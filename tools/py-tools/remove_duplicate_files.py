#!/usr/bin/env python3
"""
Script to remove duplicate wiki files that don't follow naming conventions.

Based on analysis, there are duplicate files where:
1. One file follows correct snake_case naming convention (e.g., ice_elemental.txt)
2. Another file doesn't follow the convention and appears to be a template (e.g., iceelemental.txt)

This script removes the incorrectly named template files.
"""

import os
import sys

def main():
    wiki_dir = '/home/mike/StudioProjects/remixed-dungeon/wiki-data/pages/rpd'
    
    # Change to the wiki directory
    os.chdir(wiki_dir)
    
    # Get all txt files
    files = [f for f in os.listdir('.') if f.endswith('.txt')]
    
    # Identify files that look like template files (shorter filenames without underscores, 
    # that have a longer snake_case counterpart)
    files_without_underscores = [f for f in files if '_' not in f and f != 'start.txt']
    
    # For each file without underscores, check if there's a snake_case version
    duplicates_to_remove = []
    for file in files_without_underscores:
        name_without_ext = file[:-4]  # Remove .txt extension
        
        # Look for potential snake_case equivalents
        for other_file in files:
            other_name = other_file[:-4]  # Remove .txt extension
            
            # Check if this is the same entity but in snake_case format
            normalized_name = name_without_ext.lower()
            normalized_other = other_name.replace('_', '').lower()
            
            if normalized_name == normalized_other and name_without_ext != other_name:
                # Check which one has content - assuming the snake_case version is the full one
                original_size = os.path.getsize(file)
                snake_size = os.path.getsize(other_file)
                
                if original_size < snake_size:
                    # The underscore version is larger and likely has the actual content
                    duplicates_to_remove.append(file)
                    print(f"Marking {file} for removal (duplicate of {other_file})")
                else:
                    # The non-underscore version is larger, which is unusual
                    print(f"Warning: {file} is larger than {other_file}, investigate manually")
    
    # Actually remove the duplicate files
    for file in duplicates_to_remove:
        print(f"Removing {file}")
        os.remove(file)

if __name__ == "__main__":
    main()