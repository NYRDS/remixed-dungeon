#!/usr/bin/env python3
"""
Script to identify duplicate wiki files with different capitalization
and suggest which ones to merge.
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

def main():
    wiki_dir = Path("wiki-data/pages/rpd")
    duplicates = find_duplicates(wiki_dir)
    
    print("Found duplicate files:")
    print("=" * 50)
    
    for normalized, files in duplicates.items():
        print(f"\nGroup '{normalized}':")
        for file in files:
            file_path = wiki_dir / file
            if file_path.exists():
                with open(file_path, 'r', encoding='utf-8') as f:
                    content = f.read()
                    lines = len(content.split('\n'))
                    print(f"  - {file} ({lines} lines)")
    
    print("\n" + "=" * 50)
    print("Recommended actions:")
    print("1. For each group, merge content from all files into the lowercase version")
    print("2. Remove the capitalized versions")
    print("3. Update internal links to use lowercase format")
    
    return duplicates

if __name__ == "__main__":
    main()