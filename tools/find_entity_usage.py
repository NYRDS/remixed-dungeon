#!/usr/bin/env python3
"""
Script to find usage of game entities (mobs, items, spells, etc.) in Java and Lua code with context.
This is helpful for wiki maintenance to understand how entities are implemented and used in the game.
"""

import os
import re
import sys
from pathlib import Path


def search_in_files(directory, entity_name, file_extensions, context_lines=5):
    """
    Search for an entity name in files with specified extensions in a directory.
    Returns a list of matches with context.
    """
    results = []

    # Pattern to match the entity name in code
    # For Java files
    java_patterns = [
        # Class name usage (e.g., new Skeleton(), Skeleton.class, instanceof Skeleton)
        r'\b' + re.escape(entity_name) + r'\b',
        # getEntityKind() usage for dynamic entity identification
        r'getEntityKind\(\s*\)\s*==\s*["\']' + re.escape(entity_name) + r'["\']',
        # Entity creation patterns
        r'new\s+' + re.escape(entity_name) + r'\s*\(',
        # Class references
        r'\b' + re.escape(entity_name) + r'\.class\b',
        # Instanceof checks
        r'instanceof\s+' + re.escape(entity_name),
    ]

    # For Lua files
    lua_patterns = [
        # Entity name as identifier
        r'\b' + re.escape(entity_name) + r'\b',
        # getEntityKind() usage in Lua
        r'getEntityKind\(\s*\)\s*==\s*["\']' + re.escape(entity_name) + r'["\']',
        # Entity creation in Lua
        r'create\(["\']' + re.escape(entity_name) + r'["\']',
        # Entity references in Lua
        r'\.?' + re.escape(entity_name) + r'\s*=',
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


def find_json_config_usage(directory, entity_name):
    """
    Find JSON configuration files that reference the entity name.
    Returns a list of matches with context.
    """
    results = []

    for root_dir, dirs, files in os.walk(directory):
        for file in files:
            if file.endswith('.json'):
                file_path = os.path.join(root_dir, file)

                try:
                    with open(file_path, 'r', encoding='utf-8') as f:
                        content = f.read()

                    # Look for the entity name in the JSON content
                    if entity_name.lower() in content.lower():  # Case-insensitive search
                        # Find lines containing the entity name
                        lines = content.split('\n')
                        for i, line in enumerate(lines):
                            if entity_name.lower() in line.lower():
                                # Get context lines
                                start = max(0, i - context_lines)
                                end = min(len(lines), i + context_lines + 1)
                                context = '\n'.join(lines[start:end]).strip()

                                results.append({
                                    'file': file_path,
                                    'line_number': i + 1,
                                    'match': entity_name,
                                    'context': context
                                })
                except UnicodeDecodeError:
                    # Skip files that can't be read as UTF-8
                    continue
                except Exception:
                    # Skip other problematic files
                    continue

    return results


def find_string_references(base_path, entity_name):
    """
    Find string resource references that might correspond to the entity.
    Returns potential string IDs that might relate to the entity.
    """
    string_ids = set()
    
    # Look for potential string ID patterns based on the entity name
    # Common patterns in Remixed Dungeon string IDs
    potential_patterns = [
        entity_name + "_Name",
        entity_name + "_Info",
        entity_name + "_Desc",
        entity_name + "_Gender",
        entity_name + "_Death",
        entity_name + "_Killed",
        entity_name + "_Message",
        entity_name + "_ACSpecial",
        entity_name + "_Tile",
        entity_name + "_TileDesc",
    ]
    
    # Also try variations with common prefixes
    common_prefixes = ["Mob_", "Item_", "Buff_", "Spell_", "NPC_"]
    for prefix in common_prefixes:
        potential_patterns.extend([
            prefix + entity_name + "_Name",
            prefix + entity_name + "_Info", 
            prefix + entity_name + "_Desc",
            prefix + entity_name + "_Gender",
        ])
    
    # Check if these potential string IDs exist in the English strings file
    english_file = os.path.join(base_path, "RemixedDungeon", "src", "main", "res", "values", "strings_all.xml")
    
    if os.path.exists(english_file):
        try:
            import xml.etree.ElementTree as ET
            tree = ET.parse(english_file)
            root = tree.getroot()
            
            for string_elem in root.findall('string'):
                name = string_elem.get('name')
                if any(name.startswith(pattern.replace("_Name", "").replace("_Info", "").replace("_Desc", "")
                                      .replace("_Gender", "").replace("_Death", "").replace("_Killed", "")
                                      .replace("_Message", "").replace("_ACSpecial", "").replace("_Tile", "")
                                      .replace("_TileDesc", ""))
                     for pattern in potential_patterns):
                    string_ids.add(name)
                    
        except Exception as e:
            print(f"Error parsing English strings file: {e}")

    return string_ids


def main():
    if len(sys.argv) < 2:
        print("Usage: python find_entity_usage.py <entity_name> [project_base_path]")
        print("Example: python find_entity_usage.py Skeleton /path/to/remixed-dungeon")
        sys.exit(1)

    entity_name = sys.argv[1]
    base_path = sys.argv[2] if len(sys.argv) > 2 else "/home/mike/StudioProjects/remixed-dungeon"

    print(f"Searching for entity: {entity_name}")
    print()

    # Search in Java files
    java_dir = os.path.join(base_path, "RemixedDungeon", "src", "main", "java")
    print("Java file usage:")
    java_results = search_in_files(java_dir, entity_name, ['.java'])

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

    # Check for the actual Java class file if it exists
    print(f"  Looking for Java class file: {entity_name}")
    class_files = find_java_class_files(java_dir, entity_name)
    if class_files:
        for class_file in class_files:
            print(f"    Found class {entity_name} in: {class_file}")
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
        print(f"  No Java class {entity_name} found")
    print()

    # Search in Lua files
    lua_dir = os.path.join(base_path, "RemixedDungeon", "src", "main", "assets", "scripts")
    print("Lua file usage:")
    lua_results = search_in_files(lua_dir, entity_name, ['.lua'])

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
    print()

    # Search in JSON configuration files
    assets_dir = os.path.join(base_path, "RemixedDungeon", "src", "main", "assets")
    print("JSON configuration usage:")
    json_results = find_json_config_usage(assets_dir, entity_name)

    if json_results:
        # Group results by file
        files_results = {}
        for result in json_results:
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
        print("  No usage found in JSON configuration files")
    print()

    # Find potential string resource references
    print("Potential string resource references:")
    string_ids = find_string_references(base_path, entity_name)
    if string_ids:
        for string_id in string_ids:
            print(f"  - {string_id}")
    else:
        print("  No potential string resource references found")
    print()

    print("Entity usage search completed.")


if __name__ == "__main__":
    main()