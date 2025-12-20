# Python Tools for Remixed Dungeon

This directory contains Python scripts that are useful for maintaining and developing Remixed Dungeon, particularly for wiki content generation and asset management.

## Scripts by Category

### Wiki Content Generation
- `generate_spell_wiki.py` - Generates or updates spell documentation from source code
- `generate_spell_images.py` - Creates preview images for spells
- `generate_hero_previews.py` - Generates preview content for hero classes and subclasses
- `find_red_links.py` - Finds broken links, generates backlinks reports, and identifies missing images in wiki content
- `update_links.py` - Updates links in wiki content
- `update_links_specific.py` - Updates specific links in wiki content

### Sprite and Asset Extraction
- `extract_item_sprites.py` - Extracts item sprites from the main sprite sheet
- `extract_all_item_sprites.py` - Extracts items from multiple specialized sheets
- `extract_custom_item_sprites.py` - Extracts items with special configurations
- `extract_mob_sprites.py` - Extracts mob sprites with 8x scaling
- `scale_sprites_for_wiki.py` - Enhances sprites with scaling, background, and frame for better wiki visualization (processed sprites are available in the wiki-data submodule)
- `rename_images_for_wiki.py` - Renames processed wiki images to match the page naming convention (snake_case with entity type suffixes)
- `check_unused_images.py` - Checks for unused image assets
- `check_page_image_naming.py` - Checks if properly named wiki pages have properly named images

### File Management and Validation
- `categorize_unused.py` - Categorizes unused files
- `find_unused_files.py` - Finds unused files in the project
- `remove_duplicate_files.py` - Removes duplicate files
- `rename_capitalized_files.py` - Renames capitalized files to lowercase (following wiki standards)
- `test_json_configs.py` - Validates JSON configuration files
- `test_lua_syntax.py` - Validates Lua script syntax

## Usage Notes

Most of these scripts were originally located in the root directory and have been organized here for better project structure. They are referenced in the wiki documentation and serve specific purposes in maintaining the Remixed Dungeon project.

### Image Naming Convention Update
The `scale_sprites_for_wiki.py` script has been updated to generate images with names that match the page naming convention. This ensures that image names follow the snake_case format with entity type suffixes (e.g., tengu_mob.png, ankh_item.png, heal_spell.png) to match corresponding wiki page names (e.g., tengu_mob.txt, ankh_item.txt, heal_spell.txt).

The `rename_images_for_wiki.py` script can be used to rename existing images to match the new naming convention.