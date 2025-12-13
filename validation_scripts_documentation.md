# Lua and JSON Validation Scripts for Remixed Dungeon

This document explains how to use the validation scripts for Lua and JSON files in the Remixed Dungeon project.

## Overview

Two validation scripts have been created to help ensure code quality in the Remixed Dungeon codebase:

1. `test_lua_syntax.py` - Validates Lua syntax in all `.lua` files
2. `test_json_configs.py` - Validates JSON syntax and structure in all `.json` files

## Lua Syntax Validation

The Lua syntax validation script (`test_lua_syntax.py`) checks all `.lua` files in the project for syntactic correctness.

### How it works:
- Finds all `.lua` files in the project, excluding build directories
- Validates each file using the `luac` compiler if available
- Falls back to basic syntax checking if `luac` is not installed
- Reports which files passed and which failed validation

### Usage:
```bash
python3 test_lua_syntax.py [path_to_project_root]
# Example:
python3 test_lua_syntax.py /home/user/Remixed-Dungeon/
```

If no path is provided, it defaults to `/home/mike/StudioProjects/remixed-dungeon`

## JSON Configuration Validation

The JSON validation script (`test_json_configs.py`) checks all `.json` files for both syntactic correctness and structural validity based on their configuration type.

### How it works:
- Finds all `.json` files in the project, excluding build directories
- Validates basic JSON syntax using Python's `json` module
- Performs additional structural validation based on file type:
  - Level configs: Check for required fields like `width`, `height`, `map`
  - Mob configs: Check for required fields like `name:en`, `HP`, `exp`
  - Dungeon configs: Check for required structures like `Levels` and `Graph`
  - Item configs: Basic validation for presence of name or class

### Usage:
```bash
python3 test_json_configs.py [path_to_project_root]
# Example:
python3 test_json_configs.py /home/user/Remixed-Dungeon/
```

If no path is provided, it defaults to `/home/mike/StudioProjects/remixed-dungeon`

## Expected Output

Both scripts will:
1. List each file as it's validated with a checkmark (✓) for valid or an X (✗) for invalid
2. Report the total number of valid and invalid files at the end
3. List detailed error messages for invalid files

## Integration with Development Workflow

These scripts can be integrated into your development workflow:

### Pre-commit Hook
You can set up a Git pre-commit hook to automatically run these validators:

```bash
# In .git/hooks/pre-commit
#!/bin/sh
python3 test_lua_syntax.py
if [ $? -ne 0 ]; then
    echo "Lua validation failed. Commit aborted."
    exit 1
fi

python3 test_json_configs.py
if [ $? -ne 0 ]; then
    echo "JSON validation failed. Commit aborted."
    exit 1
fi
```

### Continuous Integration
Add the scripts to your CI pipeline to validate all Lua and JSON files during automated testing.

## Known Issues

The JSON validation script identified some localization files in the Remixed Dungeon project that have syntax errors. These appear to be malformed JSON files in the strings localization files. These should be fixed in the main codebase.

## Maintenance

To update the validation logic:
- For Lua validation: Modify the `validate_lua_file` function
- For JSON validation: Update the type-specific validators (e.g., `validate_level_config`, `validate_mob_config`, etc.)
- For file discovery: Update the `find_lua_files` or `find_json_files` functions

These scripts provide a solid foundation for ensuring code quality in the Remixed Dungeon modding ecosystem.