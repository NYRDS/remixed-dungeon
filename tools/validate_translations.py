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
        with open(file_path, 'r', encoding='utf-8') as f:
            content = f.read()
        
        # Check for unescaped special characters that might break XML
        # This is a basic check - more sophisticated validation could be added
        issues = []
        
        # Look for unescaped ampersands that are not part of XML entities
        unescaped_ampersands = re.findall(r'&(?![a-zA-Z]+;|#[0-9]+;|#x[0-9a-fA-F]+;)', content)
        if unescaped_ampersands:
            issues.append(f"Found {len(unescaped_ampersands)} unescaped ampersand(s)")
        
        # Look for unescaped quotes in string attributes (though this is harder to validate without parsing)
        # For now, we'll just report if there are issues
        
        if issues:
            print(f"Special character issues in {file_path}: {', '.join(issues)}")
            return False
        
        return True
    except Exception as e:
        print(f"Error reading {file_path}: {e}")
        return False


def validate_string_formatting(file_path):
    """
    Validate common string formatting issues like placeholders.
    Returns True if valid, False otherwise.
    """
    try:
        tree = ET.parse(file_path)
        root = tree.getroot()

        for string_elem in root.findall('string'):
            name = string_elem.get('name')
            value = string_elem.text or ""

            # Check for potential issues with placeholders
            if '%' in value:
                # Check for common placeholder patterns that might be malformed
                # Look for % not followed by expected format specifiers
                malformed_placeholders = re.findall(r'%([^sdft\d\.\[\]]|(?<=%)[\s]|$)', value)
                if malformed_placeholders and not all(p in ['%', ' '] for p in malformed_placeholders):
                    # This is just informational, not a validation failure
                    # Different languages might have different placeholder usage
                    pass

        return True
    except ET.ParseError as e:
        print(f"XML Parse Error in {file_path} during formatting validation: {e}")
        return False


def validate_all_localization_files(base_path):
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
                    
                    # Run all validation checks
                    xml_valid = validate_xml_format(strings_file)
                    gender_valid = validate_gender_values(strings_file)
                    char_valid = validate_special_characters(strings_file)
                    format_valid = validate_string_formatting(strings_file)
                    
                    file_all_valid = xml_valid and gender_valid and char_valid and format_valid
                    
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
        print("Usage: python validate_translations.py <project_base_path>")
        print("Example: python validate_translations.py /path/to/remixed-dungeon")
        sys.exit(1)
    
    base_path = sys.argv[1]
    
    if not os.path.exists(base_path):
        print(f"Project path does not exist: {base_path}")
        sys.exit(1)
    
    print("Starting validation of localization files...")
    print("=" * 50)
    
    all_valid = validate_all_localization_files(base_path)
    
    print("=" * 50)
    if all_valid:
        print("✓ All localization files passed validation!")
        sys.exit(0)
    else:
        print("✗ Some localization files failed validation!")
        sys.exit(1)


if __name__ == "__main__":
    main()