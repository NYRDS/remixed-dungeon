# Wiki Redirect Fixer

This script detects and fixes redirect pages in the Remixed Dungeon wiki by:

1. Identifying pages that should redirect to other pages based on naming conventions
2. Automatically updating all links in wiki pages to point to the correct destination
3. Deleting the redirect pages (for content-based redirects)

## Naming Convention Redirects

The script identifies the following naming convention patterns:

- Pages without suffix redirecting to pages with `_class` suffix (e.g., `warrior` → `warrior_class`)
- Pages without suffix redirecting to pages with `_mob` suffix (e.g., `shaman` → `shaman_mob`)
- Pages without suffix redirecting to pages with `_spell` suffix (e.g., `heal` → `heal_spell`)
- Pages without suffix redirecting to pages with `_item` suffix (e.g., `ankh` → `ankh_item`)
- Pages without suffix redirecting to pages with `_npc` suffix (e.g., `shopkeeper` → `shopkeeper_npc`)
- Pages without suffix redirecting to pages with `_subclass` suffix (e.g., `shaman` → `shaman_subclass`)

## Usage

```bash
# Dry run to see what would be changed
python3 tools/py-tools/fix_wiki_redirects.py --dry-run

# Actually make the changes
python3 tools/py-tools/fix_wiki_redirects.py

# Specify a different wiki directory
python3 tools/py-tools/fix_wiki_redirects.py --wiki-dir /path/to/wiki
```

## Features

- Updates links in the format `[[namespace:old_name|Display Text]]` to `[[namespace:new_name|Display Text]]`
- Updates links in the format `[[old_name|Display Text]]` to `[[new_name|Display Text]]`
- Preserves display text in links
- Prevents duplicate processing of the same redirect
- Provides detailed output of changes made
- Supports dry-run mode to preview changes before making them