#!/usr/bin/env python3
"""
Script to find English and Russian values for a given string ID and show its usage in Java and Lua code with context.
"""

import os
import re
import sys
import xml.etree.ElementTree as ET
from pathlib import Path


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


def search_in_files(directory, string_id, file_extensions, context_lines=5):
    """
    Search for a string ID in files with specified extensions in a directory.
    Returns a list of matches with context.
    """
    results = []

    # Pattern to match the string ID in code (e.g., TXT("string_id"), getString(R.string.string_id), etc.)
    # For Java files
    java_patterns = [
        # TXT("string_id")
        r'(TXT\(["\']' + re.escape(string_id) + r'["\']\))',
        # TXT(R.string.string_id)
        r'(TXT\(R\.string\.' + re.escape(string_id) + r'\))',
        # getString(R.string.string_id)
        r'(getString\(R\.string\.' + re.escape(string_id) + r'\))',
        # R.string.string_id (exact match, not as part of another string)
        r'(R\.string\.' + re.escape(string_id) + r')(?!\w)',
    ]

    # For Lua files
    lua_patterns = [
        # exact string_id as a standalone identifier
        r'\b' + re.escape(string_id) + r'\b',
        # exact string_id in quotes (not as part of another string)
        r'(["\']' + re.escape(string_id) + r'["\'])',
    ]

    # Select patterns based on file type
    all_patterns = []
    if '.java' in file_extensions:
        all_patterns = java_patterns
    elif '.lua' in file_extensions:
        all_patterns = lua_patterns
    else:
        # If mixed extensions, use both
        all_patterns = java_patterns + lua_patterns

    for root_dir, dirs, files in os.walk(directory):
        for file in files:
            if any(file.endswith(ext) for ext in file_extensions):
                file_path = os.path.join(root_dir, file)

                try:
                    with open(file_path, 'r', encoding='utf-8') as f:
                        lines = f.readlines()

                    for i, line in enumerate(lines):
                        for pattern in all_patterns:
                            matches = re.finditer(pattern, line)
                            for match in matches:
                                # Get context lines
                                start = max(0, i - context_lines)
                                end = min(len(lines), i + context_lines + 1)
                                context = ''.join(lines[start:end]).strip()

                                results.append({
                                    'file': file_path,
                                    'line_number': i + 1,
                                    'match': match.group(0),
                                    'context': context
                                })
                except UnicodeDecodeError:
                    # Skip files that can't be read as UTF-8
                    continue
                except Exception:
                    # Skip other problematic files
                    continue

    return results


def find_java_class_files(directory, class_name):
    """
    Find Java files that contain a class with the given name.
    Returns a list of file paths.
    """
    results = []

    # Pattern to match class declaration
    class_pattern = r'(?:^|\s)(?:public|protected|private)?\s*(?:static\s+)?(?:abstract\s+)?(?:final\s+)?(?:class|interface|enum)\s+' + re.escape(class_name) + r'\b'

    for root_dir, dirs, files in os.walk(directory):
        for file in files:
            if file.endswith('.java'):
                file_path = os.path.join(root_dir, file)

                try:
                    with open(file_path, 'r', encoding='utf-8') as f:
                        content = f.read()

                    if re.search(class_pattern, content, re.MULTILINE):
                        results.append(file_path)
                except UnicodeDecodeError:
                    # Skip files that can't be read as UTF-8
                    continue
                except Exception:
                    # Skip other problematic files
                    continue

    return results


def main():
    if len(sys.argv) < 2:
        print("Usage: python find_string_usage.py <string_id> [project_base_path]")
        print("Example: python find_string_usage.py WndJournal_Levels /path/to/remixed-dungeon")
        sys.exit(1)

    string_id = sys.argv[1]
    base_path = sys.argv[2] if len(sys.argv) > 2 else "/home/mike/StudioProjects/remixed-dungeon"

    # Paths to English and Russian strings files
    english_file = os.path.join(base_path, "RemixedDungeon", "src", "main", "res", "values", "strings_all.xml")
    russian_file = os.path.join(base_path, "RemixedDungeon", "src", "main", "res", "values-ru", "strings_all.xml")

    # Check if files exist
    if not os.path.exists(english_file):
        print(f"English strings file not found: {english_file}")
        sys.exit(1)

    if not os.path.exists(russian_file):
        print(f"Russian strings file not found: {russian_file}")
        sys.exit(1)

    # Find string values
    english_value = find_string_value_in_xml(english_file, string_id)
    russian_value = find_string_value_in_xml(russian_file, string_id)

    print(f"String ID: {string_id}")
    print(f"English value: {english_value if english_value else 'NOT FOUND'}")
    print(f"Russian value: {russian_value if russian_value else 'NOT FOUND'}")
    print()

    # Search in Java files
    java_dir = os.path.join(base_path, "RemixedDungeon", "src", "main", "java")
    print("Java file usage:")
    java_results = search_in_files(java_dir, string_id, ['.java'])

    if java_results:
        # Group results by file
        files_results = {}
        for result in java_results:
            file_path = result['file']
            if file_path not in files_results:
                files_results[file_path] = []
            files_results[file_path].append(result)

        for file_path, results in files_results.items():
            print(f"  File: {file_path}")
            for result in results:
                print(f"    Line: {result['line_number']}")
                print(f"    Match: {result['match']}")
                print(f"    Context:\n{result['context']}")
                print("    " + "-" * 50)
    else:
        print("  No usage found in Java files")

    # Check for implicit usage - if string_id follows format Entity_Something, search for Entity class
    entity_part = string_id.split('_', 1)[0] if '_' in string_id else None
    if entity_part:
        print(f"  Looking for implicit usage in Java class: {entity_part}")
        class_files = find_java_class_files(java_dir, entity_part)
        if class_files:
            for class_file in class_files:
                print(f"    Found class {entity_part} in: {class_file}")
                # Read and print the full content of the class file
                try:
                    with open(class_file, 'r', encoding='utf-8') as f:
                        content = f.read()
                    print(f"    Class content:\n{content[:2000]}...")  # Limit output to first 2000 chars
                    if len(content) > 2000:
                        print("    ... (content truncated)")
                except Exception as e:
                    print(f"    Could not read file: {e}")
        else:
            print(f"  No Java class {entity_part} found")
    print()

    # Search in Lua files
    lua_dir = os.path.join(base_path, "RemixedDungeon", "src", "main", "assets", "scripts")
    print("Lua file usage:")
    lua_results = search_in_files(lua_dir, string_id, ['.lua'])

    if lua_results:
        # Group results by file
        files_results = {}
        for result in lua_results:
            file_path = result['file']
            if file_path not in files_results:
                files_results[file_path] = []
            files_results[file_path].append(result)

        for file_path, results in files_results.items():
            print(f"  File: {file_path}")
            for result in results:
                print(f"    Line: {result['line_number']}")
                print(f"    Match: {result['match']}")
                print(f"    Context:\n{result['context']}")
                print("    " + "-" * 50)
    else:
        print("  No usage found in Lua files")


if __name__ == "__main__":
    main()