#!/usr/bin/env python3
"""
Script to select a random string ID and check its translations across all languages.
"""

import os
import sys
import random
import xml.etree.ElementTree as ET
from collections import defaultdict


def extract_strings_from_xml(file_path):
    """
    Extract all string names and values from an XML file.
    Returns a dictionary mapping string names to values.
    """
    try:
        tree = ET.parse(file_path)
        root = tree.getroot()

        strings = {}
        for string_elem in root.findall('string'):
            name = string_elem.get('name')
            value = string_elem.text
            if name:
                strings[name] = value

        return strings
    except ET.ParseError as e:
        print(f"Error parsing XML file {file_path}: {e}")
        return {}


def find_localization_files(base_path):
    """
    Find all strings_all.xml files in localization directories.
    Returns a dictionary mapping language codes to file paths.
    """
    localization_files = {}
    english_file = os.path.join(base_path, "RemixedDungeon", "src", "main", "res", "values", "strings_all.xml")

    if os.path.exists(english_file):
        localization_files["en"] = english_file

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
        print("Usage: python check_random_string_translation.py <project_base_path>")
        print("Example: python check_random_string_translation.py /path/to/remixed-dungeon")
        sys.exit(1)

    base_path = sys.argv[1]

    # Find all localization files
    localization_files = find_localization_files(base_path)
    
    if not localization_files:
        print("No localization files found.")
        sys.exit(1)

    # Load all string dictionaries
    all_strings = {}
    for lang_code, file_path in localization_files.items():
        all_strings[lang_code] = extract_strings_from_xml(file_path)

    # Get all string IDs from English (as reference)
    english_strings = all_strings.get("en", {})
    if not english_strings:
        # If English not available, use the first available language
        first_lang = next(iter(all_strings.keys()))
        english_strings = all_strings[first_lang]
        print(f"English strings not found, using {first_lang} as reference")

    string_ids = list(english_strings.keys())
    
    if not string_ids:
        print("No strings found in the reference language.")
        sys.exit(1)

    # Select a random string ID
    random_string_id = random.choice(string_ids)
    
    print(f"Selected random string ID: {random_string_id}")
    print("="*60)
    
    # Check translations for this string ID across all languages
    for lang_code in sorted(localization_files.keys()):
        string_dict = all_strings[lang_code]
        if random_string_id in string_dict:
            translation = string_dict[random_string_id]
            print(f"{lang_code:6}: {translation}")
        else:
            print(f"{lang_code:6}: [MISSING]")


if __name__ == "__main__":
    main()