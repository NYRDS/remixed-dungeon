# Wiki and Translation Maintenance Script

This script performs automated maintenance tasks for both wiki content and translations in the Remixed Dungeon project. It follows the iteration semantic but randomly chooses between wiki tasks and translation tasks.

## Features

- Runs continuously with 1-hour intervals between tasks
- Randomly selects between wiki maintenance and translation tasks
- Automatically commits and pushes changes when modifications are detected
- Handles both wiki page updates and string translation additions

## Usage

Run the script with:

```bash
./wiki_translation_maintenance.sh
```

The script will run indefinitely, alternating between wiki and translation tasks. Press `Ctrl+C` to stop the script.

## Task Types

### Wiki Tasks
- Selects random wiki pages for analysis
- Checks for compliance with wiki standards
- Identifies and fixes issues like missing images, invalid headers, incorrect links, and improper formatting
- Runs the dokuwiki linter to validate pages
- Ensures proper image references and link consistency

### Translation Tasks
- Identifies random missing strings in various languages
- Finds context for strings using code analysis
- Translates strings based on English references and code context
- Adds translations to appropriate strings_all.xml files
- Verifies consistency with existing translations
- Ensures consistency across all languages

## Commit Messages

The script automatically generates appropriate commit messages based on the type of task performed:
- Wiki tasks: "Auto-wiki: Update wiki pages based on maintenance iteration"
- Translation tasks: "Auto-translation: Add missing string translations"