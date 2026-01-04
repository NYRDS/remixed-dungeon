# Translation Task Guide for Remixed Dungeon

This guide explains how to identify and fill in missing localization strings in Remixed Dungeon, using English, Russian, and code as references.

## Overview

Remixed Dungeon uses Android's string resource system for localization. All strings are stored in XML files in the `values` directories under `RemixedDungeon/src/main/res/`. The English strings are in `values/strings_all.xml`, and localized versions are in directories like `values-ru/strings_all.xml`, `values-es/strings_all.xml`, etc.

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

Add the new string to the appropriate `strings_all.xml` file in the target language directory:

```xml
<string name="your_string_id">Your translation here</string>
```

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
   Add to `values-de/strings_all.xml`:
   ```xml
   <string name="WndJournal_Levels">Ebenen</string>
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