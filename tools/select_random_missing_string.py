#!/usr/bin/env python3
"""
Script to randomly select a string ID that is present in English but missing in one of the other languages.
"""

import os
import re
import sys
import random
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


def find_string_value_in_xml(file_path, string_id):
    """
    Find the value of a string ID in an XML file.
    Returns the string value or None if not found.
    """
    try:
        tree = ET.parse(file_path)
        root = tree.getroot()

        for string_elem in root.findall('string'):
            name = string_elem.get('name')
            if name == string_id:
                return string_elem.text

        return None
    except ET.ParseError as e:
        print(f"Error parsing XML file {file_path}: {e}")
        return None


def main():
    if len(sys.argv) < 2:
        print("Usage: python select_random_missing_string.py <project_base_path> [language_code]")
        print("Example: python select_random_missing_string.py /path/to/remixed-dungeon        # Random from any language")
        print("Example: python select_random_missing_string.py /path/to/remixed-dungeon ru    # Random from Russian only")
        sys.exit(1)

    base_path = sys.argv[1]
    target_language = sys.argv[2] if len(sys.argv) > 2 else None

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

    # If a specific language is requested, filter the localization files
    if target_language:
        if target_language not in localization_files:
            print(f"Language '{target_language}' not found in localizations.")
            print(f"Available languages: {', '.join(sorted(localization_files.keys()))}")
            sys.exit(1)
        localization_files = {target_language: localization_files[target_language]}

    # Collect all missing strings across all languages
    all_missing = []  # List of tuples: (string_id, language_code)

    for lang_code, file_path in sorted(localization_files.items()):
        print(f"\nChecking {lang_code}...")
        
        localized_strings = extract_strings_from_xml(file_path)
        missing_strings = english_strings - localized_strings

        if missing_strings:
            print(f"Found {len(missing_strings)} missing strings in {lang_code}")
            for string_name in sorted(missing_strings):
                all_missing.append((string_name, lang_code))
        else:
            print(f"All strings present in {lang_code} (no missing strings)")

    if not all_missing:
        print("\nNo missing strings found in any language!")
        return

    # Randomly select one missing string
    selected_string, language = random.choice(all_missing)
    
    # Get the English value for reference
    english_value = find_string_value_in_xml(english_file, selected_string)
    
    print(f"\n--- Randomly Selected Missing String ---")
    print(f"String ID: {selected_string}")
    print(f"Missing in language: {language}")
    print(f"English value: {english_value if english_value else 'NOT FOUND'}")
    
    # Provide command to check usage
    print(f"\nTo check usage of this string in code:")
    print(f"python3 tools/find_string_usage.py {selected_string} {base_path}")


if __name__ == "__main__":
    main()