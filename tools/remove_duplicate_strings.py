#!/usr/bin/env python3
"""
Script to remove duplicate strings in localization files for Remixed Dungeon.
"""

import os
import sys
import re
import shutil
from collections import OrderedDict


def remove_duplicate_strings(file_path):
    """
    Remove duplicate string entries in a localization file, keeping only the first occurrence.
    
    Args:
        file_path (str): Path to the strings_all.xml file
    
    Returns:
        int: Number of duplicates removed
    """
    with open(file_path, 'r', encoding='utf-8') as f:
        lines = f.readlines()
    
    seen_strings = set()
    unique_lines = []
    duplicates_removed = 0
    
    # Regex to match string name attributes
    string_pattern = re.compile(r'<string\s+name="([^"]+)"')
    
    for line in lines:
        match = string_pattern.search(line)
        if match:
            string_name = match.group(1)
            if string_name in seen_strings:
                # This is a duplicate, skip it
                duplicates_removed += 1
                print(f"  Removed duplicate: {string_name}")
            else:
                # This is the first occurrence, keep it
                seen_strings.add(string_name)
                unique_lines.append(line)
        else:
            # Not a string entry, keep it
            unique_lines.append(line)
    
    # Write the cleaned content back to the file
    backup_path = file_path + ".backup"
    shutil.copy2(file_path, backup_path)
    print(f"  Backup created: {backup_path}")
    
    with open(file_path, 'w', encoding='utf-8') as f:
        f.writelines(unique_lines)
    
    return duplicates_removed


def main():
    if len(sys.argv) != 2:
        print("Usage:")
        print("  python3 remove_duplicate_strings.py <path_to_strings_all_xml>")
        print("")
        print("Example:")
        print("  python3 remove_duplicate_strings.py /path/to/remixed-dungeon/RemixedDungeon/src/main/res/values-el/strings_all.xml")
        sys.exit(1)
    
    file_path = sys.argv[1]
    
    if not os.path.isfile(file_path):
        print(f"Error: File does not exist: {file_path}")
        sys.exit(1)
    
    print(f"Processing {file_path}...")
    print("Looking for duplicate strings...")
    
    duplicates_count = remove_duplicate_strings(file_path)
    
    if duplicates_count > 0:
        print(f"Removed {duplicates_count} duplicate string(s).")
        print("Process completed successfully.")
    else:
        print("No duplicates found.")
        print("Process completed.")


if __name__ == "__main__":
    main()