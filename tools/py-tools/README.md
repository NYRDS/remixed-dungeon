# Python Tools for Remixed Dungeon

This directory contains Python scripts that are useful for maintaining and developing Remixed Dungeon, particularly for wiki content generation and asset management.

## Scripts by Category

### Wiki Content Generation
- `generate_spell_wiki.py` - Generates or updates spell documentation from source code
- `generate_spell_images.py` - Creates preview images for spells
- `generate_hero_previews.py` - Generates preview content for hero classes and subclasses
- `find_red_links.py` - Finds broken links in wiki content
- `update_links.py` - Updates links in wiki content
- `update_links_specific.py` - Updates specific links in wiki content

### Sprite and Asset Extraction
- `extract_item_sprites.py` - Extracts item sprites from the main sprite sheet
- `extract_all_item_sprites.py` - Extracts items from multiple specialized sheets
- `extract_custom_item_sprites.py` - Extracts items with special configurations
- `extract_mob_sprites.py` - Extracts mob sprites with 8x scaling
- `check_unused_images.py` - Checks for unused image assets

### File Management and Validation
- `categorize_unused.py` - Categorizes unused files
- `find_unused_files.py` - Finds unused files in the project
- `remove_duplicate_files.py` - Removes duplicate files
- `rename_capitalized_files.py` - Renames capitalized files to lowercase (following wiki standards)
- `test_json_configs.py` - Validates JSON configuration files
- `test_lua_syntax.py` - Validates Lua script syntax

## Usage Notes

Most of these scripts were originally located in the root directory and have been organized here for better project structure. They are referenced in the wiki documentation and serve specific purposes in maintaining the Remixed Dungeon project.