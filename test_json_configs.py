#!/usr/bin/env python3
"""
JSON Validator for Remixed Dungeon
This script validates all JSON files in the Remixed Dungeon project for syntax errors
and specific validation for various types of configuration files.
"""
import json
import os
import sys
from pathlib import Path
from typing import Dict, List, Tuple

def validate_json_file(filepath: str) -> Tuple[bool, str]:
    """Validate a single JSON file for syntax errors."""
    try:
        with open(filepath, 'r', encoding='utf-8') as f:
            content = f.read()
            # Handle potential BOM in the file
            if content.startswith('\ufeff'):
                content = content[1:]
            data = json.loads(content)
        return True, ""
    except json.JSONDecodeError as e:
        return False, str(e)
    except Exception as e:
        return False, str(e)

def validate_level_config(data: dict, filepath: str) -> List[str]:
    """Validate level configuration files for required fields and proper structure."""
    errors = []
    
    # Check required top-level fields
    required_fields = ['width', 'height', 'map']
    for field in required_fields:
        if field not in data:
            errors.append(f"Missing required field: {field}")
    
    if 'map' in data:
        if not isinstance(data['map'], list):
            errors.append("Map field must be an array")
        else:
            # Verify that the map dimensions match the width and height
            expected_height = data.get('height', 0)
            if len(data['map']) != expected_height:
                errors.append(f"Map height ({len(data['map'])}) doesn't match height field ({expected_height})")
            
            # Verify each row has the correct width
            expected_width = data.get('width', 0)
            for i, row in enumerate(data['map']):
                if isinstance(row, list):
                    if len(row) != expected_width:
                        errors.append(f"Row {i} width ({len(row)}) doesn't match width field ({expected_width})")
                elif isinstance(row, str):
                    if len(row) != expected_width:
                        errors.append(f"Row {i} width ({len(row)}) doesn't match width field ({expected_width})")
    
    return errors

def validate_mob_config(data: dict, filepath: str) -> List[str]:
    """Validate mob configuration files for required fields and proper structure."""
    errors = []
    
    # Check that required fields are present
    required_fields = ['name:en', 'HP', 'exp']  # Basic required fields
    for field in required_fields:
        if field not in data:
            errors.append(f"Missing required field: {field}")
    
    # Validate numeric fields
    numeric_fields = ['HP', 'exp', 'dmgMin', 'dmgMax', 'defenseSkill', 'attackSkill']
    for field in numeric_fields:
        if field in data and not isinstance(data[field], (int, float)):
            errors.append(f"Field {field} should be numeric, got {type(data[field])}")
    
    return errors

def validate_item_config(data: dict, filepath: str) -> List[str]:
    """Validate item configuration files for required fields and proper structure."""
    errors = []
    
    # Check that required fields are present
    # Items don't have a standard required set like mobs, but we can check common fields
    if 'name:en' not in data and 'class' not in data:
        errors.append("Item should have either 'name:en' or 'class' field")
    
    return errors

def validate_dungeon_config(data: dict, filepath: str) -> List[str]:
    """Validate dungeon configuration files for required fields and proper structure."""
    errors = []
    
    # Check for required structures
    if 'Levels' not in data:
        errors.append("Missing required 'Levels' field")
    
    if 'Graph' not in data:
        errors.append("Missing required 'Graph' field")
    
    # Validate Levels structure
    if 'Levels' in data and not isinstance(data['Levels'], dict):
        errors.append("'Levels' field must be an object")
    
    # Validate Graph structure
    if 'Graph' in data and not isinstance(data['Graph'], dict):
        errors.append("'Graph' field must be an object")
    
    return errors

def get_config_type(filepath: str) -> str:
    """Determine the type of configuration file based on its path or name."""
    path = Path(filepath)
    
    # Check by directory
    parent_dirs = [str(p).lower() for p in path.parents]
    
    if 'levelsdesc' in parent_dirs:
        return 'level'
    elif 'mobsdesc' in parent_dirs:
        return 'mob'
    elif 'items' in parent_dirs:
        return 'item'
    elif 'dungeon' in path.name.lower():
        return 'dungeon'
    
    # Check by filename patterns
    if path.name.lower() in ['dungeon.json', 'predesigned.json']:
        return 'dungeon'
    
    # Default to generic if not determinable
    return 'generic'

def validate_specific_config(data: dict, filepath: str) -> List[str]:
    """Validate specific types of configuration files."""
    config_type = get_config_type(filepath)
    
    if config_type == 'level':
        return validate_level_config(data, filepath)
    elif config_type == 'mob':
        return validate_mob_config(data, filepath)
    elif config_type == 'item':
        return validate_item_config(data, filepath)
    elif config_type == 'dungeon':
        return validate_dungeon_config(data, filepath)
    else:
        # For other types, do basic validation only
        return []

def find_json_files(base_path: str) -> List[str]:
    """Find all JSON files in the project, excluding build directories."""
    json_files = []
    for root, dirs, files in os.walk(base_path):
        # Skip build directories
        if 'build' in root or '.gradle' in root:
            continue
        for file in files:
            if file.endswith('.json'):
                # Skip the google-services.json which is a different format
                if 'google-services.json' not in root:
                    json_files.append(os.path.join(root, file))
    return json_files

def main():
    project_path = sys.argv[1] if len(sys.argv) > 1 else '/home/mike/StudioProjects/remixed-dungeon'
    
    print(f"Validating JSON files in {project_path}")
    
    json_files = find_json_files(project_path)
    print(f"Found {len(json_files)} JSON files to validate...")
    
    errors = []
    valid_count = 0
    
    for json_file in json_files:
        is_valid, error_msg = validate_json_file(json_file)
        if is_valid:
            # If basic syntax is valid, run specific validation
            try:
                with open(json_file, 'r', encoding='utf-8') as f:
                    content = f.read()
                    if content.startswith('\ufeff'):
                        content = content[1:]
                    data = json.loads(content)
                
                specific_errors = validate_specific_config(data, json_file)
                
                if not specific_errors:
                    valid_count += 1
                    print(f"✓ {json_file}")
                else:
                    errors.append((json_file, f"Specific validation failed: {'; '.join(specific_errors)}"))
                    print(f"✗ {json_file} - Specific validation failed: {'; '.join(specific_errors)}")
            except Exception as e:
                errors.append((json_file, f"Error reading file: {str(e)}"))
                print(f"✗ {json_file} - Error reading file: {str(e)}")
        else:
            errors.append((json_file, error_msg))
            print(f"✗ {json_file} - {error_msg}")
    
    print(f"\nValidation complete: {valid_count} valid, {len(errors)} invalid")
    
    if errors:
        print("\nErrors found:")
        for filepath, error in errors:
            print(f"  {filepath}: {error}")
        return 1
    else:
        print("\nAll JSON files are syntactically valid!")
        return 0

if __name__ == "__main__":
    sys.exit(main())