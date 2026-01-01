# Remixed Dungeon Wiki Documentation - Comprehensive Guide

## Overview

The Remixed Dungeon wiki uses DokuWiki as its platform (located in the `wiki-data` submodule) and can be significantly improved by extracting information directly from the game's source code. This document provides guidance on how to enhance and maintain the wiki by leveraging information sources within the game code, while following established maintenance rules and best practices.

When creating wiki content, it's important to verify information against all code sources:
- **Java code**: The main game logic is implemented in Java files in `RemixedDungeon/src/main/java/`
- **Lua code**: Custom game mechanics and behaviors are implemented in Lua scripts in `RemixedDungeon/src/main/assets/scripts/`
- **JSON configuration files**: Game data, levels, objects, and other configurations are defined in various JSON files across the `RemixedDungeon/src/main/assets/` directory
- **String resources**: Localized game text is stored in string resource files, with Russian strings specifically in `RemixedDungeon/src/main/res/values-ru/strings_all.xml` which serves as the source of truth for Russian wiki pages

## Wiki Namespace Structure

The Remixed Dungeon wiki uses a namespace system to organize content by language and content type. The primary namespace for English content is `en:rpd:` which contains all English Remixed Dungeon game documentation.


### Python Script Execution
- When running Python tools for wiki maintenance, use `python3`:
  - `python3 tools/py-tools/find_red_links.py`
  - `python3 tools/py-tools/scale_sprites_for_wiki.py`
  - `python3 tools/py-tools/check_broken_images.py`
  - `python3 tools/py-tools/dokuwiki_linter.py`
  - And other Python scripts in the tools/py-tools directory

Always include references to the source code when documenting game mechanics, as this allows other contributors to verify and update information as the game evolves. For example:
- Reference Java classes like `com/watabou/pixeldungeon/actors/mobs/Skeleton.java`
- Reference Lua scripts like `RemixedDungeon/src/main/assets/scripts/spells/heal.lua`
- Reference JSON configuration files like `RemixedDungeon/src/main/assets/levelsDesc/Bestiary.json`

## Localization for Multiple Languages

The Remixed Dungeon game has localization available in multiple languages in the string resource files. When creating or updating wiki pages for a specific language, use the corresponding `strings_all.xml` file as the authoritative source for text content. These files contain all localized strings used in the game including:

- Item names and descriptions
- Mob names and descriptions
- Spell and ability text
- UI messages and prompts
- Game mechanics descriptions

### Available Languages and Their String Resource Files

**Chinese (Simplified)**: `RemixedDungeon/src/main/res/values-zh-rCN/strings_all.xml`
For Chinese wiki pages located in the `wiki-data/pages/cn/` directory, ensure consistency with the official Chinese translation provided in this file.

**Chinese (Traditional)**: `RemixedDungeon/src/main/res/values-zh-rTW/strings_all.xml`
For Traditional Chinese wiki pages located in the `wiki-data/pages/zh/` directory, ensure consistency with the official Traditional Chinese translation provided in this file.

**Russian**: `RemixedDungeon/src/main/res/values-ru/strings_all.xml`
For Russian wiki pages located in the `wiki-data/pages/ru/` directory, ensure consistency with the official Russian translation provided in this file.

**Spanish**: `RemixedDungeon/src/main/res/values-es/strings_all.xml`
For Spanish wiki pages located in the `wiki-data/pages/es/` directory, ensure consistency with the official Spanish translation provided in this file.

**French**: `RemixedDungeon/src/main/res/values-fr/strings_all.xml`
For French wiki pages located in the `wiki-data/pages/fr/` directory, ensure consistency with the official French translation provided in this file.

**German**: `RemixedDungeon/src/main/res/values-de/strings_all.xml`
For German wiki pages located in the `wiki-data/pages/de/` directory, ensure consistency with the official German translation provided in this file.

**Portuguese (Brazilian)**: `RemixedDungeon/src/main/res/values-pt-rBR/strings_all.xml`
For Portuguese wiki pages located in the `wiki-data/pages/pt/` directory, ensure consistency with the official Brazilian Portuguese translation provided in this file.

**Italian**: `RemixedDungeon/src/main/res/values-it/strings_all.xml`
For Italian wiki pages located in the `wiki-data/pages/it/` directory, ensure consistency with the official Italian translation provided in this file.

**Japanese**: `RemixedDungeon/src/main/res/values-ja/strings_all.xml`
For Japanese wiki pages located in the `wiki-data/pages/ja/` directory, ensure consistency with the official Japanese translation provided in this file.

**Korean**: `RemixedDungeon/src/main/res/values-ko/strings_all.xml`
For Korean wiki pages located in the `wiki-data/pages/ko/` directory, ensure consistency with the official Korean translation provided in this file.

**Polish**: `RemixedDungeon/src/main/res/values-pl/strings_all.xml`
For Polish wiki pages located in the `wiki-data/pages/pl/` directory, ensure consistency with the official Polish translation provided in this file.

**Ukrainian**: `RemixedDungeon/src/main/res/values-uk/strings_all.xml`
For Ukrainian wiki pages located in the `wiki-data/pages/uk/` directory, ensure consistency with the official Ukrainian translation provided in this file.

**Hungarian**: `RemixedDungeon/src/main/res/values-hu/strings_all.xml`
For Hungarian wiki pages located in the `wiki-data/pages/hu/` directory, ensure consistency with the official Hungarian translation provided in this file.

**Turkish**: `RemixedDungeon/src/main/res/values-tr/strings_all.xml`
For Turkish wiki pages located in the `wiki-data/pages/tr/` directory, ensure consistency with the official Turkish translation provided in this file.

**Indonesian**: `RemixedDungeon/src/main/res/values-in/strings_all.xml`
For Indonesian wiki pages located in the `wiki-data/pages/id/` directory, ensure consistency with the official Indonesian translation provided in this file.

**Malay**: `RemixedDungeon/src/main/res/values-ms/strings_all.xml`
For Malay wiki pages located in the `wiki-data/pages/ms/` directory, ensure consistency with the official Malay translation provided in this file.

**Greek**: `RemixedDungeon/src/main/res/values-el/strings_all.xml`
For Greek wiki pages located in the `wiki-data/pages/el/` directory, ensure consistency with the official Greek translation provided in this file.

Using the appropriate string resource file for each language ensures that wiki content matches the in-game experience for speakers of that language.

## Wiki Maintenance Rules

### File Naming Convention
- All wiki page files must use lowercase names with underscores separating words
- Example: `chaos_armor_item.txt`, `air_elemental_mob.txt`, `potion_of_healing_item.txt`
- No capitalized filenames are allowed (e.g., no `Chaos_Armor.txt`, `Air_Elemental.txt`)

### Workflow and Collaboration Rules
- **Pull repo and analyze diff before attempting any changes**: Always pull the latest changes from the repository and analyze the diff to understand what has changed before making any modifications to wiki files. This prevents merge conflicts and ensures consistency with recent updates.
- Always commit and push your updates after making changes to wiki files to ensure your contributions are preserved and shared with the community.

### Conversion Strategy
- CamelCase names should be converted to snake_case (e.g., `TheSoulbringer` → `the_soulbringer_item`)
- Proper names should follow the same rule (e.g., `ArcaneStylus` → `arcane_stylus_item`)

### Entity Namespace Separation
To avoid confusion between similar entity names (e.g., a mob and hero subclass both named "Shaman"), different entity types should use different naming conventions. When an entity can exist in multiple forms (e.g., Lich as both a boss mob and a hero subclass), it's important to distinguish these clearly.

#### Mobs (Monsters/Enemies)
- Pages for dungeon mobs and enemies should use `_mob` suffix
- Example: `shaman_mob.txt`, `lich_mob.txt`, `gnoll_mob.txt`
- Current exceptions like `lich_mob.txt` and `crystal_mob.txt` should be maintained
- Boss entities that also serve as unlock mechanisms for subclasses should be clearly identified as mobs with the `_mob` suffix

#### Hero Classes
- Pages for main playable classes use `_class` suffix
- Example: `warrior_class.txt`, `mage_class.txt`, `rogue_class.txt`, `elf_class.txt`, `necromancer_class.txt`
- Hero class pages should use the hero's class sprite, not NPC sprites (e.g., use `hero_NECROMANCER.png` not `mob_NecromancerNPC.png`)

#### Hero Subclasses
- Pages for mastery paths or subclasses use `_subclass` suffix
- Example: `shaman_subclass.txt` (Shaman subclass for Elf class), `scout_subclass.txt` (Scout subclass for Elf class), `lich_subclass.txt` (Lich subclass for Necromancer class)
- Subclass pages should use the appropriate hero subclass sprite (e.g., use `hero_NECROMANCER_LICH.png` for the Lich subclass, not `mob_Lich.png`)
- When a subclass shares a name with a mob, ensure the subclass page clearly indicates it's a subclass and the mob page uses the `_mob` suffix

#### Items
- All items follow the `_item` format with `_item` suffix
- Example: `wooden_bow_item.txt`, `potion_of_healing_item.txt`, `ring_of_frost_item.txt`, `short_sword_item.txt`, `ankh_item.txt`, `scroll_of_identify_item.txt`

#### Spells
- All spells use `_spell` suffix to distinguish from items with similar names
- Example: `healing_spell.txt`, `ignite_spell.txt`, `wind_gust_spell.txt`, `magic_torch_spell.txt`
- When an item and spell share similar names (e.g., a healing potion and healing spell), use the suffix to differentiate

#### NPCs
- Non-player characters use `_npc` suffix
- Example: `shopkeeper_npc.txt`, `wandmaker_npc.txt`, `ghost_npc.txt`, `necromancer_npc.txt`

#### Levels
- Level pages use `_level` suffix
- Example: `sewers_level.txt`, `prison_level.txt`, `caves_level.txt`, `necropolis_level.txt`

#### Game Mechanics
- Mechanics pages use `_mechanic` suffix
- Example: `combat_mechanic.txt`, `enchantments_mechanic.txt`, `alchemy_mechanic.txt`

#### Skills and Talents
- Skills and talents use `_skill` or `_talent` suffix
- Example: `berserker_rage_skill.txt`, `armor_proficiency_talent.txt`, `dual_wielding_talent.txt`

#### Buffs and Debuffs
- Buffs and debuffs use `_buff` suffix
- Example: `poison_buff.txt`, `invisibility_buff.txt`, `fury_buff.txt`, `paralysis_buff.txt`

#### Traps
- Traps use `_trap` suffix
- Example: `poison_trap.txt`, `alarm_trap.txt`, `dart_trap.txt`, `fire_trap.txt`

#### Scripts
- Scripts use `_script` suffix
- Example: `heal_script.txt`, `ignite_script.txt`, `mobs/init_script.txt`

#### Level Objects
- Level objects use `_level_object` suffix
- Example: `pedestal_level_object.txt`, `statue_level_object.txt`, `well_level_object.txt`, `barricade_level_object.txt`, `trap_fire_level_object.txt`
- Level objects are interactive or decorative elements that can be placed on dungeon levels
- They include: pedestals, statues, barricades, wells, pots, traps, and other environmental features
- These are configured through JSON files in the `levelObjects/` directory
- Some level objects have custom behaviors implemented in Lua scripts in the `scripts/objects/` directory

#### Configuration Files
- Configuration files use `_config` suffix
- Example: `hero_stats_config.txt`, `mob_spawn_config.txt`

#### Modding Configuration Files and Lua Libraries
It is worth creating wiki pages dedicated to important JSON configuration files and Lua libraries that are usable by modders, as these serve as key resources for custom content creation:

**Important JSON Config Files for Modders:**
- `levelsDesc/Bestiary.json` - Defines monster spawn rates and level placement
- `mobsDesc/` directory - Individual mob configuration files
- `levelObjects/` - Interactive objects like chests, statues, barricades
- `spritesDesc/` - Sprite animation and effect configurations
- `hero/initHeroes.json` - Hero class starting equipment and stats
- Various other JSON files across the `assets/` directory that define game mechanics

**Lua Libraries for Modders:**
- Scripts in `RemixedDungeon/src/main/assets/scripts/` directory provide examples of moddable functionality
- Core Lua libraries that facilitate custom item, mob, spell, and game mechanic implementations
- These should have dedicated documentation pages for modders to reference

Creating dedicated wiki pages for these important configuration files and Lua libraries will greatly benefit the modding community by providing clear documentation on how to extend and modify the game.

#### Quests
- Quests use `_quest` suffix
- Example: `alchemy_quest.txt`, `impossible_quest.txt`

#### Distinguishing Similar Entities
When entities share the same name across different types (like the Lich boss and Lich subclass):

1. **Clear Naming**: Use the suffix conventions mentioned above (`_mob` for mobs, `_class` for classes, `_subclass` for subclasses, `_spell` for spells, `_item` for items, `_npc` for NPCs, etc.) to distinguish between entity types

2. **Correct Sprites**: Each entity type should use the appropriate sprite that matches its in-game representation:
   - Hero classes: Use hero class sprites (e.g., `hero_NECROMANCER.png`)
   - Hero subclasses: Use hero subclass sprites (e.g., `hero_NECROMANCER_LICH.png`)
   - Mobs/Bosses: Use mob sprites (e.g., `mob_Lich.png` → `lich_sprite.png`)
   - NPCs: Use NPC sprites (e.g., `mob_NecromancerNPC.png` → `necromancernpc_sprite.png`)

3. **Contextual Clarity**: In the content, make it clear what type of entity is being described:
   - For mobs: "The **Lich** is a powerful undead boss enemy..."
   - For subclasses: "The **Lich** is a hero subclass for the Necromancer class..."
   - For NPCs: "The **Necromancer** is a special non-player character found in..."

4. **Cross-References**: When entities are related, link them appropriately:
   - The Lich subclass page should link to the Lich mob page: `[[en:rpd:lich_mob|Lich (Mob)]]`
   - The Lich mob page should link to the Lich subclass: `[[en:rpd:lich_subclass|Lich (Subclass)]]`
   - The Necromancer class page should reference the NPC: `[[en:rpd:necromancer_npc|Necromancer NPC]]`

### Content Organization
- Each wiki page should have exactly one file in the correct lowercase naming format
- No duplicate content should exist in separate files
- When merging content from duplicate files, combine all information in the lowercase-named file
- Use consistent heading formats: `====== Page Title ======`
- Organize content with clear sections using `==== Section Title ====`
- Include relevant tags at the bottom: `{{tag> rpd items}}` or `{{tag> rpd mobs}}`

## Content Quality Standards

### Information Accuracy
- All game data (stats, mechanics, effects, drop rates) must be verified against source code
- Include specific numerical values rather than vague descriptions
- When documenting items, include stats, usage restrictions, and exact effects
- For mobs, document HP, damage, special abilities, resistances, and drop tables

### Comprehensive Coverage
- Describe both the mechanical effects and strategic implications of game elements
- Include information about where items/mobs can be found or obtained
- Explain synergies with other items or mechanics when relevant
- Document any special behaviors, AI patterns, or unique mechanics

### Formatting and Style
- Use bullet points for lists of properties, effects, or characteristics
- Include code-style references when mentioning other game elements (e.g., `[[en:rpd:sword_item|Sword]]`)
- Present information in order of importance to the player
- Provide examples where helpful for understanding complex mechanics

### Technical Information
- When documenting mechanics from source code, include the relevant class names
- Reference game constants and formulas where they enhance understanding
- Cite string resource names for accuracy of in-game text
- Link to related mechanics or concepts for better context

## Internal Linking Standards

### Link Format
- All internal links must use lowercase page names in the format: `[[en:rpd:page_name|Display Text]]`
- For same-namespace links, use: `[[page_name|Display Text]]`
- Avoid linking to capitalized file names that no longer exist

### Language Consistency
- All internal links on a page should lead to pages in the same language
- The only exception is links to the same page in other languages, which should be placed at the bottom of the page
- For example, a Russian wiki page may link to English versions of the same content at the bottom of the page, but not within the main content

### Cross-references
- When linking to related pages, ensure the target page exists in lowercase format
- Use descriptive display text that clarifies the link's purpose
- Maintain consistent terminology across linked pages

### Code References
- **Avoid using the code: namespace for links** as it creates numerous red links in the wiki
- Instead of `[[code:RemixedDungeon/src/main/java/com/example/Example.java|Example.java]]`, link directly to the GitHub repository using the format: `[[https://github.com/NYRDS/remixed-dungeon/blob/master/RemixedDungeon/src/main/java/com/example/Example.java|Example.java]]`
- This ensures all code references remain accessible and prevents broken links in the wiki

## Image Standards

### Adding Mob/Character Sprites
- To add a mob sprite to a wiki page, use the centered format with original size: `{{ rpd:images:[mob_name].png|Alt Text }}`
- Enhanced sprites for wiki use are pre-generated using `tools/py-tools/scale_sprites_for_wiki.py` which scales and frames raw game sprites with nearest-neighbor filtering
- The processed sprites are available in the `wiki-data/media/rpd/images/` directory and ready to use in wiki pages
- Enhanced images follow the same naming convention as input sprites with additional processing (scaling, background, and frame applied)
- For centered display, add spaces around the image reference: `{{ rpd:images:image.png|Alt Text }}`
- For original image size (no scaling), omit the size parameter entirely

### Image Naming Scheme and Processing Pipeline
The `scale_sprites_for_wiki.py` script follows a specific naming scheme when processing game sprites to align with the page naming convention:

#### Input Sprite Naming Convention
The raw sprites in the input directory (`sprites/`) follow these naming patterns:
- **Mobs (Enemies)**: `mob_[MobName].png` (e.g., `mob_Tengu.png`, `mob_Goo.png`, `mob_AirElemental.png`)
- **Items**: `item_[ItemName].png` (e.g., `item_TenguLiver.png`, `item_Ankh.png`, `item_ArmorKit.png`)
- **Spells**: `spell_[SpellName].png` (e.g., `spell_Heal.png`, `spell_MagicTorch.png`, `spell_Ignite.png`)
- **Buffs/Status Effects**: `buff_[BuffName].png` (e.g., `buff_Burning.png`, `buff_Poison.png`, `buff_Invisibility.png`)
- **Hero Classes/Sprites**: `hero_[ClassName].png` or `hero_[Class]_[Subclass].png` (e.g., `hero_WARRIOR.png`, `hero_WARRIOR_GLADIATOR.png`)
- **Level Objects**: `levelObject_[ObjectName].png` (e.g., `levelObject_Pedestal.png`, `levelObject_Statue.png`, `levelObject_Well.png`, `levelObject_FireTrap.png`)

#### Output Sprite Naming
- The processed sprites maintain the exact same filename as input sprites with additional processing
- All output sprites are in PNG format with scaling (8x by default), background, and frame applied
- Processed sprites are stored in the `wiki-data/media/rpd/images/` directory in the wiki-data submodule

#### Wiki Integration Naming
For wiki page integration, the sprites follow the same naming convention as page names using snake_case:
- For most entities: `{{ rpd:images:[entity_name]_mob.png|Alt Text }}` (e.g., `{{ rpd:images:tengu_mob.png|Tengu }}`)
- For items: `{{ rpd:images:[item_name]_item.png|Alt Text }}` (e.g., `{{ rpd:images:ankh_item.png|Ankh }}`)
- For spells: `{{ rpd:images:[spell_name]_spell.png|Alt Text }}` (e.g., `{{ rpd:images:heal_spell.png|Heal Spell }}`)
- For buffs: `{{ rpd:images:[buff_name]_buff.png|Alt Text }}` (e.g., `{{ rpd:images:burning_buff.png|Burning }}`)
- For heroes: `{{ rpd:images:[hero_name]_hero.png|Alt Text }}` (e.g., `{{ rpd:images:warrior_gladiator_hero.png|Warrior Gladiator }}`)
- For NPCs: `{{ rpd:images:[npc_name]_npc.png|Alt Text }}` (e.g., `{{ rpd:images:shopkeeper_npc.png|Shopkeeper }}`)
- For levels: `{{ rpd:images:[level_name]_level.png|Alt Text }}` (e.g., `{{ rpd:images:sewers_level.png|Sewers Level }}`)
- For level objects: `{{ rpd:images:[object_name]_level_object.png|Alt Text }}` (e.g., `{{ rpd:images:pedestal_level_object.png|Pedestal }}`, `{{ rpd:images:fire_trap_level_object.png|Fire Trap }}`)

The processed sprites are automatically renamed to match the page naming scheme during processing (e.g., `mob_Tengu.png` becomes `tengu_mob.png`, `levelObject_Pedestal.png` becomes `pedestal_level_object.png`).

#### Special Processing Parameters
- **Scaling**: Images are scaled up by a factor of 8 (default) using nearest neighbor interpolation to maintain pixel art quality
- **Canvas Extension**: Adds transparent pixels (default 1 pixel in each direction) before scaling to avoid edge artifacts
- **Background**: Adds a light gray background (RGB: 240, 240, 240) for better visibility
- **Frame**: Adds a dark gray frame (RGB: 100, 100, 100) around the scaled image

#### Directory Structure
- Raw sprites: `/sprites/` (generated by FactorySpriteGenerator - pre-generated for environments without Gradle)
- Processed sprites for wiki: `/wiki-data/media/rpd/images/` (available in wiki-data submodule)
- Referenced in wiki pages via: `{{ rpd:images:filename.png|... }}`

### General Image Guidelines
- Place images at the beginning of the page for visual identification
- Use descriptive alt text when possible
- Store all new images in the appropriate subdirectory within `wiki-data/media/rpd/images/`
- Maintain consistent image sizing across similar page types
- For non-sprite images, use descriptive filenames in snake_case format
- Align image names with corresponding page names using the same entity type suffixes (_mob, _item, _spell, _buff, _npc, _hero, _level_object, etc.)

## DokuWiki Syntax Guide

For proper wiki maintenance, here's the DokuWiki syntax that should be used in wiki pages:

### Headers
DokuWiki uses multiple equal signs to create headers of different levels:
```
====== Level 1 Header ======
===== Level 2 Header =====
==== Level 3 Header ====
=== Level 4 Header ===
== Level 5 Header ==
= Level 6 Header =
```

### Text Formatting
- **Bold**: `**bold text**` - This will appear as **bold text**
- *Italic*: `//italic text//` - This will appear as //italic text//
- Underline: `__underlined text__` - This will appear as __underlined text__
- Monospace: `''monospace text''` - This will appear as ''monospace text''
- Combined: `**//__''all together''__//**` - This will appear as **//__''all together''__//**
- Subscript: `<sub>subscript</sub>` - This will appear as <sub>subscript</sub>
- Superscript: `<sup>superscript</sup>` - This will appear as <sup>superscript</sup>
- Deleted: `<del>deleted text</del>` - This will appear as <del>deleted text</del>

### Lists
- Unordered lists use asterisks with different levels of indentation:
```
  * Level 1 item
  * Another level 1 item
    * Level 2 item
      * Level 3 item
```
- Ordered lists use numbers with periods:
```
  1. Level 1 item
  2. Another level 1 item
    1. Level 2 item
      1. Level 3 item
```

### Links
- External links: Either just type the URL like `https://www.google.com` or use the format `[[https://www.google.com|Link Text]]`
- Internal links: `[[wiki_page_name|Display Text]]` or `[[en:rpd:page_name|Display Text]]` for Remixed Dungeon content
- Email links: `[[mailto:email@example.com|Email Me]]`
- Footnotes: `This is a sentence with a footnote((This is the footnote content))`

### Images
- Basic image: `{{image.png}}`
- Image with title: `{{image.png|This is a title}}`
- Resized image: `{{image.png?64}}` (resizes to 64 pixels)
- Centered image: `{{ image.png }}` (with spaces around the image reference)

### Tables
Tables in DokuWiki are created using carets and pipes:
```
^ Heading 1 ^ Heading 2 ^ Heading 3 ^
| Cell 1    | Cell 2    | Cell 3    |
| Cell 4    | Cell 5    | Cell 6    |
^ Heading 4 | Cell 7    | Cell 8    |
```

### Code Blocks
- For code blocks without syntax highlighting: `<code>your code here</code>`
- For code blocks with syntax highlighting: `<code java>your Java code here</code>`
- The language name (java in this example) can be replaced with other supported languages

### Other Useful Elements
- Horizontal rule: Four or more dashes `----`
- Force newline: `\\`
- Prevent formatting: `<nowiki>**text**</nowiki>`
- Paragraph breaks: Remember that a single newline won't create a newline in the rendered text; always use double newline when new paragraphs are required

## Key Information Sources in Game Code

When creating wiki content, reference these sources of truth for accurate information:

- **Java code**: The main game logic is implemented in Java files in `RemixedDungeon/src/main/java/`
- **Lua code**: Custom game mechanics and behaviors are implemented in Lua scripts in `RemixedDungeon/src/main/assets/scripts/`
- **JSON configuration files**: Game data, levels, objects, and other configurations are defined in various JSON files across the `RemixedDungeon/src/main/assets/` directory
- **String resources**: Localized game text is stored in string resource files, with Russian strings specifically in `RemixedDungeon/src/main/res/values-ru/strings_all.xml`
- **mr: namespace pages**: For AI-assisted content creation, check the corresponding mr: namespace page for raw code references and configuration excerpts related to each entity

### 1. Core Game Classes (RemixedDungeon/src/main/java/)

#### Hero Classes and Mechanics
- **Location**: `com/watabou/pixeldungeon/actors/hero/`
- **Key files**:
  - `Hero.java` - Main hero character logic, attributes, and capabilities
  - `HeroClass.java` - Available hero classes and their characteristics
  - `HeroAction.java` - Hero actions and behaviors
  - `Belongings.java` - Hero inventory and equipment system


#### Enemies/Mobs (Monsters)
- **Location**: `com/watabou/pixeldungeon/actors/mobs/`
- **Key files**:
  - `Mob.java` - Base mob class with common properties
  - `Skeleton.java`, `Thief.java`, `Gnoll.java`, etc. - Individual enemy implementations
  - `Boss.java` - Boss mob definitions and mechanics
  - `Elemental.java`, `Rat.java` - Enemy subtypes with specific mechanics

#### Items and Equipment
- **Location**: `com/watabou/pixeldungeon/items/`
- **Key files**:
  - `Item.java` - Base item class
  - `Weapon.java`, `Armor.java` - Equipment base classes
  - `Potion.java`, `Scroll.java`, `Ring.java` - Consumable items
  - `weapon/melee/`,  - Specific weapon types
  - `armor/glyphs/` - Armor enchantments
  - `artifacts/` - Special artifacts with unique mechanics

#### Spells and Abilities
- **Location**: `com/watabou/pixeldungeon/spells/`
- **Various spells with specific mechanics**

#### Level Generation and Areas
- **Location**: `com/watabou/pixeldungeon/levels/`
- **Key files**:
  - `Level.java` - Base level implementation
  - `SewerLevel.java`, `PrisonLevel.java`, `CavesLevel.java`, etc. - Specific level layouts
  - `rooms/` - Level room generation patterns

#### Game Mechanics
- **Location**: `com/watabou/pixeldungeon/mechanics/`
- **Key files**:
  - `Chasm.java` - Chasm falling mechanics
  - `Element.java` - Damage element types and resistances

#### Level Objects
- **Location**: `com/nyrds/pixeldungeon/levels/objects/`
- **Key files**:
  - `LevelObject.java` - Base class for all level objects
  - `LevelObjectsFactory.java` - Factory for creating level objects
  - `Deco.java` - Decorative objects that enhance visual appearance of levels
  - `CustomObject.java` - Lua-scripted objects with complex behaviors
  - `Trap.java` - Trap objects that trigger effects when activated
  - `scripts/objects/` directory - Lua scripts for custom object behaviors (e.g., pedestal.lua, statue.lua, well.lua)
  - `levelObjects/` directory - JSON configuration files for level objects (e.g., pedestal.json, statue.json, well.json)

#### Buffs and Debuffs
- **Location**: `com/watabou/pixeldungeon/buffs/`
- **Key files**:
  - `Buff.java` - Base buff class
  - `Bleeding.java`, `Burning.java`, `Poison.java` - Status effects with detailed mechanics
  - `Hunger.java` - Hunger system
  - `Invisibility.java` - Invisibility mechanics
  - `Regeneration.java` - Health regeneration

### 2. Automatically Generated Sprites and Entities (sprites/ and entities/ directories)

#### Pre-generated Sprite Directory
- **Location**: `sprites/` (created by `FactorySpriteGenerator` task)
- **Content**: Pre-generated sprite images for all game entities that can be enhanced for wiki use
- **Important Note**: The sprites directory and its contents are pre-generated because the Gradle build system required for the FactorySpriteGenerator task may not be available in all environments. These files should be generated in development environments and committed to the repository for use in environments without Gradle.
- **Sprite types**:
  - **Mobs**: Files named `mob_[EntityName].png` (e.g., `mob_Skeleton.png`, `mob_Tengu.png`)
  - **Items**: Files named `item_[EntityName].png` (e.g., `item_ScrollOfIdentify.png`, `item_RingOfMight.png`)
  - **Spells**: Files named `spell_[SpellName].png` (e.g., `spell_Heal.png`, `spell_MagicTorch.png`)
  - **Buffs**: Files named `buff_[BuffName].png` (e.g., `buff_Healing.png`, `buff_Burning.png`)
  - **Hero classes**: Files named `hero_[HeroClassName].png` (e.g., `hero_WARRIOR.png`, `hero_MAGE.png`)
  - **Hero subclasses**: Files named `hero_subclass_[HeroSubClassName].png` (e.g., `hero_subclass_GLADIATOR.png`, `hero_subclass_WARLOCK.png`)
  - **Hero class+subclass combinations**: Files named `hero_[HeroClassName]_[HeroSubClassName].png` (e.g., `hero_WARRIOR_GLADIATOR.png`, `hero_MAGE_WARLOCK.png`)
  - **Level objects**: Files named `levelObject_[EntityName].png` (e.g., `levelObject_Pedestal.png`, `levelObject_Statue.png`, `levelObject_Well.png`, `levelObject_FireTrap.png`)
- **Tools for working with sprites**:
  - **`tools/py-tools/scale_sprites_for_wiki.py`**: Enhances sprites with scaling, background, and frame for better wiki visualization (processed sprites are available in the wiki-data submodule)
  - **Usage**: `python3 tools/py-tools/scale_sprites_for_wiki.py -i sprites/ -o wiki-data/media/rpd/images/`
- **Generation command**: `./gradlew -c settings.desktop.gradle :RemixedDungeonDesktop:generateSpritesFromFactories`

#### Entity Lists Directory
- **Location**: `entities/` (created by `FactorySpriteGenerator` task)
- **Content**: Generated text files containing lists of all game entities
- **Important Note**: Like the sprites directory, the entities directory is also pre-generated because the Gradle build system required for the FactorySpriteGenerator task may not be available in all environments. These files should be generated in development environments and committed to the repository for use in environments without Gradle.
- **Files**:
  - **mobs.txt**: Complete list of all mob entity names
  - **items.txt**: Complete list of all item entity names
  - **spells.txt**: Complete list of all spell names
  - **buffs.txt**: Complete list of all buff names
  - **levelObjects.txt**: Complete list of all level object names
- **Tools for working with entities**:
  - **`tools/py-tools/find_red_links.py`**: Uses entity lists to identify gaps in wiki coverage
  - **`tools/py-tools/wiki_potential_links_optimized.py`**: Cross-references entity names with wiki content to identify potential links
- **Usage**: These files serve as authoritative source lists for entity names and can be used to verify wiki coverage

### 3. Sprites and Visual Assets (RemixedDungeon/src/main/java/)

#### Character Sprites
- **Location**: `com/watabou/pixeldungeon/sprites/`
- **Key files**:
  - `CharSprite.java` - Base character sprite class
  - `HeroSprite.java`, `MobSprite.java` - Specific sprite implementations
  - Individual sprite files for each mob and character

#### Visual Effects
- **Location**: `com/watabou/pixeldungeon/effects/`
- **Key files**:
  - `Speck.java` - Particle effects
  - `Wound.java` - Damage display effects
  - `EmoIcon.java` - Status icons

### 4. UI and Screens (RemixedDungeon/src/main/java/)

#### Game Interface
- **Location**: `com/watabou/pixeldungeon/ui/`
- **Key files**:
  - `GameScene.java` - Main game interface
  - `WndBag.java` - Inventory interface
  - `QuickSlotButton.java` - Quick slot mechanics
  - `StatusPane.java` - Player status display
  - `Toolbar.java` - Action toolbar

#### Windows and Dialogs
- **Location**: `com/watabou/pixeldungeon/windows/`
- **Key files**:
  - `WndItem.java` - Item information display
  - `WndHero.java` - Hero information window
  - `WndInfoItem.java` - Detailed item information

### 5. Modding Support (RemixedDungeon/src/main/java/)

#### Custom Content Creation
- **Location**: `com/nyrds/pixeldungeon/` and subdirectories
- **Key directories**:
  - `items/`, `mobs/`, `levels/`, `ai/`, `effects/`, `utils/` - Various custom content implementations
  - `mechanics/` - Custom mechanics including spells
  - `support/` - Mod support utilities

#### Content Generation
- **Location**: `com/nyrds/pixeldungeon/items/`, `com/nyrds/pixeldungeon/mobs/`
- **Key files**:
  - Various custom item and mob implementations

### 6. Game Configuration and Data

#### Localized Strings
- **Location**: `RemixedDungeon/src/main/assets/l10ns/`
- **Files**: `strings_*.json` - Localized game text that can be used for game descriptions

#### Item and Mob Configuration
- **Location**: Various JSON files in `RemixedDungeon/src/main/assets/`
- **Files**:
  - `hero/initHeroes.json` - Hero class starting equipment, stats, and special properties
  - `levelsDesc/Bestiary.json` - Monster spawn rates and level placement
  - `levelsDesc/` directory - Level-specific configuration files
  - `mobsDesc/` directory - Mob-specific configuration files
  - `spritesDesc/` directory - Sprite animation and effect configurations
  - `levelObjects/` directory - Interactive objects configuration

#### Resource Strings
- **Location**: `RemixedDungeon/src/main/res/values/`
- **Files**: `strings-all.xml` - Main string resources with game text, descriptions, and labels that can be used for wiki content

#### Asset Configuration Files
- **Location**: `RemixedDungeon/src/main/assets/`
- **Key directories**:
  - `levelObjects/` - JSON files defining interactive objects like chests, statues, barricades, etc.
  - `mobs/` - Mob sprite images and related configurations
  - `items/` - Item sprite images and related configurations
  - `scripts/` - Lua scripts defining custom game content
  - `effects/` - Visual effect configurations
  - `fonts/` - Font definitions
  - `sounds/` - Sound asset configurations
  - `tilemapDesc/`, `tilesets/` - Level tilemap configurations
  - `levelsDesc/` - Level description files
  - `mobsDesc/` - Mob description configurations
  - `spritesDesc/` - Sprite description configurations

#### JSON-based Content
- **Location**: Various directories in `RemixedDungeon/src/main/assets/`
- **Files**: 220+ JSON files distributed across asset directories defining:
  - Level objects (chests, statues, barricades, etc.) in `levelObjects/`
  - Item properties and metadata
  - Mob configurations and stats
  - Level layouts and features
  - Special event configurations
  - Shop inventory and prices
  - Game mechanics parameters

#### Lua Scripting Content
- **Location**: `RemixedDungeon/src/main/assets/scripts/`
- **Key directories**:
  - `actors/` - Actor behavior scripts
  - `ai/` - AI behavior scripts
  - `buffs/` - Status effect scripts
  - `items/` - Custom item functionality
  - `lib/` - Utility libraries
  - `mobs/` - Custom mob behaviors
  - `npc/` - NPC scripts (Bard.lua, Innkeeper.lua, PlagueDoctor.lua, etc.)
  - `objects/` - Level objects and features
  - `services/` - Game services
  - `spells/` - Spell mechanics
  - `startup/` - Initialization scripts
  - `stats/` - Equipment stats (shields.lua, etc.)
  - `stuff/` - Various game components
  - `traps/` - Trap mechanics
  - `userServices/` - User service implementations
- **Files**: Lua scripts that define:
  - Custom game mechanics
  - Special event behaviors
  - Dynamic content generation
  - Scripted interactions and dialogues
  - Advanced item functions not available in Java base code
  - NPC behaviors and dialogue
  - Custom mob AI and mechanics

#### Limitations and Capabilities of JSON vs Lua/Java for Game Entities
- **Can be defined with JSON only**: Mobs, sprites, level objects, and some basic mechanics can be defined using JSON configuration files
- **Require Lua or Java implementation**: Items, buffs, and spells require actual code implementation in either Lua scripts or Java classes
- **Preference for modding**: Lua scripting is preferred for modding over Java implementation due to easier distribution and loading
- **Modding reference**: For detailed modding information, see `docs/modding.md` which provides comprehensive guides for creating custom content

## Hero Class and Subclass Pages

To maintain consistency and completeness in documentation of hero classes and subclasses, a script has been created to generate preview content for wiki pages.

### Hero Class Pages
- All hero classes (Warrior, Mage, Rogue, Huntress, Elf, Necromancer, Gnoll, Priest, Doctor) have dedicated wiki pages
- Each hero class page should include:
  - Description of the class's unique characteristics
  - Starting equipment and stats (from initHeroes.json)
  - Special abilities and mechanics
  - Sprite information where applicable
  - Magic affinity (Combat, Elemental, Rogue, Huntress, Elf, Necromancy, Witchcraft, Priest, PlagueDoctor)
  - Any class-specific mechanics (forbidden actions, friendly mobs, immunities, etc.)

### Hero Subclass Pages
- All hero subclasses (Gladiator, Berserker, Warlock, BattleMage, Assassin, FreeRunner, Sniper, Warden, Scout, Shaman, Lich, WitchDoctor, Guardian) have dedicated wiki pages
- Subclasses are mastery paths available for specific hero classes
- Each subclass page should include:
  - Description of the subclass's unique abilities
  - Which base class it belongs to
  - How to unlock the subclass (typically via Tome of Mastery after Boss #2)
  - Special mechanics and gameplay changes
  - Special armor obtained via ArmorKit (GladiatorArmor, BerserkArmor, WarlockArmor, BattleMageArmor, etc.) - these are special armors obtained by using the ArmorKit (found in City level, dropped by King boss) on any regular armor, which transforms it into the class-specific armor


## Spell Documentation and Automated Generation

### Spell Wiki Pages Structure

Spell documentation in the wiki follows the same general structure as other entity pages but includes specific sections relevant to spell mechanics:

#### Standard Spell Page Sections
- **Title**: Using the `_spell` suffix (e.g., `healing_spell.txt`, `ignite_spell.txt`)
- **Image**: Spell icon at the top of the page using `{{ rpd:images:spell_name_spell.png|Spell Name Icon }}`
- **Description**: Detailed explanation of the spell's effect based on source code analysis
- **Stats**: Magic affinity, targeting type, level, mana cost, and special effects
- **Usage**: Specific applications of the spell in gameplay
- **Classes**: Which classes typically have access to this spell affinity
- **Strategy**: Advanced tips for using the spell effectively
- **Data Validation**: Information about source code references and verification
- **Source Code**: Direct links to the relevant Java or Lua files
- **See Also**: Related spells and mechanics
- **Tags**: Appropriate categorization tags

### Spell Content Verification

Spell pages must include verification information to ensure accuracy:

#### Content Verification Section
Each spell page includes a "Content Verification" section that provides:
- Information source (Java Class or Lua Script)
- Stats verification status (extracted directly from spell class properties)
- Effect descriptions source (code analysis and string resources)
- Last updated date and source file name

#### Source Code References
Each spell page links directly to the relevant source code file on GitHub, allowing readers to verify information and understand implementation details.

### Automated Spell Documentation Generation

#### Java Spell Processing
- Located in `com.nyrds.pixeldungeon.mechanics.spells/`
- Extracts properties like magic affinity, targeting type, level, and mana cost from constructor
- Analyzes `cast()` method to determine special effects
- Uses string resources for descriptions where available

#### Lua Spell Processing
- Located in `RemixedDungeon/src/main/assets/scripts/spells/`
- Reads spell properties from the `desc` function in each Lua file
- Extracts name, targeting type, magic affinity, level, and mana cost
- Uses string resources for descriptions via the 'info' parameter

### Spell Affinity Categories

The wiki recognizes multiple spell affinity categories which should be linked appropriately:
- Common: General-purpose spells (e.g., Town Portal, Magic Torch)
- Elemental: Fire, ice, and nature-based spells (e.g., Ignite, Root Spell)
- Necromancy: Undead and death-related spells (e.g., Raise Dead, Exhumation)
- Combat: Offensive and defensive spells (e.g., Smash, Die Hard)
- Rogue: Stealth and agility spells (e.g., Backstab, Haste)
- Witchcraft: Magic and manipulation spells (e.g., Lightning Bolt, Roar)
- Huntress: Nature and charm spells (e.g., Calm, Charm)
- Elf: Natural magic spells (e.g., Magic Arrow, Sprout)
- Priest: Healing and sanctity spells (e.g., Heal, Order)
- PlagueDoctor: Disease and toxic spells (e.g., Plague, GasBomb)

## Item Sprite Generation and Management

### Understanding Item Sprite System

The Remixed Dungeon game uses a sprite sheet system for managing item images:

#### Main Items Sprite Sheet
- Located at `RemixedDungeon/src/main/assets/items.png` (256x256 pixels in 16x16 grid)
- Additional specialized sheets for different categories like `items/armor.png`, `items/potions.png`, etc.
- Each individual sprite is 16x16 pixels

#### Java-Based Mapping
- `ItemSpriteSheet.java` contains constants that map each item image to an index
- Each item class in the Java code references these constants via the `image` field
- Example: `image = ItemSpriteSheet.ANKH;`

#### Runtime Processing
- The game engine automatically extracts individual sprites from sheets at runtime
- Sprites are positioned in the sheet based on their index
- Extracted sprites are scaled using nearest-neighbor interpolation

### Sprite Enhancement for Wiki Visualization

#### Enhanced Sprite Scaling and Framing
- `tools/py-tools/scale_sprites_for_wiki.py` - Enhances raw sprites from the `sprites/` directory for better wiki visualization (processed sprites are available in the wiki-data submodule)
  - **Note**: Processed sprites are available in the `wiki-data/media/rpd/images/` directory and ready to use in wiki pages. The tool works with sprites generated by the FactorySpriteGenerator Gradle task (`./gradlew -c settings.desktop.gradle :RemixedDungeonDesktop:generateSpritesFromFactories`) which creates raw game sprites in the `sprites/` directory
  - Scales sprites 8x using nearest neighbor interpolation
  - Extends canvas by 1 transparent pixel in each direction before scaling (to prevent edge artifacts)
  - Adds a background and frame for better visualization
  - Preserves transparency
  - Automatically renames sprites to match the page naming convention (e.g., `mob_Tengu.png` becomes `tengu_mob.png`)
  - Processes all supported image formats in the input directory
  - Usage: `python3 tools/py-tools/scale_sprites_for_wiki.py -i sprites/ -o wiki-data/media/rpd/images/`
  - Additional options:
    - `-s, --scale`: Scale factor (default: 8)
    - `-c, --canvas-extension`: Number of transparent pixels to add in each direction before scaling (default: 1)
    - `--bg-color`: Background color as R G B values (default: 240 240 240)
    - `--frame-color`: Frame color as R G B values (default: 100 100 100)

#### Image Quality Settings for Wiki Display
- Use nearest-neighbor scaling to preserve pixel art quality
- Scale factor of 8x for optimal visibility in wiki pages
- Maintain square aspect ratio (16x16 original becomes 128x128)
- Add background and frame to enhance visual presentation in wiki context
- Preserve transparency to maintain visual fidelity of game sprites

#### Naming Convention
- Enhanced sprites follow the format: `{entity_name}_{entity_type}.png` (e.g., `tengu_mob.png`, `ankh_item.png`, `heal_spell.png`)
- In wiki pages, use: `{{ rpd:images:entity_name_entity_type.png|Alt Text }}` (matching the page naming scheme)
- For centering in DokuWiki: `{{ rpd:images:image.png|Alt Text }}` (with spaces)

### Custom Sprite Configurations

Some items and mobs use advanced sprite configurations defined in JSON files in `RemixedDungeon/src/main/assets/spritesDesc/`:

#### Advanced Features
- Particle emitters (e.g., for Fetid Rat's gas effect)
- Event handlers (e.g., Shopkeeper coin toss effect)
- Water ripple effects (e.g., for Piranha)
- Camera shake effects (e.g., for Rotting Fist)
- Immediate removal effects (e.g., for Imp)

#### JSON Configuration Format
```json
{
  "texture": "filename.png",
  "width": 16,
  "height": 16,
  "idle": { "fps": 2, "looped": true, "frames": [0,0,0,1] },
  "run": { "fps": 14, "looped": true, "frames": [6,7,8,9,10] },
  "particleEmitters": { /* particle configuration */ }
}
```

## Class Unlock Requirements and Hero Mechanics

### Class Unlock Requirements
- **Location**: `RemixedDungeon/src/main/java/com/watabou/pixeldungeon/scenes/StartScene.java`
- **Key Information**:
  - `huntressUnlocked = Badges.isUnlocked(Badges.Badge.BOSS_SLAIN_3) || (GamePreferences.donated() >= 1)` - Huntress unlocked by defeating 3rd boss (DM300) or by donating 1+
  - `elfUnlocked = Badges.isUnlocked(Badges.Badge.BOSS_SLAIN_4) || (GamePreferences.donated() >= 2)` - Elf unlocked by defeating 4th boss (The King) or by donating 2+
  - `gnollUnlocked = Badges.isUnlocked(Badges.Badge.GNOLL_UNLOCKED) || (GamePreferences.donated() >= 3)` - Gnoll unlocked by healing Gnoll Brute at town priest or by donating 3+
  - Boss unlock badges are validated in `RemixedDungeon/src/main/java/com/watabou/pixeldungeon/Badges.java`
  - Additional classes: Doctor and Priest have "Coming Soon" status and are not yet unlocked through gameplay

### Hero Classes and Subclasses
- **Available Classes** (9 total): Warrior, Mage, Rogue, Huntress, Elf, Necromancer, Gnoll, Priest, Doctor
- **Location**: `RemixedDungeon/src/main/java/com/watabou/pixeldungeon/actors/hero/HeroClass.java`
- **Starting Properties**: Defined in `RemixedDungeon/src/main/assets/hero/initHeroes.json`
  - **Common properties**: Cloth armor, 100 gold, 10 STR, 20 HP, 10 skill points
  - **Warrior**: ShortSword, 11 STR, PotionOfStrength, BodyArmor spell, Combat affinity
  - **Mage**: WandOfMagicMissile, ScrollOfIdentify, WindGust spell, 15 SP, Elemental affinity
  - **Rogue**: RingOfShadows +1, ScrollOfMagicMapping, Cloak spell, Rogue affinity
  - **Huntress**: Boomerang, ShootInEye spell, 15 HP, Huntress affinity
  - **Elf**: ElvenBow +1, 20 CommonArrows, ElvenDagger, MagicArrow spell, immunity to Roots, 9 STR, 15 HP, Elf affinity
  - **Necromancer**: NecromancerRobe, SkeletonKey for level 7, SummonDeathling spell, 15 HP, Necromancy affinity
  - **Gnoll**: GnollTamahawk, NoItem armor (starts with no armor), 8 Darts, Roar spell, high STR (15) low HP (5), Witchcraft affinity
  - **Priest**: ScrollOfRemoveCurse, Priest affinity
  - **Doctor**: Various potions (Healing, ToxicGas, ParalyticGas), PlagueDoctor affinity

### Hero Subclasses
- **Available Subclasses** (13 total): Gladiator, Berserker, Warlock, BattleMage, Assassin, FreeRunner, Sniper, Warden, Scout, Shaman, Lich, WitchDoctor, Guardian
- **Location**: `RemixedDungeon/src/main/java/com/watabou/pixeldungeon/actors/hero/HeroSubClass.java`
- **Unlocking**: Via Tome of Mastery after boss #2 (Tengu) is defeated
  - **Warrior subclasses**: Gladiator, Berserker
  - **Mage subclasses**: Warlock, BattleMage
  - **Rogue subclasses**: Assassin, FreeRunner
  - **Huntress subclasses**: Sniper, Warden
  - **Elf subclasses**: Scout, Shaman
  - **Necromancer subclasses**: Lich, WitchDoctor
  - **Gnoll subclasses**: Guardian

### Monster Spawning and Level Configuration
- **Location**: `RemixedDungeon/src/main/assets/levelsDesc/Bestiary.json`
- **Key Information**: Defines which monsters appear on which levels and their spawn weights/rates

### Class Unlock Validation
- **Location**: `RemixedDungeon/src/main/java/com/nyrds/pixeldungeon/windows/WndPriest.java`
- **Key Information**:
  - `Badges.validateGnollUnlocked()` is called when healing a Gnoll Brute NPC
  - Brute healing mechanism: `if(patient.getEntityKind().equals("Brute")) { Badges.validateGnollUnlocked(); }`
  - Unlocking also possible via donations (1+ for Huntress, 2+ for Elf, 3+ for Gnoll)

### Class-Specific Perks and Mechanics
- **Location**: `RemixedDungeon/src/main/java/com/watabou/pixeldungeon/actors/hero/HeroClass.java`
- **Key Information**:
  - Each class has unique mechanics in methods like `attackSkillBonus()`, `allowed()`, etc.
  - The `allowed()` method checks `initHeroes.has(name())` to determine if class is available
  - Special mechanics like Doctor's accuracy penalty when not using Bone Saw (5% chance to receive accuracy penalty, -5 to attack skill)
  - Hero classes have different dew bonus: Huntress and Elf get +1 dew bonus
  - Elf class has natural haste (hasteLevel = 1)
  - Gnoll class has forbidden actions (Scroll_ACRead, TomeOfMastery_ACRead, Stylus_ACInscribe)
  - Gnoll class has friendly mobs (Gnoll, Shaman, Brute, Shielded, ShamanElder)

## Tools for Information Extraction

### 1. Code Analysis Tools
- Use the `tools/py-tools/find_red_links.py` script to identify gaps in wiki coverage
- Generate code maps to understand relationships between game elements
- Use static analysis to identify all subclasses of important classes (e.g., all items inherit from Item)
- Note that the find_red_links.py script now identifies links to the new `en:rpd:` namespace rather than the old `rpd:` namespace

### 2. Class Hierarchy Analysis
- Identify all subclasses of base classes like `Item`, `Mob`, `Buff`, etc.
- Document the inheritance chain and unique properties of each subclass
- Extract common properties from base classes

### 3. Method Documentation
- Extract Javadoc-style comments from source methods
- Document parameters, return values, and behavior patterns
- Identify complex algorithms and game mechanics

## Best Practices for Wiki Improvement

### 1. Content Structure
- Follow the existing wiki format: `[[link|display text]]` for internal links
- Use consistent naming conventions for pages
- Maintain cross-links between related content

### 2. Information Accuracy
- Verify information by examining the source code
- Include version information when features change
- Document both intended behavior and known bugs

### 3. Comprehensive Coverage
- Document each property of items, mobs, and abilities
- Include statistics and numerical values
- Describe interactions between different systems

### 4. Maintenance
- Use the `tools/py-tools/find_red_links.py` script regularly to identify missing pages
- Update wiki content when game code is updated
- Create visual aids (like the generated wiki map) to show relationships

## Specific Areas for Improvement

### 1. Item Mechanics
- Extract damage ranges, stats, and special effects from weapon and armor classes
- Document all potion, scroll, and ring effects

### 2. Mob Behavior
- Document AI patterns and decision-making processes
- Include drop tables and spawn conditions
- Describe special mechanics and unique abilities

### 3. Game Mechanics
- Document combat calculations and formulas
- Explain level generation algorithms and special rooms
- Cover buff/debuff stacking rules and interactions
- Detail the hunger system and food mechanics
- Explain the talent system for heroes
- Document the difficulty system and scaling
- Detail the magic affinity system and spell mechanics
- Explain the quickslot system and item management
- Cover the enchantment and glyph mechanics for weapons and armor
- Detail the trap mechanics and identification system

### 4. Balance Information
- Extract numerical values for balancing purposes
- Document how scaling works with level progression
- Include information about probability and chance mechanics
- Detail the dungeon level depth mechanics (how stats scale with dungeon level)
- Explain mob stat scaling and special ability chances
- Document item drop rates and rarity calculations
- Cover the relationship between strength, weight and equipment penalties

## Useful Commands

### Finding Relevant Classes and Resources
```bash
# Find all item classes
find RemixedDungeon/src/main/java -name "*.java" -exec grep -l "extends Item" {} \;

# Find all mob classes
find RemixedDungeon/src/main/java -name "*.java" -exec grep -l "extends Mob" {} \;

# Find all buff classes
find RemixedDungeon/src/main/java -name "*.java" -exec grep -l "extends Buff" {} \;

# Find methods related to specific mechanics
grep -r "hunger" RemixedDungeon/src/main/java/

# Find resource strings
find RemixedDungeon/src/main/res -name "*.xml" -exec grep -l "string" {} \;

# Find JSON configuration files (220+ files across asset directories)
find RemixedDungeon/src/main/assets -name "*.json" -type f

# Find Lua script files (in scripts/ directory)
find RemixedDungeon/src/main/assets/scripts -name "*.lua" -type f

# Search inside JSON files for specific content (e.g., in levelObjects/)
grep -r "chest" RemixedDungeon/src/main/assets/levelObjects/

# Search inside Lua files for specific mechanics
grep -r "function" RemixedDungeon/src/main/assets/scripts/

# List all asset directories to understand the structure
ls -la RemixedDungeon/src/main/assets/

# Find specific JSON files by category
find RemixedDungeon/src/main/assets/levelObjects -name "*.json" -type f
find RemixedDungeon/src/main/assets/scripts -name "*.lua" -type f

# Find sprite images for visual references
find RemixedDungeon/src/main/assets -name "*.png" -type f | grep -i "mob\|item"

# Find specific configuration files
find RemixedDungeon/src/main/assets/hero -name "initHeroes.json" -type f
find RemixedDungeon/src/main/assets/levelsDesc -name "Bestiary.json" -type f
find RemixedDungeon/src/main/assets/mobsDesc -name "*.json" -type f
find RemixedDungeon/src/main/assets/spritesDesc -name "*.json" -type f
```

### Running Wiki Analysis Tools
```bash
# Generate wiki map
python3 tools/py-tools/find_red_links.py --output all
dot -Tpng fixed_wiki_map.dot -o wiki_map.png
```

## Quality Assurance

### Regular Maintenance
- Run `tools/py-tools/find_red_links.py` regularly to identify broken links
- Address any red links immediately by creating missing pages or correcting link targets
- Audit for duplicate content and merge as needed
- Regularly validate that all image references are properly formatted and exist
- Ensure all pages follow the naming conventions consistently
- Note that the find_red_links.py script now checks for links to the new `en:rpd:` namespace rather than the old `rpd:` namespace

### Content Verification
- Cross-reference all game information with actual game code and string resources
- When documenting mechanics, use information extracted directly from source code
- Update wiki content when game mechanics change in new versions
- Verify specific numeric values, formulas, and game mechanics against current code

### Review Process
- Before adding new content, verify that a page doesn't already exist under a different naming convention
- Ensure all new pages follow the lowercase naming standard
- Update related pages to link to new content appropriately
- Verify that all source code references point to actual files in the repository
- Check that all game mechanics are described accurately with reference to specific classes or config files

### Automated Checks
- Run the generation scripts periodically to check for inconsistencies
- Use `grep` to find all references to specific game elements and ensure consistency across wiki pages
- Check that all class-specific mechanics are documented (e.g., Doctor's accuracy penalty, Gnoll's forbidden actions, etc.)
- Verify that all hero subclasses and their special mechanics are correctly documented
- Ensure spell affinities match the actual magicAffinity values in code or initHeroes.json

## Additional Wiki Improvement Tools

The project includes additional scripts to help maintain and improve wiki quality that complement the existing tools:

### 1. Backlinks Tracking (Enhanced `find_red_links.py`)
- **Location**: `tools/py-tools/find_red_links.py` (enhanced with backlinks option)
- **Purpose**: Identifies which wiki pages link to each other, creating backlinks reports
- **Usage**: Run `python3 tools/py-tools/find_red_links.py --output backlinks` from the project root
- **Specific Page Usage**: Run `python3 tools/py-tools/find_red_links.py --output backlinks --page "page_name"` to see only backlinks to a specific page
- **Benefit**: Helps maintain navigation consistency by showing which pages link to each target page
- **Output**: Lists each page and the pages linking to it directly to console
- **Note**: The functionality has been integrated into the more comprehensive `find_red_links.py` tool which is the recommended approach.

### 2. Potential Link and Duplicate Detection (`wiki_potential_links_and_duplicates.py`)
- **Location**: `tools/py-tools/wiki_potential_links_and_duplicates.py`
- **Purpose**: Identifies pages that might want to link to the current page based on semantic similarity AND detects potential duplicate pages
- **Performance**: Optimized version runs ~14x faster than the original script (from ~12 minutes to ~50 seconds)
- **Usage**:
  - Analyze all pages for links and duplicates: `python3 tools/py-tools/wiki_potential_links_and_duplicates.py`
  - Analyze specific page: `python3 tools/py-tools/wiki_potential_links_and_duplicates.py --target-page start`
  - Analyze only for potential links: `python3 tools/py-tools/wiki_potential_links_and_duplicates.py --analysis links`
  - Analyze only for duplicates: `python3 tools/py-tools/wiki_potential_links_and_duplicates.py --analysis duplicates`
  - Analyze random pages: `python3 tools/py-tools/wiki_potential_links_and_duplicates.py --random-pages 5`
  - Custom threshold: `python3 tools/py-tools/wiki_potential_links_and_duplicates.py --threshold 0.5`
  - Custom duplicate threshold: `python3 tools/py-tools/wiki_potential_links_and_duplicates.py --dup-threshold 0.6`
  - Custom output: `python3 tools/py-tools/wiki_potential_links_and_duplicates.py --output-file my_report.txt`
- **Benefit**: Suggests potential internal links to improve wiki navigation and connectivity AND identifies potential duplicate content that could be merged or better organized
- **Output**: Comprehensive report with potential linking pages and potential duplicates, saved to `wiki_analysis_report.txt` by default
- **Features**:
  - **Potential links detection**: Identifies pages that might want to link to each other based on name and content similarity
  - **Duplicate detection**: Uses two methods to identify potential duplicates:
    - Content-based: Compares page content using Jaccard similarity to identify pages with similar content
    - Naming pattern: Identifies pages with similar names when normalized (e.g., "raise_dead" vs "raise_dead_spell")
  - Duplicate prevention: Prevents duplicate page names from appearing multiple times in results
  - Flexible analysis: Analyze links, duplicates, or both; analyze specific pages, random samples, or all pages
  - Performance optimized: Caching and efficient algorithms for fast processing
  - Configurable thresholds: Adjust similarity thresholds for different results

### 3. Redirect Page Detection (`wiki_redirects.py`)
- **Location**: `tools/py-tools/wiki_redirects.py`
- **Purpose**: Identifies redirect pages and potential redirects based on naming conventions
- **Usage**: Run `python3 tools/py-tools/wiki_redirects.py` from the project root
- **Benefit**: Finds potential redirects between pages with similar naming (e.g., `lich_mob` and `lich`)
- **Output**: Lists explicit redirects, duplicate content, and potential redirects, saved to `wiki_redirects_report.txt`

### 4. Automatic Redirect Page Fixer (`fix_wiki_redirects.py`)
- **Location**: `tools/py-tools/fix_wiki_redirects.py`
- **Purpose**: Detects redirect pages (e.g., warrior -> warrior_class), automatically fixes links in all wiki pages
- **Usage**:
  - Dry run: `python3 tools/py-tools/fix_wiki_redirects.py --dry-run`
  - Apply changes: `python3 tools/py-tools/fix_wiki_redirects.py`
  - With custom wiki directory: `python3 tools/py-tools/fix_wiki_redirects.py --wiki-dir /path/to/wiki`
- **Benefit**: Automatically resolves redirect patterns by updating links to follow the wiki's naming conventions
- **Features**:
  - Updates links in the format `[[namespace:old_name|Display Text]]` to `[[namespace:new_name|Display Text]]`
  - Updates links in the format `[[old_name|Display Text]]` to `[[new_name|Display Text]]`
  - Preserves display text in links
  - Handles multiple naming convention patterns (e.g., `warrior` → `warrior_class`, `shaman` → `shaman_mob`)
  - Prevents duplicate processing of the same redirect
  - Provides detailed output of changes made
  - Supports dry-run mode to preview changes before making them

### 5. Wiki Page Rename Tool (`wiki_page_rename.py`)
- **Location**: `tools/py-tools/wiki_page_rename.py`
- **Purpose**: Renames wiki pages and updates all internal links to the renamed page throughout the wiki
- **Usage**:
  - Basic rename: `python3 tools/py-tools/wiki_page_rename.py old_name new_name`
  - With custom wiki directory: `python3 tools/py-tools/wiki_page_rename.py old_name new_name --wiki-dir /path/to/wiki`
  - Dry run (no changes): `python3 tools/py-tools/wiki_page_rename.py old_name new_name --dry-run`
- **Benefit**: Ensures consistency when renaming pages by updating all links automatically
- **Features**:
  - Updates namespace links: `[[namespace:old_name|Display Text]]` -> `[[namespace:new_name|Display Text]]` (works with any namespace like `rpd:`, `wiki:`, `custom:`, etc.)
  - Updates regular links: `[[old_name|Display Text]]` -> `[[new_name|Display Text]]`
  - Updates links without display text: `[[namespace:old_name]]` and `[[old_name]]`
  - Uses word boundaries to avoid partial matches in longer page names (e.g., won't change `boss_old_name` when renaming `old_name`)
  - Dry run mode to preview changes before making them
  - Reports exactly which pages will be updated and how many links will be changed

### 6. Remove Redirect Pages (`remove_redirect_pages.py`)
- **Location**: `tools/py-tools/remove_redirect_pages.py`
- **Purpose**: Identifies and removes redirect pages after all links have been updated to follow the wiki's naming conventions
- **Usage**:
  - Dry run: `python3 tools/py-tools/remove_redirect_pages.py --dry-run`
  - Apply changes: `python3 tools/py-tools/remove_redirect_pages.py`
- **Benefit**: Cleans up the wiki by removing redundant redirect pages that have both base name and suffixed versions (e.g., removes `warrior.txt` when `warrior_class.txt` exists)
- **Features**:
  - Identifies pages that have both base name and suffixed versions (e.g., `warrior` and `warrior_class`)
  - Checks if the base name page contains redirect indicators in its content
  - Removes only the pages that are actual redirects, preserving pages that are not redirects
  - Provides detailed output of which pages are marked for removal
  - Supports dry-run mode to preview changes before making them
- **Safety**: Performs checks to ensure the old page exists and new page name doesn't conflict with existing pages

### 7. Missing mr: Namespace Pages Detection (`list_missing_mr_pages.py`)
- **Location**: `tools/py-tools/list_missing_mr_pages.py`
- **Purpose**: Identifies which mr: namespace pages are missing based on the actual entity lists in the `entities/` directory
- **Usage**: Run `python3 tools/py-tools/list_missing_mr_pages.py` from the project root
- **Benefit**: Helps maintain a complete set of machine-readable reference pages containing raw code facts, configuration excerpts, and source code references for all game entities
- **Output**: Comprehensive report with all missing mr: pages by entity type (mobs, items, buffs, spells) and a simple list for easy processing
- **Features**:
  - **Entity type detection**: Correctly identifies which pages are missing for each entity type (mobs, items, buffs, spells)
  - **Name normalization**: Converts Java/CamelCase entity names to appropriate snake_case formats for wiki pages (e.g., `Dreamweed.Seed` becomes `mr:dreamweed_seed_item`)
  - **Comprehensive listing**: Generates both categorized and flat lists of missing pages
  - **Future maintenance**: When mr: namespace pages are created, continues to track any additional missing pages as new entities are added to the game

### 8. mr: Link Validation (`check_mr_links.py`)
- **Location**: `tools/py-tools/check_mr_links.py`
- **Purpose**: Verifies that all file references in mr namespace pages point to actual existing files in the codebase
- **Usage**: Run `python3 tools/py-tools/check_mr_links.py` from the project root
- **Benefit**: Ensures that mr namespace pages contain accurate and up-to-date references to source code files
- **Features**:
  - **Local file checking**: Verifies that local file paths referenced in mr pages actually exist in the project
  - **Remote URL checking**: Verifies that GitHub URLs referenced in mr pages are accessible
  - **Comprehensive scanning**: Checks all links within all mr namespace pages (.txt files in wiki-data/pages/mr/)
  - **Detailed reporting**: Reports both valid and broken links, with a summary of the validation results
  - **Error status**: Returns error code 1 if broken links are found, 0 if all links are valid
  - **Verbose output**: Shows detailed status for each link being checked
- **Output**: Summary showing total valid/broken links, and detailed list of any broken links found

### 9. DokuWiki Linter (`dokuwiki_linter.py`)
- **Location**: `tools/py-tools/dokuwiki_linter.py`
- **Purpose**: Validates DokuWiki pages against the standards defined in WIKI_DOCUMENTATION.md for the Remixed Dungeon project
- **Usage**:
  - Lint a single file: `python3 tools/py-tools/dokuwiki_linter.py path/to/wiki_page.txt`
  - Lint a directory: `python3 tools/py-tools/dokuwiki_linter.py path/to/wiki/directory`
  - Fix trivial issues: `python3 tools/py-tools/dokuwiki_linter.py path/to/wiki_page.txt --fix`
  - Fix all files in a directory recursively: `python3 tools/py-tools/dokuwiki_linter.py path/to/wiki/directory --fix`
  - With JSON output: `python3 tools/py-tools/dokuwiki_linter.py path/to/wiki_page.txt --format json`
- **Benefit**: Ensures wiki pages follow proper formatting, naming conventions, and structural standards
- **Features**:
  - **Filename validation**: Checks that filenames are lowercase with underscores and use proper suffixes (_mob, _item, _spell, etc.)
  - **Header validation**: Verifies headers follow the format `====== Page Title ======`
  - **Link validation**: Checks internal links use lowercase names with proper namespace
  - **Image validation**: Validates image names follow lowercase convention and proper format
  - **Image existence check**: Verifies that referenced images actually exist in the wiki-data/media directory
  - **Tag validation**: Ensures tags follow the correct format `{{tag> ... }}`
  - **Paragraph validation**: Checks for proper paragraph breaks and formatting
  - **List validation**: Checks that list items are properly indented (with at least 2 spaces before * or -)
  - **Fix functionality**: Can automatically fix trivial issues like paragraph breaks
  - **Entity suffix validation**: Verifies proper suffixes are used for different entity types
- **Output**: Lists errors and warnings found in the wiki pages, with exit code 1 if errors are found

### 10. Language Link Consistency Checker (`check_language_links.py`)
- **Location**: `tools/py-tools/check_language_links.py`
- **Purpose**: Verifies that all internal links on a wiki page are in the same language as the page itself, with the exception of language links to the same page in other languages which should be placed at the bottom of the page
- **Usage**:
  - Check all pages: `python3 tools/py-tools/check_language_links.py`
  - Check specific language pages: `python3 tools/py-tools/check_language_links.py --language ru`
  - Check specific page: `python3 tools/py-tools/check_language_links.py --page ru:rpd:warrior_mob`
  - Check with verbose output: `python3 tools/py-tools/check_language_links.py --verbose`
- **Language Detection**: The script assumes `ru:` as Russian and treats the absence of a language code at the top level as English
- **Benefit**: Ensures language consistency across the wiki, preventing users from being unexpectedly redirected to content in a different language
- **Output**: Reports any language consistency violations found, including the page name, target link, line number, and violation type

These tools help automate wiki maintenance by identifying linking patterns and potential improvements that would be difficult to track manually. The enhanced `find_red_links.py` now provides multiple analysis functions in a single tool to reduce duplication.


## Tools and Automation

### Script Usage
- Use `tools/py-tools/find_red_links.py` to periodically scan for broken or incorrect links
- Use the merge script to handle any future duplicate files that may be created
- Use `pick_random_wiki_pages.sh` to randomly select wiki pages for review or editing
- Use `tools/py-tools/dokuwiki_linter.py` to validate wiki pages against documentation standards
- Implement automated checking in development workflow to catch naming convention violations

### Verification Steps
1. Before committing wiki changes, run `tools/py-tools/find_red_links.py --output red-links`
2. Verify all new links point to existing lowercase files
3. Ensure no capitalized files are being created
4. Check that merged content doesn't introduce duplicate information within pages

### Random Wiki Page Selection
- Use the `pick_random_wiki_pages.sh` script to randomly select wiki pages for review
- Run with optional argument for number of pages (defaults to 5): `./pick_random_wiki_pages.sh 3`
- Useful for identifying pages that need updates or quality checks
- Helps with rotating focus across the wiki content to ensure overall quality

## Exception and Special Cases

### External Links
- Links to external resources (HTTP/HTTPS) maintain their original format
- Links to special namespaces (wiki: pages, etc.) may follow different conventions

### Images and Assets
- Image references in wiki pages should maintain their original naming if they're external assets
- New images should follow same lowercase convention where possible

## Enforcement

### Responsibilities
- All wiki contributors must follow these naming conventions
- Code reviewers should verify wiki changes adhere to these standards
- Automated checks should be run as part of the build process where possible


## Verification of Wiki Documentation Against Code

### Platform and Market Separation Architecture
- **Documentation Coverage**: The documentation accurately describes the multi-module structure (Android, Desktop, Web), Gradle product flavors for Android market separation, source set organization for platform separation, and EventCollector as an example of market-specific implementations.
- **Code Verification**: The documentation is accurate - the EventCollector class exists with different implementations for different markets:
  - **googlePlay**: Full Firebase Analytics/Crashlytics implementation with actual logging and crash reporting
  - **fdroid**: Empty stub implementation with no tracking capabilities
  - **ruStore**: Minimal logging implementation with basic functionality
  - **Desktop**: Stub with basic logging and file output
  - **Web**: Console-based logging implementation
- **Additional Note**: The project also uses donation thresholds as alternative unlock mechanisms for some hero classes in addition to badge requirements

### UI Composition System
- **Documentation Coverage**: The documentation accurately describes the inheritance hierarchy from Gizmo to specific UI components, layout containers (VBox, HBox) with proper examples, interface-based design (IPlaceable), and UI patterns and best practices.
- **Code Verification**: UI component hierarchy matches documentation, with Button extending Component in com.watabou.noosa.ui package and RedButton extending TextButton as described.

### Overlay System
- **Documentation Coverage**: The documentation accurately describes source set hierarchy and precedence order, overlay patterns (Interface-based, Class Replacement, Stub Implementation), and build system integration with Gradle.
- **Code Verification**: Overlay system works as described with Android using Gradle flavors with source set precedence and Desktop using source sets from multiple directories.

### Hero Class and Subclass Information
- **Documentation Coverage**: The documentation now accurately reflects 9 hero classes (Warrior, Mage, Rogue, Huntress, Elf, Necromancer, Gnoll, Priest, Doctor) and 13 subclasses
- **Code Verification**: All starting equipment, stats, and special mechanics match the initHeroes.json configuration; subclass-specific armor (ClassArmor) is obtained by using the ArmorKit item (dropped by King boss) on any regular armor, not given as starting equipment
- **Additional Note**: Some classes (Doctor, Priest) have "Coming Soon" status and are not yet unlocked through gameplay

### Hero Class to Subclass Mapping
- **Mapping Location**: The official mapping between hero classes and their available subclasses is defined in `RemixedDungeon/src/main/java/com/watabou/pixeldungeon/windows/WndClass.java` in the MasteryTab class
- **Available Mappings**:
  - **Warrior**: Gladiator, Berserker
  - **Mage**: BattleMage, Warlock
  - **Rogue**: FreeRunner, Assassin
  - **Huntress**: Sniper, Warden
  - **Elf**: Scout, Shaman
  - **Necromancer**: Lich
  - **Gnoll**: Guardian, WitchDoctor
  - **Priest**: (no subclasses defined yet)
  - **Doctor**: (no subclasses defined yet)
- **Special Notes**:
  - The Tome of Mastery normally provides subclass choices for all classes except Necromancer and Gnoll
  - Special items like Tengu Liver provide subclass choices for classes normally excluded from Tome of Mastery (e.g., Tengu Liver for Gnoll class)
  - The game doesn't validate if a player selects an inappropriate subclass for their hero class (though it's unlikely to occur through normal gameplay)

### Custom Items Implementation
- **Documentation Coverage**: Custom items are implemented via Lua scripts in `scripts/items/` directory, not through JSON configuration as some wiki pages might suggest
- **Code Verification**: The `CustomItem.java` class serves as the base for all Lua-scripted items
  - Items are defined by Lua scripts that extend the item library via `require "scripts/lib/item"`
  - The `desc()` function in Lua returns item properties (image, name, info, price, etc.)
  - Item behaviors are handled through Lua API methods like `execute`, `attackProc`, `defenceProc`, etc.
- **Key Location**: `RemixedDungeon/src/main/java/com/nyrds/pixeldungeon/items/CustomItem.java`

### Custom Mobs Implementation
- **Documentation Coverage**: Custom mobs are implemented using JSON configuration files in `mobsDesc/` directory with optional Lua behavior scripts
- **Code Verification**: The `CustomMob.java` class handles JSON-defined mob properties
  - JSON files define mob stats like HP, damage, speed, etc.
  - Lua scripts provide custom behaviors via the mob library `require "scripts/lib/mob"`
  - The `scriptFile` property in JSON links to Lua scripts in `scripts/mobs/` directory
- **Key Location**: `RemixedDungeon/src/main/java/com/nyrds/pixeldungeon/mobs/common/CustomMob.java`

### Key Code References for Wiki Maintenance
When maintaining wiki pages, these are important code locations to reference for accuracy:

#### Core Game Classes
- **Hero System**: `RemixedDungeon/src/main/java/com/watabou/pixeldungeon/actors/hero/`
  - `Hero.java` - Main hero character logic
  - `HeroClass.java` - Available hero classes and their characteristics
  - `HeroSubClass.java` - Hero mastery/subclass definitions
- **Items**: `RemixedDungeon/src/main/java/com/watabou/pixeldungeon/items/`
  - `Item.java` - Base item class
  - `Weapon.java`, `Armor.java` - Equipment base classes
  - `Potion.java`, `Scroll.java`, `Ring.java` - Consumable item classes
- **Mobs**: `RemixedDungeon/src/main/java/com/watabou/pixeldungeon/actors/mobs/`
  - `Mob.java` - Base mob class with common properties
  - Individual mob classes (e.g., `Skeleton.java`, `Thief.java`)
- **Spells**: `RemixedDungeon/src/main/java/com/nyrds/pixeldungeon/mechanics/spells/`
  - Spell definitions with magic affinity, targeting, etc.
- **Buffs/Debuffs**: `RemixedDungeon/src/main/java/com/watabou/pixeldungeon/buffs/`
  - Status effects with detailed mechanics
- **Game Mechanics**: `RemixedDungeon/src/main/java/com/watabou/pixeldungeon/mechanics/`
  - Combat, damage calculations, special mechanics

#### Configuration Files
- **Hero Starting Stats**: `RemixedDungeon/src/main/assets/hero/initHeroes.json`
  - Starting equipment, stats, and special properties for each hero class
- **Mob Spawning**: `RemixedDungeon/src/main/assets/levelsDesc/Bestiary.json`
  - Defines which monsters appear on which levels and their spawn weights/rates
- **Sprites**: `RemixedDungeon/src/main/assets/spritesDesc/`
  - Sprite animation and effect configurations
- **Level Objects**: `RemixedDungeon/src/main/assets/levelObjects/`
  - Interactive objects configuration (chests, statues, barricades, etc.)

#### String Resources
- **Game Text**: `RemixedDungeon/src/main/res/values/strings-all.xml`
  - Main string resources with game text, descriptions, and labels

#### Lua Scripts
- **Item Scripts**: `RemixedDungeon/src/main/assets/scripts/items/`
- **Mob Scripts**: `RemixedDungeon/src/main/assets/scripts/mobs/`
- **Spell Scripts**: `RemixedDungeon/src/main/assets/scripts/spells/`
- **Library Scripts**: `RemixedDungeon/src/main/assets/scripts/lib/`

## Maintaining Consistency

- Keep the wiki updated with each game release
- Create new pages for new content in each update
- Use the visualization tools to identify disconnected parts of the wiki
- Encourage community contributions by providing clear documentation on where information can be found
- Maintain accurate source code references for all spell information
- Use the automated generation tools to keep spell information current
- Extract and update item sprites when new items are added
- Use consistent naming conventions for all extracted sprites
- Include detailed class-specific mechanics (forbidden actions, friendly mobs, immunities, etc.)
- Ensure all wiki pages have appropriately sized and positioned images

## Image Generation and Scaling Guidelines

Sprite generation and scaling steps should only be executed when images are definitely missing from wiki-data/media, to avoid unnecessary processing:

### When to Generate and Scale Sprites
- Only when adding new wiki pages that require sprites not already present in `wiki-data/media/rpd/images/`
- When new game content (mobs, items, spells, etc.) has been added to the game that needs wiki documentation
- Run the following commands only when you confirm specific sprites are missing:
  - Generate raw sprites: `./gradlew -c settings.desktop.gradle :RemixedDungeonDesktop:generateSpritesFromFactories`
  - Scale sprites for wiki: `python3 tools/py-tools/scale_sprites_for_wiki.py -i sprites/ -o wiki-data/media/rpd/images/`

### Image Naming Conventions
- The sprite generation process creates files in the format: `mob_[Name].png`, `item_[Name].png`, etc.
- The scaling process maintains the same names but enhances them: `mob_[Name].png` remains `mob_[Name].png` but with scaling, background, and frame
- Use the appropriate naming based on the entity type in your wiki pages as described in the "Wiki Integration Naming" section above

## mr: Namespace - AI Reference Pages

### Purpose
The mr: namespace (standing for "machine-readable" or "metadata reference") is a special namespace that contains only raw facts, code fragments, configuration excerpts, and string resource references related to specific game entities. These pages are designed for AI and automated tools to simplify the creation of accurate human-targeted wiki pages.

### Content Structure
Pages in the mr: namespace contain only:
- Direct references to relevant Java classes
- Excerpts from JSON configuration files
- Relevant string resource entries
- Code snippets showing implementation details
- Numerical values and game mechanics as they appear in code
- Links to source files on GitHub
- References to entity lists found in the `entities/` directory (generated by FactorySpriteGenerator)
- Links to related mr namespace pages for connected entities
- No human-written descriptions, explanations, or prose content

### Format
mr: namespace pages should follow this structure:

====== Entity Name - Code References ======

===== Java Classes =====
  * [[https://github.com/NYRDS/remixed-dungeon/blob/master/RemixedDungeon/src/main/java/com/watabou/pixeldungeon/actors/mobs/Skeleton.java|Skeleton.java]]
  * [[https://github.com/NYRDS/remixed-dungeon/blob/master/RemixedDungeon/src/main/java/com/watabou/pixeldungeon/sprites/SkeletonSprite.java|SkeletonSprite.java]]

===== JSON Configuration =====
  * Include JSON configuration if the entity is implemented via JSON; otherwise, state: "This entity is implemented in Java, no JSON configuration exists"
  * For JSON configuration, include relevant fields like entityKind, HP, damage, accuracy, evasion, loot, lootChance, etc. as appropriate for the entity type
  * Use DokuWiki code blocks for JSON: ''<code json>'' and ''</code>''

===== String Resources =====
  * Include actual string resources from the values/strings_all.xml file for this entity
  * Include names, descriptions, and other relevant strings
  * Use DokuWiki code blocks for XML: ''<code xml>'' and ''</code>''

===== Lua Scripts =====
  * Include Lua script reference if the entity uses Lua scripts; otherwise, state: "This entity is implemented in Java, no Lua script exists"

===== Related mr Entities =====
  * Include links to related mr namespace pages that connect to this entity (e.g., subclass linked to parent class, item linked to related spell, mob linked to related quest, etc.)
  * Format: [[mr:entity_name_type|Related Entity Name]]

### Human-Readable Page Links
- When creating human-readable wiki pages in the en:rpd: namespace, link to the corresponding mr: namespace page for technical details
- Format: [[mr:entity_name_type|Technical Details]]

Example with existing configuration:
====== Example Entity - Code References ======

===== Java Classes =====
  * [[https://github.com/NYRDS/remixed-dungeon/blob/master/RemixedDungeon/src/main/java/com/example/ExampleEntity.java|ExampleEntity.java]]

===== JSON Configuration =====
''<code json>''
{
  "entityKind": "ExampleEntity",
  "HP": "20",
  "damage": "5-10"
}
''</code>''

===== String Resources =====
''<code xml>''
<string name="example_entity_name">Example Entity</string>
<string name="example_entity_desc">An example entity for demonstration.</string>
''</code>''

===== Lua Scripts =====
  * [[https://github.com/NYRDS/remixed-dungeon/blob/master/RemixedDungeon/src/main/assets/scripts/exampleEntity.lua|exampleEntity.lua]]

===== Related mr Entities =====
  * [[mr:parent_entity_mob|Parent Entity (Mob)]]
  * [[mr:related_item_item|Related Item (Item)]]

### Content Rules
- **Code fragments only**: mr pages should contain only direct excerpts from Java, Lua, JSON configuration files, and string resources related to the described entity
- **No transformations**: Converting code from one format to another (e.g., converting Java or Lua code to JSON configuration format) is not allowed - only include the actual code as it exists in the source files
- **Accurate representation**: It is acceptable to have missing information that will be added later, but it is unacceptable to include incorrect or hallucinated information
- **Empty sections**: It is acceptable to have empty sections if a particular type of resource (e.g., Lua script, JSON config) does not exist for the entity
- **Selective referencing**: Not all entities need to have references in all possible code sources (Java, Lua, JSON, string resources) - include only what actually exists for each specific entity

### Verification Requirements
All pages in the mr namespace must be regularly verified against:
- The actual Java code in the repository
- The current JSON configuration files
- The relevant string resource files
- Any Lua script implementations
- Any other configuration that affects the entity behavior
- The actual entities list, which can be found in the `entities/` directory (generated by the FactorySpriteGenerator task)

### Usage Instructions
- When creating human-targeted wiki pages, authors should reference the corresponding mr namespace page for accurate technical information
- AI tools should use mr namespace pages as a primary source of factual data
- All information in mr pages should be verified as current and accurate before using for human-facing content
- When the game updates, mr namespace pages should be checked first to identify how the raw data has changed

## Recommended Wiki Maintenance Workflow

When updating and maintaining wiki pages, follow this systematic workflow to ensure accuracy and consistency:

### 1. Preparation
- Pull the latest changes from the repository: `git pull origin master`
- Read the latest changes in the codebase to understand recent updates
- Review the current WIKI_DOCUMENTATION.md to refresh yourself on standards
- Check the mr namespace pages for the entities you're updating to ensure you have accurate, current information
- Note: Avoid performing unused image cleanup as a standard part of wiki improvement, as this may remove images that are legitimately in use

### 2. Page Selection
- Use the `pick_random_wiki_pages.sh` script to randomly select wiki pages for review: `./pick_random_wiki_pages.sh [number_of_pages]`
- Alternatively, select pages that correspond to recently updated game features

### 3. Verification Against Code
- For each selected wiki page, locate the corresponding game entity in the codebase:
  - Java classes in `RemixedDungeon/src/main/java/`
  - Lua scripts in `RemixedDungeon/src/main/assets/scripts/`
  - JSON configuration files in `RemixedDungeon/src/main/assets/` (specifically `mobsDesc/`, `spritesDesc/`, `levelObjects/`, etc.)
  - String resources in `RemixedDungeon/src/main/res/values/strings_all.xml`
- Verify all game mechanics, stats, and properties described in the wiki against the actual implementation
- Identify any discrepancies or outdated information
- Check for completely incorrect information (like items that don't exist in the code)
- When verifying entity behavior, don't only check the entity definition - also check entity usages and mentions in Java, Lua code, and JSON configurations to understand the complete behavior and context

### 4. Wiki Improvement
- Remove any incorrect information found during verification
- Add missing information based on code analysis
- Wikify content by adding appropriate internal links in the format `[[en:rpd:page_name|Display Text]]`
- Check and update backlinks to maintain navigation consistency
- Add or update source code references to GitHub files
- Include specific numerical values and mechanics as documented in the source code
- Follow the established formatting and style guidelines

### 5. Link Validation
- Use the automated script `check_mr_links.py` to verify that all file references in mr namespace pages point to actual existing files:
  - Run from project root: `python3 tools/py-tools/check_mr_links.py`
  - The script checks both local file paths and GitHub URLs mentioned in mr namespace pages
  - It reports broken links that need to be fixed
  - The script will return an error code if broken links are found

### 6. Commit and Push Changes
- Review changes to wiki pages for accuracy and completeness
- Commit changes to the wiki-data submodule with descriptive commit messages
- Push the updated wiki pages to the remote repository
- Verify that all changes are properly reflected on the wiki

### 7. Ongoing Maintenance
- Regularly repeat this workflow to maintain accuracy over time
- Use automated tools like `find_red_links.py` to identify broken links
- Update the documentation itself when new patterns or procedures emerge
- Keep the workflow updated based on lessons learned during maintenance