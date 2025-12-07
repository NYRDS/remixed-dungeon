#!/usr/bin/env python3
"""
Script to merge duplicate wiki files with different capitalization.
Merges content from capitalized files into lowercase files and removes the capitalized versions.
"""
import os
import re
from pathlib import Path

def normalize_name(filename):
    """Convert filename to lowercase without extension for comparison"""
    name = filename.lower().replace('_', '')
    if name.endswith('.txt'):
        name = name[:-4]
    return name

def find_duplicates(directory):
    """Find files that have both lowercase and capitalized versions"""
    all_files = []
    for file_path in Path(directory).glob('*.txt'):
        all_files.append(file_path.name)
    
    # Group files by normalized name (ignoring case and underscores)
    groups = {}
    for filename in all_files:
        normalized = normalize_name(filename)
        if normalized not in groups:
            groups[normalized] = []
        groups[normalized].append(filename)
    
    # Find groups with more than one file (duplicates)
    duplicates = {k: v for k, v in groups.items() if len(v) > 1}
    return duplicates

def merge_duplicate_files(duplicates, wiki_dir):
    """Merge duplicate files - keep lowercase version, merge content from capitalized version"""
    
    merged_files = []
    
    for normalized, files in duplicates.items():
        # Separate lowercase and capitalized files
        lowercase_files = [f for f in files if f[0].islower() or f[0] == '_']
        capitalized_files = [f for f in files if not f[0].islower() and f[0] != '_']
        
        if not lowercase_files:
            # If no lowercase file exists, pick the first one as "lowercase" version
            lowercase_file = files[0]
            other_files = files[1:]
        else:
            lowercase_file = lowercase_files[0]  # Take the first lowercase-named file
            other_files = capitalized_files  # and merge with capitalized ones
        
        lowercase_path = wiki_dir / lowercase_file
        print(f"Merging {other_files} into {lowercase_file}")
        
        # Read content from the lowercase file
        with open(lowercase_path, 'r', encoding='utf-8') as f:
            main_content = f.read()
        
        # Read and merge content from other files
        merged_content = main_content
        for other_file in other_files:
            other_path = wiki_dir / other_file
            if other_path.exists():
                with open(other_path, 'r', encoding='utf-8') as f:
                    other_content = f.read()
                
                # Add separator between contents
                if not merged_content.strip().endswith('=') and other_content.strip().startswith('======'):
                    # Avoid double headers
                    merged_content += f"\n\n--- Additional Information from {other_file} ---\n\n{other_content}"
                else:
                    merged_content += f"\n\n--- Information from {other_file} ---\n\n{other_content}"
                
                print(f"  - Merged content from {other_file}")
        
        # Write merged content back to the lowercase file
        with open(lowercase_path, 'w', encoding='utf-8') as f:
            f.write(merged_content)
        
        # Remove the other files
        for other_file in other_files:
            other_path = wiki_dir / other_file
            if other_path.exists():
                os.remove(other_path)
                print(f"  - Removed {other_file}")
        
        merged_files.append((lowercase_file, other_files))
    
    return merged_files

def main():
    wiki_dir = Path("wiki-data/pages/rpd")
    duplicates = find_duplicates(wiki_dir)
    
    if not duplicates:
        print("No duplicates found!")
        return
    
    print(f"Found {len(duplicates)} groups of duplicate files")
    
    # Confirm before proceeding
    print("\nThis will merge the following duplicate files:")
    for normalized, files in duplicates.items():
        lowercase_files = [f for f in files if f[0].islower() or f[0] == '_']
        other_files = [f for f in files if not f[0].islower() and f[0] != '_']
        
        if not lowercase_files:
            lowercase_file = files[0]
            other_files = files[1:]
        else:
            lowercase_file = lowercase_files[0]
        
        print(f"  - Keep {lowercase_file}, merge content from {other_files}")
    
    print("\nProceeding with merging (automated mode)...")
    
    merged_files = merge_duplicate_files(duplicates, wiki_dir)
    
    print(f"\nSuccessfully merged {len(merged_files)} groups of duplicate files")
    print("The lowercase-named files now contain merged content from all versions.")
    
    # Also process other directories like ru
    for lang_dir in Path("wiki-data/pages").glob("*/"):
        if lang_dir.is_dir() and lang_dir.name != "rpd":
            rpd_subdir = lang_dir / "rpd"
            if rpd_subdir.exists():
                print(f"\nProcessing {rpd_subdir}...")
                lang_duplicates = find_duplicates(rpd_subdir)
                if lang_duplicates:
                    print(f"Found {len(lang_duplicates)} groups of duplicate files in {rpd_subdir}")
                    merge_duplicate_files(lang_duplicates, rpd_subdir)

if __name__ == "__main__":
    main()