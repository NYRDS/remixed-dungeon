#!/usr/bin/env python3
"""
Lua Syntax Validator for Remixed Dungeon
This script validates all Lua files in the Remixed Dungeon project for syntax errors.
"""
import os
import subprocess
import sys
import json
from pathlib import Path

def validate_lua_file(filepath):
    """Validate a single Lua file for syntax errors."""
    try:
        # Use luac to check syntax
        result = subprocess.run(
            ["luac", "-p", str(filepath)], 
            capture_output=True, 
            text=True
        )
        return result.returncode == 0, result.stderr
    except FileNotFoundError:
        # If luac is not available, try using lua itself
        try:
            result = subprocess.run(
                ["lua", "-c", str(filepath)], 
                capture_output=True, 
                text=True
            )
            return result.returncode == 0, result.stderr
        except FileNotFoundError:
            # If lua is not available, try using Python to detect basic syntax issues
            # by importing the LuaParser module
            try:
                with open(filepath, 'r', encoding='utf-8') as f:
                    content = f.read()
                # Basic check: ensure parentheses and braces are balanced
                stack = []
                for i, char in enumerate(content):
                    if char in '([{':
                        stack.append((char, i))
                    elif char in ')]}':
                        if not stack:
                            return False, f"Unmatched closing bracket '{char}' at position {i}"
                        last_open, pos = stack.pop()
                        if (char == ')' and last_open != '(') or \
                           (char == ']' and last_open != '[') or \
                           (char == '}' and last_open != '{'):
                            return False, f"Mismatched brackets at position {i}"
                
                if stack:
                    last_open, pos = stack[0]
                    return False, f"Unclosed bracket '{last_open}' at position {pos}"
                
                return True, ""
            except Exception as e:
                return False, str(e)

def find_lua_files(base_path):
    """Find all Lua files in the project."""
    lua_files = []
    for root, dirs, files in os.walk(base_path):
        # Skip build directories
        if 'build' in root or '.gradle' in root:
            continue
        for file in files:
            if file.endswith('.lua'):
                lua_files.append(os.path.join(root, file))
    return lua_files

def main():
    project_path = sys.argv[1] if len(sys.argv) > 1 else '/home/mike/StudioProjects/remixed-dungeon'
    
    print(f"Validating Lua files in {project_path}")
    
    lua_files = find_lua_files(project_path)
    print(f"Found {len(lua_files)} Lua files to validate...")
    
    errors = []
    valid_count = 0
    
    for lua_file in lua_files:
        is_valid, error_msg = validate_lua_file(lua_file)
        if is_valid:
            valid_count += 1
            print(f"✓ {lua_file}")
        else:
            errors.append((lua_file, error_msg))
            print(f"✗ {lua_file} - {error_msg}")
    
    print(f"\nValidation complete: {valid_count} valid, {len(errors)} invalid")
    
    if errors:
        print("\nErrors found:")
        for filepath, error in errors:
            print(f"  {filepath}: {error}")
        return 1
    else:
        print("\nAll Lua files are syntactically valid!")
        return 0

if __name__ == "__main__":
    sys.exit(main())