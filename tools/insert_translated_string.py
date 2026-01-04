#!/usr/bin/env python3
"""
Script to insert translated strings into localization XML files with validation.
Enforces rules for _Gender suffix (only values allowed: feminine, masculine, neuter).
"""

import os
import re
import sys
import xml.etree.ElementTree as ET
from xml.dom import minidom


def validate_gender_value(value):
    """
    Validate that the gender value is one of the allowed values in lowercase.
    Returns True if valid, False otherwise.
    """
    allowed_genders = {'feminine', 'masculine', 'neuter'}
    return value.strip() in allowed_genders  # Check exact case (lowercase) match


def insert_string_in_xml(file_path, string_id, string_value, sort_strings=True):
    """
    Insert a string into an XML file.
    If the string already exists, it updates the value.
    If sort_strings is True, sorts the strings alphabetically by name.
    """
    # Parse the existing XML file
    try:
        tree = ET.parse(file_path)
        root = tree.getroot()
    except ET.ParseError:
        print(f"Error parsing XML file {file_path}. Creating a new one.")
        root = ET.Element("resources")
        tree = ET.ElementTree(root)

    # Check if the string already exists
    existing_string = None
    for string_elem in root.findall('string'):
        name = string_elem.get('name')
        if name == string_id:
            existing_string = string_elem
            break

    # Validate _Gender suffix if applicable
    if string_id.endswith('_Gender'):
        if not validate_gender_value(string_value):
            raise ValueError(
                f"Invalid gender value '{string_value}' for string ID '{string_id}'. "
                f"Allowed values are: feminine, masculine, neuter (lowercase only)."
            )
        # Ensure gender values are lowercase
        string_value = string_value.lower()

    # If string exists, update its value
    if existing_string is not None:
        existing_string.text = string_value
        print(f"Updated string '{string_id}' in {file_path}")
    else:
        # Create a new string element
        new_string = ET.SubElement(root, 'string', name=string_id)
        new_string.text = string_value
        print(f"Added string '{string_id}' to {file_path}")

    # Sort strings alphabetically if requested
    if sort_strings:
        # Create a list of (name, element) tuples
        string_list = []
        for string_elem in root.findall('string'):
            name = string_elem.get('name')
            string_list.append((name, string_elem))

        # Sort by name
        string_list.sort(key=lambda x: x[0])

        # Clear the root and add elements back in sorted order
        root.clear()
        root.tag = 'resources'  # Restore the root tag
        for name, elem in string_list:
            root.append(elem)

    # Write the XML back to the file with proper formatting
    rough_string = ET.tostring(root, encoding='unicode')
    reparsed = minidom.parseString(rough_string)
    pretty_xml = reparsed.toprettyxml(indent="    ")[23:]  # Remove XML declaration
    
    # Remove extra blank lines
    lines = pretty_xml.split('\n')
    filtered_lines = [line for line in lines if line.strip() or line == lines[-1]]
    pretty_xml = '\n'.join(filtered_lines)
    
    with open(file_path, 'w', encoding='utf-8') as f:
        f.write(pretty_xml)


def main():
    if len(sys.argv) < 4:
        print("Usage: python insert_translated_string.py <lang_code> <string_id> <string_value> [project_base_path] [--no-sort]")
        print("Example: python insert_translated_string.py ru Hero_Name \"Герой\"")
        print("Example with custom project path: python insert_translated_string.py ru Hero_Name \"Герой\" /path/to/project")
        print("Example with no sorting: python insert_translated_string.py ru Hero_Name \"Герой\" --no-sort")
        sys.exit(1)

    lang_code = sys.argv[1]
    string_id = sys.argv[2]
    string_value = sys.argv[3]

    # Check if the last argument is --no-sort
    sort_strings = True
    base_path = os.getcwd()  # Default to current working directory

    # Check if there's an optional project path and/or --no-sort flag
    if len(sys.argv) >= 5:
        if sys.argv[4] == "--no-sort":
            sort_strings = False
        else:
            base_path = sys.argv[4]

    if len(sys.argv) >= 6 and sys.argv[5] == "--no-sort":
        sort_strings = False

    # Validate language code format
    # Support both standard format (e.g., 'pt-BR') and Android resource format (e.g., 'pt-rBR', 'zh-rTW')
    if not re.match(r'^[a-z]{2,3}(?:-[A-Z]{2}|-[a-z][A-Z]{2})?$', lang_code):
        print(f"Invalid language code format: {lang_code}. Expected format: 'ru', 'es', 'pt-BR', 'pt-rBR', 'zh-rTW', etc.")
        sys.exit(1)

    # Path to the target localization file
    target_dir = os.path.join(base_path, "RemixedDungeon", "src", "main", "res", f"values-{lang_code}")
    target_file = os.path.join(target_dir, "strings_all.xml")

    # Create directory if it doesn't exist
    os.makedirs(target_dir, exist_ok=True)

    # Create a basic XML file if it doesn't exist
    if not os.path.exists(target_file):
        with open(target_file, 'w', encoding='utf-8') as f:
            f.write('<?xml version="1.0" encoding="utf-8"?>\n<resources>\n</resources>\n')
        print(f"Created new strings file: {target_file}")

    try:
        # Insert the string into the XML file
        insert_string_in_xml(target_file, string_id, string_value, sort_strings)
        print(f"Successfully added/updated string '{string_id}' in {lang_code} localization")
    except ValueError as e:
        print(f"Error: {e}")
        sys.exit(1)
    except Exception as e:
        print(f"Unexpected error: {e}")
        sys.exit(1)


if __name__ == "__main__":
    main()