#!/usr/bin/env python3
"""
Script to detect duplicate strings in localization files for Remixed Dungeon.
"""

import os
import sys
import re
from collections import defaultdict


def find_duplicate_strings(file_path):
    """
    Find duplicate string entries in a localization file.
    
    Args:
        file_path (str): Path to the strings_all.xml file
    
    Returns:
        dict: Dictionary with duplicate string names as keys and list of line numbers as values
    """
    with open(file_path, 'r', encoding='utf-8') as f:
        lines = f.readlines()
    
    # Dictionary to store string names and their line numbers
    string_lines = defaultdict(list)
    
    # Regex to match string name attributes
    string_pattern = re.compile(r'<string\s+name="([^"]+)"')
    
    for idx, line in enumerate(lines, start=1):
        match = string_pattern.search(line)
        if match:
            string_name = match.group(1)
            string_lines[string_name].append(idx)
    
    # Find duplicates (strings appearing more than once)
    duplicates = {name: positions for name, positions in string_lines.items() if len(positions) > 1}
    
    return duplicates


def check_all_localizations(base_path):
    """
    Check all localization files in the project for duplicates.
    
    Args:
        base_path (str): Base path of the Remixed Dungeon project
    """
    res_dir = os.path.join(base_path, "RemixedDungeon", "src", "main", "res")
    
    if not os.path.exists(res_dir):
        print(f"Error: Resource directory not found at {res_dir}")
        return
    
    # Find all values-* directories containing strings_all.xml
    for dir_name in os.listdir(res_dir):
        if dir_name.startswith("values"):
            lang_dir = os.path.join(res_dir, dir_name)
            strings_file = os.path.join(lang_dir, "strings_all.xml")
            
            if os.path.isfile(strings_file):
                print(f"\nChecking {dir_name}/strings_all.xml...")
                duplicates = find_duplicate_strings(strings_file)
                
                if duplicates:
                    print(f"  Found {len(duplicates)} duplicate string(s):")
                    for string_name, line_numbers in duplicates.items():
                        print(f"    {string_name}: lines {', '.join(map(str, line_numbers))}")
                else:
                    print(f"  No duplicates found.")


def check_specific_language(base_path, lang_code):
    """
    Check a specific language localization file for duplicates.
    
    Args:
        base_path (str): Base path of the Remixed Dungeon project
        lang_code (str): Language code (e.g., 'el', 'ru', 'fr')
    """
    if lang_code == 'en':
        # English is in the default values directory
        strings_file = os.path.join(base_path, "RemixedDungeon", "src", "main", "res", "values", "strings_all.xml")
    else:
        strings_file = os.path.join(base_path, "RemixedDungeon", "src", "main", "res", f"values-{lang_code}", "strings_all.xml")
    
    if not os.path.isfile(strings_file):
        print(f"Error: Localization file not found at {strings_file}")
        return
    
    print(f"Checking {os.path.basename(os.path.dirname(strings_file))}/strings_all.xml...")
    duplicates = find_duplicate_strings(strings_file)
    
    if duplicates:
        print(f"Found {len(duplicates)} duplicate string(s):")
        for string_name, line_numbers in duplicates.items():
            print(f"  {string_name}: lines {', '.join(map(str, line_numbers))}")
    else:
        print("No duplicates found.")


def main():
    if len(sys.argv) < 2:
        print("Usage:")
        print("  python3 check_duplicate_strings.py <project_base_path> [language_code]")
        print("")
        print("Examples:")
        print("  python3 check_duplicate_strings.py /path/to/remixed-dungeon      # Check all languages")
        print("  python3 check_duplicate_strings.py /path/to/remixed-dungeon el  # Check Greek only")
        print("  python3 check_duplicate_strings.py /path/to/remixed-dungeon ru  # Check Russian only")
        sys.exit(1)
    
    base_path = sys.argv[1]
    
    if not os.path.isdir(base_path):
        print(f"Error: Path does not exist: {base_path}")
        sys.exit(1)
    
    if len(sys.argv) > 2:
        # Check specific language
        lang_code = sys.argv[2]
        check_specific_language(base_path, lang_code)
    else:
        # Check all languages
        check_all_localizations(base_path)


if __name__ == "__main__":
    main()