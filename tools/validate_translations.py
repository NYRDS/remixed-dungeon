#!/usr/bin/env python3
"""
Script to validate that translated strings are properly formatted and saved in XML files.
Checks for common issues like:
- Malformed XML
- Missing translations
- Incorrect gender values
- Special character issues
"""

import os
import re
import sys
import xml.etree.ElementTree as ET
from pathlib import Path


def auto_fix_trivial_issues(file_path):
    """
    Automatically fix trivial escape issues in the XML file.
    Returns True if changes were made, False otherwise.
    Operates at the XML level to avoid breaking XML structure.
    """
    try:
        # Parse the XML file
        tree = ET.parse(file_path)
        root = tree.getroot()

        changes_made = False

        # Process each string element for escape issues
        for string_elem in root.findall('string'):
            original_text = string_elem.text
            if original_text is None:
                continue

            fixed_text = original_text

            # Fix wrapped quotes around entire string content (e.g., "This is a quote")
            # This removes the outer quotes if the entire string is wrapped in them
            if (fixed_text.startswith('"') and fixed_text.endswith('"') and len(fixed_text) >= 2):
                # Check if it's actually a string wrapped in quotes (not just a string that happens to start/end with ")
                # This is a simple heuristic: if there are no other quotes in the middle, it's likely wrapped
                inner_content = fixed_text[1:-1]  # Remove first and last character
                if '"' not in inner_content or inner_content.count('"') < fixed_text.count('"'):
                    fixed_text = inner_content
                    changes_made = True

            # Fix unescaped ampersands that are not part of XML entities
            fixed_text = re.sub(r'&(?![a-zA-Z]+;|#[0-9]+;|#x[0-9a-fA-F]+;)', '&amp;', fixed_text)

            # Fix unescaped apostrophes in any language (should be \' or &apos;)
            # This targets standalone apostrophes not already escaped with \ or part of &apos; entity
            # Look for apostrophes that are not preceded by \ and not part of &apos; entity
            i = 0
            temp_fixed_text = ""
            while i < len(fixed_text):
                if fixed_text[i] == "'" and (i == 0 or fixed_text[i-1] != '\\'):
                    # Check if this is part of &apos; entity
                    is_entity = (i >= 5 and fixed_text[i-5:i+2] == '&apos;') or \
                               (i >= 4 and fixed_text[i-4:i+2] == '&apo') or \
                               (i >= 3 and fixed_text[i-3:i+2] == '&ap') or \
                               (i >= 2 and fixed_text[i-2:i+2] == '&a') or \
                               (i < len(fixed_text)-6 and fixed_text[i:i+7] == '&apos;') or \
                               (i < len(fixed_text)-5 and fixed_text[i:i+6] == '&apos') or \
                               (i < len(fixed_text)-4 and fixed_text[i:i+5] == '&apos') or \
                               (i < len(fixed_text)-3 and fixed_text[i:i+4] == '&apo')

                    if not is_entity:
                        temp_fixed_text += "\\'"
                    else:
                        temp_fixed_text += fixed_text[i]
                else:
                    temp_fixed_text += fixed_text[i]
                i += 1
            fixed_text = temp_fixed_text

            # Fix unescaped quotes in any language (should be \")
            # Only escape quotes that are not already escaped
            # Prefer \" over &quot; for Android string resources
            fixed_text = re.sub(r'(?<!\\)"', '\\"', fixed_text)

            # Fix unescaped @ symbols (these need to be escaped in Android XML as \@)
            fixed_text = re.sub(r'(?<!\\)@', '\\@', fixed_text)

            # Fix unescaped ? symbols at the beginning of a string value
            if fixed_text.startswith('?'):
                fixed_text = '\\' + fixed_text

            # Fix invalid unicode escape sequences like {str}
            fixed_text = re.sub(r'\{str\}', '', fixed_text)  # Remove {str} sequences

            # Update the element text if changes were made
            if fixed_text != original_text:
                string_elem.text = fixed_text
                changes_made = True

        # Remove duplicate string entries
        seen_names = set()
        elements_to_remove = []

        for string_elem in root.findall('string'):
            name = string_elem.get('name')
            if name in seen_names:
                elements_to_remove.append(string_elem)
                changes_made = True
            else:
                seen_names.add(name)

        # Remove duplicate elements
        for elem in elements_to_remove:
            root.remove(elem)

        # Write the fixed XML back to the file if changes were made
        if changes_made:
            # Write with proper XML declaration and formatting
            # Use xml_declaration=True to ensure proper XML format
            tree.write(file_path, encoding='utf-8', xml_declaration=True)
            if elements_to_remove:
                print(f"Auto-fixed trivial escape issues and removed {len(elements_to_remove)} duplicate entries in {file_path}")
            else:
                print(f"Auto-fixed trivial escape issues in {file_path}")
            return True
        else:
            print(f"No trivial escape issues found in {file_path}")
            return False

    except ET.ParseError as e:
        print(f"XML Parse Error in {file_path} during auto-fix: {e}")
        # If we can't parse the XML, we can't fix it at the XML level
        # We could try a more robust approach, but for now just return False
        return False
    except Exception as e:
        print(f"Error processing {file_path}: {e}")
        return False


def validate_xml_format(file_path):
    """
    Validate that the XML file is properly formatted.
    Returns True if valid, False otherwise.
    """
    try:
        tree = ET.parse(file_path)
        return True
    except ET.ParseError as e:
        print(f"XML Parse Error in {file_path}: {e}")
        return False


def validate_gender_values(file_path):
    """
    Validate that all _Gender strings have correct values (feminine, masculine, neuter).
    Returns True if all values are valid, False otherwise.
    """
    try:
        tree = ET.parse(file_path)
        root = tree.getroot()
        
        valid_genders = {'feminine', 'masculine', 'neuter'}
        all_valid = True
        
        for string_elem in root.findall('string'):
            name = string_elem.get('name')
            value = string_elem.text
            
            if name and name.endswith('_Gender'):
                if value not in valid_genders:
                    print(f"Invalid gender value '{value}' for string '{name}' in {file_path}")
                    all_valid = False
        
        return all_valid
    except ET.ParseError as e:
        print(f"XML Parse Error in {file_path} during gender validation: {e}")
        return False


def validate_special_characters(file_path):
    """
    Validate that special characters are properly escaped in the XML.
    Returns True if valid, False otherwise.
    """
    try:
        tree = ET.parse(file_path)
        root = tree.getroot()

        all_valid = True

        for string_elem in root.findall('string'):
            name = string_elem.get('name')
            value = string_elem.text or ""

            # Check for wrapped quotes around entire string content (e.g., "This is a quote")
            if (value.startswith('"') and value.endswith('"') and len(value) >= 2):
                # Check if it's actually a string wrapped in quotes (not just a string that happens to start/end with ")
                # This is a simple heuristic: if there are no other quotes in the middle, it's likely wrapped
                inner_content = value[1:-1]  # Remove first and last character
                if '"' not in inner_content or inner_content.count('"') < value.count('"'):
                    print(f"Error: String content wrapped in quotes found in string '{name}' in {file_path}: '{value}'. Quotes should not wrap entire string content in Android XML.")
                    all_valid = False

            # Check for unescaped quotes in any language (should be \")
            # Properly identify quotes that are NOT escaped with \
            i = 0
            while i < len(value):
                if value[i] == '"':
                    # Check if this quote is escaped with backslash
                    is_escaped = (i > 0 and value[i-1] == '\\')

                    if not is_escaped:
                        print(f"Error: Unescaped quote found in string '{name}' in {file_path}: '{value}'. Use \\\" instead.")
                        all_valid = False
                        break
                i += 1

            # Check for unescaped apostrophes in any language (should be \' or &apos;)
            # This is particularly important for contractions in various languages
            # Check for apostrophes that are not preceded by \ and not part of &apos; entity
            i = 0
            while i < len(value):
                if value[i] == "'" and (i == 0 or value[i-1] != '\\'):
                    # Check if this is part of &apos; entity
                    is_entity = (i >= 5 and value[i-5:i+2] == '&apos;') or \
                               (i >= 4 and value[i-4:i+2] == '&apo') or \
                               (i >= 3 and value[i-3:i+2] == '&ap') or \
                               (i >= 2 and value[i-2:i+2] == '&a') or \
                               (i < len(value)-6 and value[i:i+7] == '&apos;') or \
                               (i < len(value)-5 and value[i:i+6] == '&apos') or \
                               (i < len(value)-4 and value[i:i+5] == '&apos') or \
                               (i < len(value)-3 and value[i:i+4] == '&apo')

                    if not is_entity:
                        print(f"Error: Unescaped apostrophe found in string '{name}' in {file_path}: '{value}'. Use \\' or &apos; instead.")
                        all_valid = False
                        break
                i += 1

            # Check for unescaped quotes in any language (should be \")
            # Properly identify quotes that are NOT escaped with \
            i = 0
            while i < len(value):
                if value[i] == '"':
                    # Check if this quote is escaped with backslash
                    is_escaped = (i > 0 and value[i-1] == '\\')

                    if not is_escaped:
                        print(f"Error: Unescaped quote found in string '{name}' in {file_path}: '{value}'. Use \\\" instead.")
                        all_valid = False
                        break
                i += 1

            # Check for unescaped ampersands that are not part of XML entities
            unescaped_ampersands = re.findall(r'&(?![a-zA-Z]+;|#[0-9]+;|#x[0-9a-fA-F]+;)', value)
            if unescaped_ampersands:
                print(f"Error: Unescaped ampersand found in string '{name}' in {file_path}: '{value}'. Use &amp; instead.")
                all_valid = False

            # Check for unescaped @ symbols (these need to be escaped in Android XML as \@)
            unescaped_at = re.findall(r'(?<!\\)@', value)
            if unescaped_at:
                print(f"Error: Unescaped @ symbol found in string '{name}' in {file_path}: '{value}'. Use \\@ instead.")
                all_valid = False

            # Check for unescaped ? symbols at the beginning of a string (these need to be escaped in Android XML as \?)
            if value.startswith('?'):
                print(f"Error: Unescaped ? symbol at beginning of string '{name}' in {file_path}: '{value}'. Use \\? instead.")
                all_valid = False

        return all_valid
    except ET.ParseError as e:
        print(f"XML Parse Error in {file_path} during special character validation: {e}")
        return False


def validate_android_specific_formatting(file_path):
    """
    Validate Android-specific string formatting requirements.
    Returns True if valid, False otherwise.
    """
    try:
        tree = ET.parse(file_path)
        root = tree.getroot()

        all_valid = True

        for string_elem in root.findall('string'):
            name = string_elem.get('name')
            value = string_elem.text or ""

            # Check for proper handling of whitespace - if string starts/ends with space, it should be quoted
            if (value.startswith(' ') or value.endswith(' ')) and not (value.startswith('"') and value.endswith('"')):
                # This is a warning, not an error, as Android handles this differently
                pass

            # Check for properly closed HTML tags within strings
            # Find all HTML-like tags
            html_tags = re.findall(r'<(/?)(\w+)([^>]*?)>', value)
            opened_tags = []
            for is_closing, tag_name, attrs in html_tags:
                if not is_closing:
                    opened_tags.append(tag_name)
                elif is_closing and tag_name in opened_tags:
                    opened_tags.remove(tag_name)

            if opened_tags:
                print(f"Error: Unclosed HTML tags {opened_tags} found in string '{name}' in {file_path}: '{value}'")
                all_valid = False

        return all_valid
    except ET.ParseError as e:
        print(f"XML Parse Error in {file_path} during Android-specific validation: {e}")
        return False


def validate_duplicate_strings(file_path):
    """
    Validate that there are no duplicate string names in the XML file.
    Returns True if no duplicates found, False otherwise.
    """
    try:
        tree = ET.parse(file_path)
        root = tree.getroot()

        string_names = []
        duplicates_found = False

        for string_elem in root.findall('string'):
            name = string_elem.get('name')

            if name in string_names:
                print(f"Duplicate string name '{name}' found in {file_path}")
                duplicates_found = True
            else:
                string_names.append(name)

        return not duplicates_found
    except ET.ParseError as e:
        print(f"XML Parse Error in {file_path} during duplicate validation: {e}")
        return False


def validate_string_formatting(file_path):
    """
    Validate common string formatting issues like placeholders.
    Returns True if valid, False otherwise.
    """
    try:
        tree = ET.parse(file_path)
        root = tree.getroot()

        all_valid = True

        for string_elem in root.findall('string'):
            name = string_elem.get('name')
            value = string_elem.text or ""

            # Check for potential issues with placeholders
            if '%' in value:
                # Find all placeholder patterns like %s, %d, %f, %1$s, %2$s, etc.
                placeholders = re.findall(r'%\d*\$?[sdft]', value)

                # Check if there are multiple placeholders without positional identifiers
                simple_placeholders = [p for p in placeholders if p.startswith('%') and not '$' in p]

                if len(simple_placeholders) > 1:
                    # If there are multiple simple placeholders (without positions), this could cause Android build issues
                    print(f"Error: Multiple non-positional placeholders in string '{name}' in {file_path}: {simple_placeholders}. Must use positional identifiers like %1$s, %2$s, etc.")
                    all_valid = False

                # Check for potential malformed placeholders
                malformed_placeholders = re.findall(r'%([^sdft\d\.\[\]$]|(?<=%)[\s]|$)', value)
                if malformed_placeholders and not all(p in ['%', ' '] for p in malformed_placeholders):
                    # This is just informational, not a validation failure
                    # Different languages might have different placeholder usage
                    pass

                # Check for potential invalid unicode escape sequences (common issue seen as {str})
                if '{str}' in value:
                    print(f"Error: Invalid unicode escape sequence '{{str}}' found in string '{name}' in {file_path}")
                    all_valid = False

            # Check for properly formatted Unicode escapes (should be \uXXXX format)
            # Look for improperly formatted Unicode sequences
            improper_unicode = re.findall(r'\\u([0-9A-Fa-f]{0,3})($|[^0-9A-Fa-f])', value)
            if improper_unicode:
                print(f"Error: Improperly formatted Unicode escape sequence in string '{name}' in {file_path}: '{value}'. Use \\uXXXX format.")
                all_valid = False

            # Check for invalid unicode escape sequences like {str}
            invalid_unicode_seq = re.findall(r'\{str\}', value)
            if invalid_unicode_seq:
                print(f"Error: Invalid unicode escape sequence '{{str}}' found in string '{name}' in {file_path}: '{value}'.")
                all_valid = False

        return all_valid
    except ET.ParseError as e:
        print(f"XML Parse Error in {file_path} during formatting validation: {e}")
        return False


def validate_all_localization_files(base_path, auto_fix=False):
    """
    Validate all localization files in the project.
    """
    values_dir = os.path.join(base_path, "RemixedDungeon", "src", "main", "res")

    if not os.path.exists(values_dir):
        print(f"Values directory does not exist: {values_dir}")
        return False

    all_valid = True

    # Find all values directories
    for dir_name in os.listdir(values_dir):
        if dir_name.startswith("values"):
            lang_dir = os.path.join(values_dir, dir_name)
            if os.path.isdir(lang_dir):
                strings_file = os.path.join(lang_dir, "strings_all.xml")

                if os.path.exists(strings_file):
                    print(f"Validating {strings_file}...")

                    # Auto-fix trivial issues if requested
                    if auto_fix:
                        auto_fix_trivial_issues(strings_file)

                    # Run all validation checks
                    xml_valid = validate_xml_format(strings_file)
                    gender_valid = validate_gender_values(strings_file)
                    char_valid = validate_special_characters(strings_file)
                    format_valid = validate_string_formatting(strings_file)
                    android_valid = validate_android_specific_formatting(strings_file)
                    duplicate_valid = validate_duplicate_strings(strings_file)

                    file_all_valid = xml_valid and gender_valid and char_valid and format_valid and android_valid and duplicate_valid

                    if file_all_valid:
                        print(f"  ✓ All checks passed for {strings_file}")
                    else:
                        all_valid = False
                        print(f"  ✗ Some checks failed for {strings_file}")
                else:
                    print(f"  Warning: strings_all.xml not found in {lang_dir}")

    return all_valid


def main():
    if len(sys.argv) < 2:
        print("Usage: python validate_translations.py <project_base_path> [--auto-fix]")
        print("Example: python validate_translations.py /path/to/remixed-dungeon")
        print("Example with auto-fix: python validate_translations.py /path/to/remixed-dungeon --auto-fix")
        sys.exit(1)

    base_path = sys.argv[1]
    auto_fix = "--auto-fix" in sys.argv

    if not os.path.exists(base_path):
        print(f"Project path does not exist: {base_path}")
        sys.exit(1)

    print("Starting validation of localization files...")
    if auto_fix:
        print("Auto-fix mode enabled for trivial escape issues")
    print("=" * 50)

    all_valid = validate_all_localization_files(base_path, auto_fix)

    print("=" * 50)
    if all_valid:
        print("✓ All localization files passed validation!")
        sys.exit(0)
    else:
        print("✗ Some localization files failed validation!")
        if auto_fix:
            print("Note: Auto-fix was applied, but some non-trivial issues may remain")
        sys.exit(1)


if __name__ == "__main__":
    main()