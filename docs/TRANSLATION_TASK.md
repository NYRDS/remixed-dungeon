# Localization (l10n) Task Guide for Remixed Dungeon

This guide explains how to identify and fill in missing localization strings in Remixed Dungeon, using English, Russian, and code as references.

## Overview

Remixed Dungeon uses Android's string resource system for localization. All strings are stored in XML files in the `values` directories under `RemixedDungeon/src/main/res/`. The English strings are in `values/strings_all.xml`, and localized versions are in directories like `values-ru/strings_all.xml`, `values-es/strings_all.xml`, etc.

## Localization Directory Structure

Localization files are organized in the Android resource directory structure:
- `RemixedDungeon/src/main/res/values/strings_all.xml` - English (default) strings
- `RemixedDungeon/src/main/res/values-ru/strings_all.xml` - Russian strings
- `RemixedDungeon/src/main/res/values-es/strings_all.xml` - Spanish strings
- `RemixedDungeon/src/main/res/values-[lang-code]/strings_all.xml` - Other language strings

Each language-specific directory follows the Android resource naming convention where `lang-code` is the ISO language code (e.g., `ru`, `es`, `fr`) or Android-specific format (e.g., `pt-rBR` for Brazilian Portuguese, `zh-rTW` for Traditional Chinese, `zh-rCN` for Simplified Chinese). The 'r' prefix is Android's convention for country/region-specific variants.

## Available Localization Tools

### 1. check_missing_strings.py
- **Purpose**: Identifies missing strings in localizations by comparing them against the English strings_all.xml file
- **Usage**: `python3 tools/check_missing_strings.py <project_base_path>`
- **Output**: Lists missing strings per language and provides a summary

### 2. find_string_usage.py
- **Purpose**: Finds English and Russian values for a given string ID and shows its usage in Java and Lua code with context
- **Usage**: `python3 tools/find_string_usage.py <string_id> [project_base_path]`
- **Output**: Shows string values in English and Russian, code usage, and related class content

### 3. select_random_missing_string.py
- **Purpose**: Randomly selects a string ID that is present in English but missing in one of the other languages
- **Usage**: `python3 tools/select_random_missing_string.py <project_base_path> [target_language_code]`
- **Output**: Shows which language is missing the string and provides the English value as reference

### 4. insert_translated_string.py
- **Purpose**: Inserts translated strings into localization XML files with validation
- **Usage**: `python3 tools/insert_translated_string.py <lang_code> <string_id> <string_value> [project_base_path] [--no-sort]`
- **Features**:
  - Enforces rules for `_Gender` suffix (only lowercase values: feminine, masculine, neuter)
  - Creates new localization files if they don't exist
  - Updates existing strings if they already exist in the file
  - Sorts strings alphabetically by name (unless `--no-sort` is specified)
  - Provides proper XML formatting
  - Supports both standard and Android resource language code formats (e.g., 'pt-BR', 'pt-rBR', 'zh-rTW')

### 5. check_random_string_translation.py
- **Purpose**: Selects a random string ID and checks its translations across all languages
- **Usage**: `python3 tools/check_random_string_translation.py <project_base_path>`
- **Output**: Shows the translation of a randomly selected string across all available languages

### 6. validate_translations.py
- **Purpose**: Validates that translated strings are properly formatted and saved in XML files
- **Usage**: `python3 tools/validate_translations.py <project_base_path>`
- **Features**:
  - Checks for malformed XML
  - Validates gender values (must be feminine, masculine, or neuter)
  - Checks for unescaped special characters
  - Provides validation report for all localization files

## Identifying Missing Strings

### Method 1: Using the Missing Strings Script

I've created a script to automatically identify missing strings in localizations:

**Script location:** `tools/check_missing_strings.py`

**Usage:**
```bash
python3 tools/check_missing_strings.py /path/to/remixed-dungeon
```

This script will:
- Extract all string names from the English `strings_all.xml` file
- Compare with each localized `strings_all.xml` file
- Report which strings are missing in each localization
- Provide a summary of missing strings per language

### Method 2: Manual Comparison

You can also manually compare the English and localized string files:
- English: `RemixedDungeon/src/main/res/values/strings_all.xml`
- Russian: `RemixedDungeon/src/main/res/values-ru/strings_all.xml`
- Other languages: `RemixedDungeon/src/main/res/values-[lang-code]/strings_all.xml`

### Method 3: Random Missing String Selector

I've created an additional script to randomly select a string ID that is present in English but missing in one of the other languages:

**Script location:** `tools/select_random_missing_string.py`

**Usage:**
```bash
python3 tools/select_random_missing_string.py /path/to/remixed-dungeon        # Random from any language
python3 tools/select_random_missing_string.py /path/to/remixed-dungeon ru    # Random from Russian only
```

This script will:
- Identify all strings present in English but missing in other languages
- Randomly select one missing string
- Show which language is missing the string and provide the English value as reference

### Method 4: Check Random String Translation Across Languages

I've created an additional script to select a random string ID and check its translations across all languages:

**Script location:** `tools/check_random_string_translation.py`

**Usage:**
```bash
python3 tools/check_random_string_translation.py /path/to/remixed-dungeon
```

This script will:
- Select a random string ID from the available strings
- Show the translation of that string across all available languages
- Highlight any missing translations with "[MISSING]" markers
- Help verify consistency of translations across languages

## Identifying Missing Strings

### Method 1: Using the Missing Strings Script

I've created a script to automatically identify missing strings in localizations:

**Script location:** `tools/check_missing_strings.py`

**Usage:**
```bash
python3 tools/check_missing_strings.py /path/to/remixed-dungeon
```

This script will:
- Extract all string names from the English `strings_all.xml` file
- Compare with each localized `strings_all.xml` file
- Report which strings are missing in each localization
- Provide a summary of missing strings per language

### Method 2: Manual Comparison

You can also manually compare the English and localized string files:
- English: `RemixedDungeon/src/main/res/values/strings_all.xml`
- Russian: `RemixedDungeon/src/main/res/values-ru/strings_all.xml`
- Other languages: `RemixedDungeon/src/main/res/values-[lang-code]/strings_all.xml`

## Filling in Missing Strings

### Step 1: Find the Missing String ID

From the script output or manual comparison, identify the string ID that's missing in the target language.

### Step 2: Understand the Context

Use the string usage script to find where and how the string is used in code:

**Script location:** `tools/find_string_usage.py`

**Usage:**
```bash
python3 tools/find_string_usage.py <string_id> /path/to/remixed-dungeon
```

This script will:
- Show the English and Russian values for the string ID
- Find where the string is used in Java and Lua code with context
- If the string follows the format "Entity_Something", search for and include the content of the corresponding Java class

### Step 3: Translate Based on Context

When translating, consider:

1. **English value**: Use as the primary reference for meaning
2. **Russian value**: Use as a secondary reference for context and translation approach
3. **Code context**: Understand how the string is used to ensure appropriate translation
4. **Game context**: Consider the game mechanics and terminology

### Step 4: Add the Translation

Use the insert script to add the new string to the appropriate `strings_all.xml` file in the target language directory:

**Script location:** `tools/insert_translated_string.py`

**Usage:**
```bash
python3 tools/insert_translated_string.py <lang_code> <string_id> <translation> [project_path]
```

Example:
```bash
python3 tools/insert_translated_string.py de WndJournal_Levels "Ebenen"
```

This will add the string to the appropriate localization file with proper formatting.

## Best Practices for Translation

### Consistency
- Maintain consistency with existing translations
- Use the same terminology throughout the game
- Follow the style of existing translations in the target language

### Context Awareness
- Consider where the string appears in the game (UI, dialog, description)
- Ensure the translation fits the space constraints
- Maintain the tone appropriate for a roguelike game

### Special Characters
- Be careful with special characters that might have meaning in XML
- Use appropriate escape sequences if needed
- Preserve formatting placeholders like `%1$s`, `%d`, etc.
- Avoid wrapping entire string content with quotes (e.g., `"This is text"` should be `This is text`)
- For quotes within strings, use `\"` (preferred) or `&quot;` to escape them

### Gender Values
- For strings with IDs ending in `_Gender`, only specific lowercase values are allowed:
  - `feminine`
  - `masculine`
  - `neuter`
- When using the `insert_translated_string.py` tool, any other values (including uppercase variants) will be rejected
- This ensures consistency across all language translations for grammatical gender

### Cultural Adaptation
- Adapt cultural references appropriately for the target language
- Consider if direct translation makes sense in the target culture

## Example Workflow

1. **Run the missing strings script:**
   ```bash
   python3 tools/check_missing_strings.py /path/to/remixed-dungeon
   ```
   This shows that `WndJournal_Levels` is missing in the German translation.

2. **Check the string usage:**
   ```bash
   python3 tools/find_string_usage.py WndJournal_Levels /path/to/remixed-dungeon
   ```
   This shows:
   - English: "Levels"
   - Russian: "Уровни"
   - Used in `WndJournal.java` as a tab title
   - Part of the Journal window UI

3. **Translate appropriately:**
   ```bash
   python3 tools/insert_translated_string.py de WndJournal_Levels "Ebenen"
   ```

## Additional Tools

### Random Missing String Selector

I've created an additional script to randomly select a string ID that is present in English but missing in one of the other languages:

**Script location:** `tools/select_random_missing_string.py`

**Usage:**
```bash
python3 tools/select_random_missing_string.py /path/to/remixed-dungeon        # Random from any language
python3 tools/select_random_missing_string.py /path/to/remixed-dungeon ru    # Random from Russian only
```

This script will:
- Identify all strings present in English but missing in other languages
- Randomly select one missing string
- Show which language is missing the string and provide the English value as reference

### Check Random String Translation Across Languages

I've created an additional script to select a random string ID and check its translations across all languages:

**Script location:** `tools/check_random_string_translation.py`

**Usage:**
```bash
python3 tools/check_random_string_translation.py /path/to/remixed-dungeon
```

This script will:
- Select a random string ID from the available strings
- Show the translation of that string across all available languages
- Highlight any missing translations with "[MISSING]" markers
- Help verify consistency of translations across languages

### Translation Iteration Scripts

#### translation_iteration.sh
- Runs automated translation tasks in a loop with 1-hour intervals
- Performs git operations to commit and push changes

#### translation_iteration_with_check.sh
- Runs various translation-related tools in sequence:
  1. Checks for missing strings
  2. Selects a random missing string
  3. Checks random string translation across all languages

## Reference Materials

### English Strings
- Location: `RemixedDungeon/src/main/res/values/strings_all.xml`
- Contains all original English strings

### Russian Strings
- Location: `RemixedDungeon/src/main/res/values-ru/strings_all.xml`
- Contains Russian translations that can serve as reference

### Code Context
- Java files: `RemixedDungeon/src/main/java/`
- Lua files: `RemixedDungeon/src/main/assets/scripts/`
- Provides context for how strings are used in-game

## Quality Assurance

After adding translations:

1. **Verify completeness:** Ensure all placeholders and formatting are preserved
2. **Check context:** Make sure the translation makes sense in the game UI
3. **Test in game:** If possible, test the translation in the actual game
4. **Consistency check:** Ensure the translation is consistent with other similar strings
5. **Run validation:** Use the validation script to check for common issues: `python3 tools/validate_translations.py /path/to/remixed-dungeon`

## Common Issues and Solutions

### Language Code Format Issues
- **Problem**: Android uses specific resource naming conventions like `zh-rTW` (with 'r' prefix) rather than standard formats like `zh-TW`
- **Solution**: The `insert_translated_string.py` tool now supports both formats: standard (`pt-BR`) and Android-specific (`pt-rBR`, `zh-rTW`)

### Gender Value Issues
- **Problem**: Gender values must be in English and lowercase: `feminine`, `masculine`, `neuter`
- **Solution**: The validation script will catch non-compliant gender values and report them
- **Example**: `Spear_Gender` with value "中性" (Chinese for "neutral") should be "neuter"
- **Example**: `MercenaryNPC_Gender` with value "maskulin" (Indonesian for "masculine") should be "masculine"

### Special Character Issues
- **Problem**: Unescaped special characters like ampersands (&) can break XML parsing
- **Solution**: Use proper XML entities (`&amp;` instead of `&`, `&quot;` instead of `"`, etc.)
- **Note**: Apostrophes (') and quotes (") in non-English languages need to be properly escaped in XML using `\'` or `&apos;` and `&quot;` respectively

### Multiple Placeholders Issues
- **Problem**: Multiple `%s` placeholders without positional identifiers can cause Android build failures
- **Solution**: Use positional identifiers (`%1$s`, `%2$s`, etc.) for multiple placeholders
- **Example**: Instead of `"This %s is %s"`, use `"This %1$s is %2$s"`
- **Important**: When translating strings with multiple placeholders, ensure the order matches the code usage. For example, if the code calls `Utils.format(R.string.MobAi_status, me.getName(), getTag())`, then `%1$s` should correspond to the mob name and `%2$s` to the status tag.

### Apostrophe Escaping Issues
- **Problem**: Apostrophes in contractions like `po'` (meaning "of" in some languages) need to be properly escaped
- **Solution**: Use `\'` to escape apostrophes that are not part of XML entities like `&apos;`
- **Example**: `Starò con te per un po'.` should be `Starò con te per un po\'.` (the apostrophe before the period needs escaping)
- **Note**: The validation and auto-fix tools now properly detect and fix this pattern

### XML Formatting Issues
- **Problem**: Improperly formatted XML strings can cause build failures
- **Common Issues**:
  - Unescaped apostrophes in any language (e.g., "l'objet" should be "l\'objet" or "l&apos;objet")
  - Unescaped quotes in any language
  - Malformed XML tags
  - Invalid Unicode escape sequences
- **Solution**: Always validate your XML after adding translations
- **Best Practice**: Use the validation script regularly: `python3 tools/validate_translations.py /path/to/remixed-dungeon`

### Android-Specific String Resource Requirements
- **Problem**: Android has specific requirements for string resources that differ from standard XML
- **Solutions**:
  - Escape `@` symbols as `\@` when they appear in strings
  - Escape `?` symbols as `\?` when they appear at the beginning of strings
  - Properly format Unicode escapes as `\uXXXX` (4-digit hex codes)
  - Ensure all HTML-like tags within strings are properly closed
  - Handle whitespace correctly - if a string starts or ends with spaces, consider wrapping in quotes

### Android Build Issues
- **Problem**: Android builds can fail due to various localization issues:
  - Multiple substitutions in non-positional format (e.g., multiple `%s` without positional identifiers) - **THIS WILL CAUSE BUILD FAILURE**
  - Invalid unicode escape sequences (appearing as `{str}`) - **THIS WILL CAUSE BUILD FAILURE**
  - Malformed XML strings - **THIS WILL CAUSE BUILD FAILURE**
  - Duplicate string entries - **THIS WILL CAUSE BUILD FAILURE**
  - Unescaped special characters (apostrophes, quotes, ampersands) - **THIS WILL CAUSE BUILD FAILURE**
  - Improperly formatted Unicode escapes - **THIS WILL CAUSE BUILD FAILURE**
  - Unclosed HTML tags within strings - **THIS CAN CAUSE RUNTIME ERRORS**
- **Solution**:
  - Always use positional identifiers for multiple placeholders: `%1$s`, `%2$s`, etc.
  - Ensure all special characters are properly escaped in XML format
  - Run the validation script before committing changes
  - Test the Android build after adding new translations
  - Use the insertion tools (`insert_translated_string.py`) which help prevent these issues

### Wrapped Quotes Issues
- **Problem**: Sometimes strings get wrapped with quotes in the XML (e.g., `<string name="example">"This text is wrapped"</string>`) - **THIS WILL CAUSE BUILD ISSUES**
- **Solution**:
  - Remove the outer quotes from the string content: `<string name="example">This text is wrapped</string>`
  - If you need quotes within the string content, use `\"` (preferred) or `&quot;` to escape them
  - The validation script will detect and auto-fix this issue when using `--auto-fix` option

### Validation Checklist
Before submitting translations, ensure:
1. All apostrophes are properly escaped in any language using `\'` or `&apos;`
2. All quotes are properly escaped using `\"` (preferred) or `&quot;`
3. Multiple placeholders use positional identifiers (%1$s, %2$s, etc.)
4. Special characters are converted to proper XML entities
5. The XML file is well-formed and can be parsed without errors
6. Gender values are in English and lowercase (feminine, masculine, neuter)
7. The translated string maintains the same meaning and context as the original
8. Positional identifiers are used for all placeholders when there are multiple in a string
9. No invalid unicode escape sequences are present
10. All `@` symbols are escaped as `\@`
11. All `?` symbols at the beginning of strings are escaped as `\?`
12. Unicode escapes are properly formatted as `\uXXXX`
13. All HTML-like tags within strings are properly closed
14. Strings with leading/trailing spaces are handled correctly
15. Contractions with apostrophes (like `po'`) are properly escaped as `po\'`
16. Avoid wrapping entire string content with quotes (e.g., `"This is text"` should be `This is text`)
17. The auto-fix script has been run to address trivial escape issues: `python3 tools/validate_translations.py --auto-fix`

## Auto-Fix Functionality

### Overview
The project includes an auto-fix functionality that automatically resolves common escape issues in localization files:

**Script location:** `tools/validate_translations.py`

**Usage:**
```bash
python3 tools/validate_translations.py /path/to/remixed-dungeon --auto-fix
```

### Issues Addressed by Auto-Fix
The auto-fix functionality addresses the following common issues:
- Unescaped apostrophes (e.g., `po'` becomes `po\'`)
- Unescaped quotes (e.g., `"` becomes `\"`) - now consistently uses `\"` notation
- Unescaped ampersands (e.g., `&` becomes `&amp;`)
- Unescaped @ symbols (e.g., `@` becomes `\@`)
- Unescaped ? symbols at the beginning of strings (e.g., `?` becomes `\?`)
- Invalid unicode escape sequences (e.g., `{str}` gets removed)
- Duplicate string entries (removes duplicates, keeps first occurrence)
- Strings with quotes wrapping entire content (e.g., `"This text"` becomes `This text`)

### Benefits
- Significantly reduces manual work required to fix common issues
- Ensures consistent formatting across all localization files
- Helps maintain Android buildability by preventing common XML parsing errors
- Automatically handles the `po'` apostrophe case and other contractions
- Automatically detects and fixes strings with quotes wrapping entire content
- Consistently applies `\"` notation for quote escaping throughout all files

### Workflow Integration
1. Run the auto-fix script before committing changes: `python3 tools/validate_translations.py --auto-fix`
2. Review the changes made by the auto-fix script
3. Run the validation script again to ensure all issues are resolved: `python3 tools/validate_translations.py`
4. Test the Android build to confirm buildability

## Duplicate String Detection and Removal

### Problem
Localization files can sometimes contain duplicate string entries, which cause Android build failures with errors like:
```
ERROR: .../strings_all.xml: Resource and asset merger: Found item String/[string_name] more than one time
```

### Detection Methods

#### Method 1: Using grep to find duplicate entries
```bash
# Find all duplicate string names in a localization file
grep -o 'name="[^"]*"' RemixedDungeon/src/main/res/values-[lang]/strings_all.xml | sort | uniq -d
```

#### Method 2: Using a dedicated script
The project includes a script to detect and report duplicate strings across all localization files:

**Script location:** `tools/check_duplicate_strings.py`

**Usage:**
```bash
python3 tools/check_duplicate_strings.py /path/to/remixed-dungeon [language_code]
```

This script will:
- Scan all string entries in the specified language's strings_all.xml file
- Identify and list any duplicate string names
- Report the line numbers where duplicates occur
- Support scanning all languages if no language code is specified

### Removal Process

#### Manual Removal
1. Identify the duplicate strings using one of the detection methods above
2. Open the localization file in an editor
3. Remove the duplicate entries, keeping only the correct/updated version
4. Verify the XML remains well-formed after removal
5. Test the build to ensure the issue is resolved

#### Automated Removal
For cases with many duplicates (like the Greek localization), manual removal is tedious. The auto-fix functionality in `validate_translations.py` now handles this automatically.

### Prevention
- Always use the insertion tools (`insert_translated_string.py`) which prevent duplicate entries
- Regularly run duplicate checks as part of the localization workflow
- Validate localization files before committing changes using the validation script: `python3 tools/validate_translations.py /path/to/remixed-dungeon`
- The validation script now includes checks for duplicate strings as part of the standard validation process

## Common String Patterns

### UI Elements
- Prefixed with `Wnd` (window), `Btn` (button), `Lbl` (label), etc.
- Usually short, concise terms

### Game Entities
- Prefixed with entity type: `Mob_`, `Item_`, `Buff_`, `Spell_`, etc.
- Names and descriptions of game elements

### Game Mechanics
- Related to gameplay: `Health`, `Mana`, `Damage`, etc.
- Often appear in formulas or calculations

### Messages
- Feedback to player actions
- May contain formatting placeholders

## Common String ID Suffixes

Based on analysis of the strings_all.xml file, here are the most common suffixes used in Remixed Dungeon:

### Primary Suffixes (Most Frequent)
- `_Name` (439 occurrences): The primary name of an entity, item, or UI element
- `_Info` (308 occurrences): Descriptive information about an entity or item
- `_Gender` (161 occurrences): Grammatical gender of the entity name (masculine, feminine, neuter)
- `_Desc` (144 occurrences): Detailed description of an entity or item
- `_Name_Objective` (113 occurrences): The name of an entity in objective case (for grammatical purposes)

### Secondary Suffixes
- `_Info1`, `_Info2`, `_Info3`, `_Info4`: Additional or alternative information strings
- `_Title` (21 occurrences): Title for windows, sections, or UI elements
- `_Defense`: Defense-related text for entities
- `_Txt`: General text content
- `_Name_0`, `_Name_1`, `_Name_2`: Multiple name variations for the same entity
- `_Message`, `_Message1`, `_Message2`, `_Message3`: Message text for various purposes
- `_Death`: Text displayed when an entity dies
- `_ACSpecial`: Special action button text for entities
- `_TileWater`, `_TileGrass`, `_TileHighGrass`: Text related to specific tile types
- `_TileDesc*`: Descriptive text for various tile types (e.g., `_TileDescDeco`, `_TileDescStatue`)
- `_Killed`: Text when an entity is killed
- `_Prompt`: Prompt text for user input
- `_Apply`: Text for apply or use actions
- `_Quest_End`, `_Quest_Reminder`: Quest-related text
- `_Use`: Use action text
- `_Select`: Select action text
- `_Ok`, `_Yes`, `_No`: Common response buttons
- `_Taste`: Text related to tasting items
- `_LooksBetter`: Text for appearance improvements

### UI-Specific Suffixes
- `_title` (lowercase): Usually for window or section titles
- `_text` (lowercase): General text content in UI
- `_name` (lowercase): Name fields in UI
- `_desc` (lowercase): Description fields in UI
- `_InvTitle`: Inventory-related titles

These suffixes help translators understand the purpose of each string and maintain consistency in translation. When translating, consider the suffix to understand the context and grammatical role of the string in the game.

This guide should help translators effectively identify and fill in missing localization strings while maintaining consistency and quality throughout the Remixed Dungeon localization effort.