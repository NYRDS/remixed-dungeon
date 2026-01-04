#!/usr/bin/env python3
"""
Script to identify missing strings in localizations by comparing them against the English strings_all.xml file.
"""

import os
import re
import sys
from xml.etree import ElementTree as ET


def extract_strings_from_xml(file_path):
    """
    Extract all string names from an XML file.
    Returns a set of string names.
    """
    try:
        tree = ET.parse(file_path)
        root = tree.getroot()
        
        strings = set()
        for string_elem in root.findall('string'):
            name = string_elem.get('name')
            if name:
                strings.add(name)
        
        return strings
    except ET.ParseError as e:
        print(f"Error parsing XML file {file_path}: {e}")
        return set()


def find_localization_files(base_path):
    """
    Find all strings_all.xml files in localization directories.
    Returns a dictionary mapping language codes to file paths.
    """
    localization_files = {}
    
    res_dir = os.path.join(base_path, "RemixedDungeon", "src", "main", "res")
    
    for dir_name in os.listdir(res_dir):
        if dir_name.startswith("values-") and os.path.isdir(os.path.join(res_dir, dir_name)):
            strings_file = os.path.join(res_dir, dir_name, "strings_all.xml")
            if os.path.exists(strings_file):
                # Extract language code (e.g., "ru" from "values-ru")
                lang_code = dir_name.replace("values-", "")
                localization_files[lang_code] = strings_file
    
    return localization_files


def main():
    if len(sys.argv) < 2:
        print("Usage: python check_missing_strings.py <project_base_path>")
        print("Example: python check_missing_strings.py /path/to/remixed-dungeon")
        sys.exit(1)
    
    base_path = sys.argv[1]
    
    # Path to English strings file
    english_file = os.path.join(base_path, "RemixedDungeon", "src", "main", "res", "values", "strings_all.xml")
    
    if not os.path.exists(english_file):
        print(f"English strings file not found: {english_file}")
        sys.exit(1)
    
    print("Extracting strings from English file...")
    english_strings = extract_strings_from_xml(english_file)
    print(f"Found {len(english_strings)} strings in English file")
    
    # Find all localization files
    localization_files = find_localization_files(base_path)
    print(f"Found {len(localization_files)} localization files")
    
    # Compare each localization with English
    for lang_code, file_path in sorted(localization_files.items()):
        print(f"\n--- Checking {lang_code} ---")
        
        localized_strings = extract_strings_from_xml(file_path)
        missing_strings = english_strings - localized_strings
        
        if missing_strings:
            print(f"Missing {len(missing_strings)} strings in {lang_code}:")
            for string_name in sorted(missing_strings):
                print(f"  - {string_name}")
        else:
            print(f"All strings present in {lang_code} (no missing strings)")
    
    # Summary
    print(f"\n--- Summary ---")
    print(f"Total English strings: {len(english_strings)}")
    print(f"Total localization files checked: {len(localization_files)}")
    
    for lang_code in sorted(localization_files.keys()):
        localized_strings = extract_strings_from_xml(localization_files[lang_code])
        missing_count = len(english_strings - localized_strings)
        print(f"{lang_code}: {missing_count} missing strings")


if __name__ == "__main__":
    main()