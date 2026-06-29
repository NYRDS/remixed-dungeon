#!/usr/bin/env python3
"""
Sort strings in Android XML resource files by key in lexicographic order.

Usage:
    python3 tools/sort_strings_xml.py <project_base_path> [--lang LANG_CODE]
    python3 tools/sort_strings_xml.py /path/to/remixed-dungeon
    python3 tools/sort_strings_xml.py /path/to/remixed-dungeon --lang ru
    python3 tools/sort_strings_xml.py /path/to/remixed-dungeon --all
"""

import os
import sys
import argparse
import xml.etree.ElementTree as ET
from xml.dom import minidom


def sort_strings_in_file(file_path):
    """Sort string elements in an XML file by name attribute."""
    try:
        tree = ET.parse(file_path)
        root = tree.getroot()
    except ET.ParseError as e:
        print(f"Error parsing XML file {file_path}: {e}")
        return False

    # Collect all string elements
    strings = []
    for string_elem in root.findall('string'):
        name = string_elem.get('name')
        if name:
            strings.append((name, string_elem))

    if not strings:
        return False

    # Sort by name (lexicographic order)
    strings.sort(key=lambda x: x[0])

    # Clear root and re-add in sorted order
    root.clear()
    root.tag = 'resources'
    for name, elem in strings:
        root.append(elem)

    # Write back with pretty formatting
    rough_string = ET.tostring(root, encoding='unicode')
    reparsed = minidom.parseString(rough_string)
    pretty_xml = reparsed.toprettyxml(indent="    ")[23:]  # Remove XML declaration

    # Remove extra blank lines
    lines = pretty_xml.split('\n')
    filtered_lines = [line for line in lines if line.strip() or line == lines[-1]]
    pretty_xml = '\n'.join(filtered_lines)

    with open(file_path, 'w', encoding='utf-8') as f:
        f.write(pretty_xml)

    return True


def find_localization_files(base_path, target_lang=None):
    """Find all strings_all.xml files in localization directories."""
    localization_files = {}

    # English (default values/ directory)
    english_file = os.path.join(base_path, "RemixedDungeon", "src", "main", "res", "values", "strings_all.xml")
    if os.path.exists(english_file):
        localization_files["en"] = english_file

    res_dir = os.path.join(base_path, "RemixedDungeon", "src", "main", "res")

    for dir_name in os.listdir(res_dir):
        if dir_name.startswith("values-") and os.path.isdir(os.path.join(res_dir, dir_name)):
            strings_file = os.path.join(res_dir, dir_name, "strings_all.xml")
            if os.path.exists(strings_file):
                lang_code = dir_name.replace("values-", "")
                if target_lang is None or lang_code == target_lang or (target_lang == "en" and lang_code == "en"):
                    localization_files[lang_code] = strings_file

    return localization_files


def main():
    parser = argparse.ArgumentParser(description="Sort Android string resource files by key")
    parser.add_argument("project_path", help="Path to project root")
    parser.add_argument("--lang", help="Specific language code to sort (e.g., ru, es). Default: all languages")
    parser.add_argument("--all", action="store_true", help="Sort all languages (default behavior)")

    args = parser.parse_args()

    base_path = args.project_path
    target_lang = args.lang

    if not os.path.exists(base_path):
        print(f"Project path does not exist: {base_path}")
        sys.exit(1)

    localization_files = find_localization_files(base_path, target_lang)

    if not localization_files:
        print("No localization files found")
        sys.exit(1)

    print(f"Found {len(localization_files)} localization file(s) to sort")

    sorted_count = 0
    for lang_code, file_path in sorted(localization_files.items()):
        print(f"Sorting {lang_code}: {file_path}")
        if sort_strings_in_file(file_path):
            sorted_count += 1
            print(f"  ✓ Sorted")
        else:
            print(f"  ✗ Failed or no strings to sort")

    print(f"\nDone. Sorted {sorted_count} file(s).")


if __name__ == "__main__":
    main()