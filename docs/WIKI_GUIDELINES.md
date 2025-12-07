# Remixed Dungeon Wiki Guidelines: Improving and Maintaining the Wiki

This comprehensive document provides guidance on how to enhance and maintain the Remixed Dungeon wiki by leveraging information sources within the game code, while following established maintenance rules and best practices.

## Overview

The Remixed Dungeon wiki (located in the `wiki-data` submodule) can be significantly improved by extracting information directly from the game's source code. This document outlines the various sources of information available, how to use them to enhance wiki content, and the rules for maintaining consistency and quality.

## Key Information Sources in Game Code

### 1. Core Game Classes (RemixedDungeon/src/main/java/)

#### Hero Classes and Mechanics
- **Location**: `com/watabou/pixeldungeon/actors/hero/`
- **Key files**:
  - `Hero.java` - Main hero character logic, attributes, and capabilities
  - `HeroClass.java` - Available hero classes and their characteristics
  - `HeroAction.java` - Hero actions and behaviors
  - `Belongings.java` - Hero inventory and equipment system
  - `Talent.java` - Hero talents and progression system

#### Enemies/Mobs (Monsters)
- **Location**: `com/watabou/pixeldungeon/actors/mobs/`
- **Key files**:
  - `Mob.java` - Base mob class with common properties
  - `Skeleton.java`, `Thief.java`, `Gnoll.java`, etc. - Individual enemy implementations
  - `Boss.java` - Boss mob definitions and mechanics
  - `Elemental.java`, `Demon.java` - Enemy subtypes with specific mechanics

#### Items and Equipment
- **Location**: `com/watabou/pixeldungeon/items/`
- **Key files**:
  - `Item.java` - Base item class
  - `Weapon.java`, `Armor.java` - Equipment base classes
  - `Potion.java`, `Scroll.java`, `Ring.java` - Consumable items
  - `weapon/melee/`, `weapon/magic/` - Specific weapon types
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
  - `BlastWave.java` - Blast mechanics
  - `Chasm.java` - Chasm falling mechanics
  - `Element.java` - Damage element types and resistances

#### Buffs and Debuffs
- **Location**: `com/watabou/pixeldungeon/buffs/`
- **Key files**:
  - `Buff.java` - Base buff class
  - `Bleeding.java`, `Burning.java`, `Poison.java` - Status effects with detailed mechanics
  - `Hunger.java` - Hunger system
  - `Invisibility.java` - Invisibility mechanics
  - `Regeneration.java` - Health regeneration

### 2. Sprites and Visual Assets (RemixedDungeon/src/main/java/)

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

### 3. UI and Screens (RemixedDungeon/src/main/java/)

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

### 4. Modding Support (RemixedDungeon/src/main/java/)

#### Custom Content Creation
- **Location**: `com/nyrds/mod/`
- **Key files**:
  - `ModData.java` - Mod data structure
  - `ModManager.java` - Mod loading and management
  - `Custom*` classes - Custom content implementations

#### Content Generation
- **Location**: `com/nyrds/pixeldungeon/items/`, `com/nyrds/pixeldungeon/mobs/`
- **Key files**:
  - Various custom item and mob implementations

### 5. Game Configuration and Data

#### Localized Strings
- **Location**: `RemixedDungeon/src/main/assets/l10ns/`
- **Files**: `strings_*.json` - Localized game text that can be used for game descriptions

#### Item and Mob Configuration
- **Location**: `RemixedDungeon/src/main/assets/data/`
- **Files**: Various JSON files containing game configuration data

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

## File Naming Convention

### Standard Format
- All wiki page files must use lowercase names with underscores separating words
- Example: `chaos_armor.txt`, `air_elemental.txt`, `potion_of_healing.txt`
- No capitalized filenames are allowed (e.g., no `Chaos_Armor.txt`, `Air_Elemental.txt`)

### Conversion Strategy
- CamelCase names should be converted to snake_case (e.g., `TheSoulbringer` → `the_soulbringer`)
- Proper names should follow the same rule (e.g., `ArcaneStylus` → `arcane_stylus`)

## Content Organization

### Entity Namespace Separation
To avoid confusion between similar entity names (e.g., a mob and hero subclass both named "Shaman"),
different entity types should use different naming conventions:

#### Mobs (Monsters/Enemies)
- Pages for dungeon mobs and enemies should use `_mob` suffix
- Example: `shaman_mob.txt`, `lich_mob.txt`, `gnoll_mob.txt`
- Current exceptions like `lich_mob.txt` and `crystal_mob.txt` should be maintained

#### Hero Classes
- Pages for main playable classes use simple names
- Example: `warrior.txt`, `mage.txt`, `rogue.txt`, `elf.txt`

#### Hero Subclasses
- Pages for mastery paths or subclasses can use simple names within context
- Example: `shaman.txt` (Shaman subclass for Elf class), `scout.txt` (Scout subclass for Elf class)

#### Items
- All items follow the standard format without special suffixes
- Example: `wooden_bow.txt`, `potion_of_healing.txt`, `ring_of_frost.txt`

#### Spells
- All spells use `_spell` suffix to distinguish from items with similar names
- Example: `healing_spell.txt`, `ignite_spell.txt`, `wind_gust_spell.txt`, `magic_torch_spell.txt`
- When an item and spell share similar names (e.g., a healing potion and healing spell), use the suffix to differentiate

#### NPCs
- Non-player characters follow standard format
- Example: `shopkeeper.txt`, `wandmaker.txt`, `ghost.txt`

#### Other Entities
- Levels, mechanics, and other game elements follow standard format
- Example: `sewers.txt`, `combat.txt`, `mechanics.txt`

### Single Source of Truth
- Each wiki page should have exactly one file in the correct lowercase naming format
- No duplicate content should exist in separate files
- When merging content from duplicate files, combine all information in the lowercase-named file

### Content Structure
- Use consistent heading formats: `====== Page Title ======`
- Organize content with clear sections using `==== Section Title ====`
- Include relevant tags at the bottom: `{{tag> rpd items}}` or `{{tag> rpd mobs}}`

## Image Standards

### Adding Mob/Character Sprites
- To add a mob sprite to a wiki page, use the centered format with original size: `{{ rpd:images:mob_name_sprite.png|Alt Text }}`
- The script `tools/py-tools/extract_mob_sprites.py` automatically generates scaled 8x sprites with nearest-neighbor filtering
- Generated images are saved in `wiki-data/media/rpd/images/` with the naming convention `{mob_name}_sprite.png`
- For centered display, add spaces around the image reference: `{{ rpd:images:image.png|Alt Text }}`
- For original image size (no scaling), omit the size parameter entirely

### General Image Guidelines
- Place images at the beginning of the page for visual identification
- Use descriptive alt text when possible
- Store all new images in the appropriate subdirectory within `wiki-data/media/rpd/images/`
- Maintain consistent image sizing across similar page types
- For non-sprite images, use descriptive filenames in snake_case format

## Content Quality Standards

### Handling Naming Conflicts
When creating pages for entities that share names between different types (e.g., a mob and hero subclass),
follow these rules to prevent confusion:

#### Example: Shaman Conflict
- **Shaman (Mob)**: Create page as `shaman_mob.txt` for the dungeon enemy (typically a gnoll shaman)
- **Shaman (Hero Subclass)**: Keep page as `shaman.txt` for the Elf class mastery path
- **Image references**: Use correct sprite for each entity type
  - Mobs use `_sprite.png` images (e.g., `shaman_sprite.png`)
  - Hero subclasses are documented on the main hero class page with appropriate text description

#### Example: Item/Spell Conflicts
- When items and spells have similar names that could cause confusion, use descriptive names:
  - For spells: Use the `_spell` suffix when needed to differentiate (e.g., if both `healing.txt` item and `healing.txt` spell existed, they could be `healing_item.txt` and `healing_spell.txt`)
  - For items: Use descriptive prefixes when needed (e.g., `potion_of_healing.txt`, `scroll_of_identify.txt`)
- In cases where item and spell names conflict, create separate pages with distinguishing suffixes to avoid confusion
- Currently, spells like `healing_spell.txt` and `root_spell.txt` use the suffix to distinguish them from potential related items

#### Best Practices for Entity Images
- Mobs and enemies should use their sprite images
- Hero classes and subclasses should use appropriate class-specific images or be documented with text descriptions
- Items should use their sprite images when available
- Spells may use icon images or be documented with text descriptions
- When both entity types require the same name, prioritize the most common usage or add suffixes to distinguish

### Information Accuracy
- All game data (stats, mechanics, effects, drop rates) must be verified against source code
- Include specific numerical values rather than vague descriptions
- When documenting items, include durability, usage restrictions, and exact effects
- For mobs, document HP, damage, special abilities, resistances, and drop tables

### Comprehensive Coverage
- Describe both the mechanical effects and strategic implications of game elements
- Include information about where items/mobs can be found or obtained
- Explain synergies with other items or mechanics when relevant
- Document any special behaviors, AI patterns, or unique mechanics

### Formatting and Style
- Use bullet points for lists of properties, effects, or characteristics
- Include code-style references when mentioning other game elements (e.g., `[[rpd:sword|Sword]]`)
- Present information in order of importance to the player
- Provide examples where helpful for understanding complex mechanics

### Technical Information
- When documenting mechanics from source code, include the relevant class names
- Reference game constants and formulas where they enhance understanding
- Cite string resource names for accuracy of in-game text
- Link to related mechanics or concepts for better context

## Internal Linking Standards

### Link Format
- All internal links must use lowercase page names in the format: `[[rpd:page_name|Display Text]]`
- For same-namespace links, use: `[[page_name|Display Text]]`
- Avoid linking to capitalized file names that no longer exist

### Cross-references
- When linking to related pages, ensure the target page exists in lowercase format
- Use descriptive display text that clarifies the link's purpose
- Maintain consistent terminology across linked pages

## Tools for Information Extraction

### 1. Code Analysis Tools
- Use the `find_red_links.py` script to identify gaps in wiki coverage
- Generate code maps to understand relationships between game elements
- Use static analysis to identify all subclasses of important classes (e.g., all items inherit from Item)

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
- Use the `find_red_links.py` script regularly to identify missing pages
- Update wiki content when game code is updated
- Create visual aids (like the generated wiki map) to show relationships

## Specific Areas for Improvement

### 1. Item Mechanics
- Extract damage ranges, durability, and special effects from weapon and armor classes
- Document all potion, scroll, and ring effects
- Include information about item synergies and combinations

### 2. Mob Behavior
- Document AI patterns and decision-making processes
- Include drop tables and spawn conditions
- Describe special mechanics and unique abilities

### 3. Game Mechanics
- Document combat calculations and formulas
- Explain level generation algorithms and special rooms
- Cover buff/debuff stacking rules and interactions

### 4. Balance Information
- Extract numerical values for balancing purposes
- Document how scaling works with level progression
- Include information about probability and chance mechanics

## Quality Assurance

### Regular Maintenance
- Run `find_red_links.py` regularly to identify broken links
- Address any red links immediately by creating missing pages or correcting link targets
- Audit for duplicate content and merge as needed

### Content Verification
- Cross-reference all game information with actual game code and string resources
- When documenting mechanics, use information extracted directly from source code
- Update wiki content when game mechanics change in new versions

### Review Process
- Before adding new content, verify that a page doesn't already exist under a different naming convention
- Ensure all new pages follow the lowercase naming standard
- Update related pages to link to new content appropriately

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
```

### Running Wiki Analysis Tools
```bash
# Generate wiki map
python find_red_links.py --output all
dot -Tpng fixed_wiki_map.dot -o wiki_map.png
```

## Migration Guidelines

### For Existing Content
- All capitalized files were merged into their lowercase counterparts during the migration
- Any remaining capitalized files should be removed after confirming content was properly merged
- Links pointing to capitalized file names have been updated to lowercase equivalents

### For Naming Conflicts
- Existing pages with ambiguous names should be evaluated for potential confusion
- If a page exists for a hero subclass or class but shows mob sprite (like `shaman.txt`), consider:
  - Creating separate page for the mob (e.g., `shaman_mob.txt`)
  - Updating existing page to focus on hero class/subclass content
  - Updating links to point to the appropriate page for each context

### For New Content
- Create all new pages using the lowercase naming convention
- When creating content that references existing pages, use the correct lowercase link format
- Follow the entity namespace separation guidelines to avoid future conflicts
- If you find old capitalized links still in code or documentation, update them to lowercase format

## Tools and Automation

### Script Usage
- Use `find_red_links.py` to periodically scan for broken or incorrect links
- Use the merge script to handle any future duplicate files that may be created
- Implement automated checking in development workflow to catch naming convention violations

### Verification Steps
1. Before committing wiki changes, run `find_red_links.py --output red-links`
2. Verify all new links point to existing lowercase files
3. Ensure no capitalized files are being created
4. Check that merged content doesn't introduce duplicate information within pages

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

### Migration Tracking
- A record of all merged files and their new locations is maintained for historical reference
- Links from outside sources should be updated to reflect the new naming convention

## Additional Sources of Truth Discovered

### Class Unlock Requirements
- **Location**: `RemixedDungeon/src/main/java/com/watabou/pixeldungeon/scenes/StartScene.java`
- **Key Information**:
  - `huntressUnlocked = Badges.isUnlocked(Badges.Badge.BOSS_SLAIN_3)` - Huntress unlocked by defeating 3rd boss (DM300)
  - `elfUnlocked = Badges.isUnlocked(Badges.Badge.BOSS_SLAIN_4)` - Elf unlocked by defeating 4th boss (The King)
  - `gnollUnlocked = Badges.isUnlocked(Badges.Badge.GNOLL_UNLOCKED)` - Gnoll unlocked by healing Gnoll Brute at town priest
  - Boss unlock badges are validated in `RemixedDungeon/src/main/java/com/watabou/pixeldungeon/Badges.java`

### Monster Spawning and Level Configuration
- **Location**: `RemixedDungeon/src/main/assets/levelsDesc/Bestiary.json`
- **Key Information**: Defines which monsters appear on which levels and their spawn weights/rates
  - Example: `"CavesLevel": {"11":{"Bat":1,"Brute":0.2}}` - Shows Gnoll Brutes spawn in Caves levels 11-14
  - `"SewerBossLevel": {"any":{"Goo":1}}` - Goo is the sewer boss
  - `"PrisonBossLevel": {"any":{"Tengu":1}}` - Tengu is the prison boss
  - `"CavesBossLevel": {"any":{"DM300":1}}` - DM300 is the caves boss, etc.

### Class Unlock Validation
- **Location**: `RemixedDungeon/src/main/java/com/nyrds/pixeldungeon/windows/WndPriest.java`
- **Key Information**:
  - `Badges.validateGnollUnlocked()` is called when healing a Gnoll Brute NPC
  - Brute healing mechanism: "if(patient.getEntityKind().equals("Brute")) { Badges.validateGnollUnlocked(); }"

### Class-Specific Perks and Mechanics
- **Location**: `RemixedDungeon/src/main/java/com/watabou/pixeldungeon/actors/hero/HeroClass.java`
- **Key Information**:
  - Each class has unique mechanics in methods like `attackSkillBonus()`, `allowed()`, etc.
  - The `allowed()` method checks `initHeroes.has(name())` to determine if class is available
  - Special mechanics like Doctor's accuracy penalty when not using Bone Saw

## Hero Class and Subclass Previews

To maintain consistency and completeness in documentation of hero classes and subclasses, a script has been created to generate preview content for wiki pages.

### Hero Class Previews
- All hero classes (Warrior, Mage, Rogue, Huntress, Elf, Necromancer, Gnoll, Priest, Doctor) have dedicated wiki pages
- Use the `tools/py-tools/generate_hero_previews.py` script to extract information from source code and generate preview content
- Each hero class page should include:
  - Description of the class's unique characteristics
  - Starting equipment and stats
  - Special abilities and mechanics
  - Sprite information where applicable

### Hero Subclass Previews
- All hero subclasses (Gladiator, Berserker, Warlock, BattleMage, Assassin, FreeRunner, Sniper, Warden, Scout, Shaman, Lich, WitchDoctor, Guardian) have dedicated wiki pages
- Subclasses are mastery paths available for specific hero classes
- Each subclass page should include:
  - Description of the subclass's unique abilities
  - Which base class it belongs to
  - How to unlock the subclass (typically via Tome of Mastery)
  - Special mechanics and gameplay changes

### Generating Previews
The `tools/py-tools/generate_hero_previews.py` script extracts information by:
- Parsing `HeroClass.java` and `HeroSubClass.java` enums to identify all classes/subclasses
- Reading `initHeroes.json` to get starting equipment and stats for classes
- Using predefined descriptions for subclasses based on in-game mechanics

To generate previews:
```bash
python tools/py-tools/generate_hero_previews.py
```

This creates preview files in the `hero_previews/` directory that can be used as templates for wiki pages.

## Spell Documentation and Automated Generation

### Spell Wiki Pages Structure

Spell documentation in the wiki follows the same general structure as other entity pages but includes specific sections relevant to spell mechanics:

#### Standard Spell Page Sections
- **Title**: Using the `_spell` suffix (e.g., `healing_spell.txt`, `ignite_spell.txt`)
- **Image**: Spell icon at the top of the page using `{{ rpd:images:spell_name_icon.png|Spell Name Icon }}`
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

The `tools/py-tools/generate_spell_wiki.py` script automates the creation of spell documentation from source code:

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

#### Image Generation
The `tools/py-tools/generate_spell_images.py` script automatically creates preview images for spells by:
- Mapping spell classes to appropriate icon files from `spellsIcons/` directory
- Resizing images to appropriate dimensions for wiki display
- Naming images according to the pattern `spell_name_icon.png`

### Generating Spell Documentation

To generate or update spell documentation:
```bash
python tools/py-tools/generate_spell_wiki.py
python tools/py-tools/generate_spell_images.py
```

This creates or updates spell pages in the `generated_spell_wiki/` directory and spell icon images in the `generated_spell_images/wiki_images/` directory.

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

### Item Sprite Extraction for Wiki

#### Automatic Extraction Process
The project includes Python scripts in the `tools/py-tools/` directory for extracting item sprites:

- `extract_item_sprites.py` - Extracts from the main items.png sheet
- `extract_all_item_sprites.py` - Extracts from multiple specialized sheets
- `extract_custom_item_sprites.py` - Extracts items with special configurations
- `extract_mob_sprites.py` - Extracts mob sprites with 8x scaling

#### Extraction Workflow
1. Parse Java files to identify item classes and their image indices
2. Locate the appropriate sprite sheet based on the image index
3. Extract the 16x16 pixel sprite from the sheet at the calculated position
4. Scale the sprite using nearest-neighbor interpolation (typically 8x for wiki)
5. Save the individual sprite in the wiki media directory
6. Update wiki pages to include the extracted sprite reference

#### Naming Convention
- Extracted sprites follow the format: `{itemname}_sprite.png`
- In wiki pages, use: `{{ rpd:images:ITEMNAME_sprite.png|Alt Text }}`
- For centering in DokuWiki: `{{ rpd:images:image.png|Alt Text }}` (with spaces)

#### Image Quality Settings
- Use nearest-neighbor scaling to preserve pixel art quality
- Scale factor of 8x for optimal visibility in wiki pages
- Maintain square aspect ratio (16x16 original becomes 128x128)

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

## Maintaining Consistency

- Keep the wiki updated with each game release
- Create new pages for new content in each update
- Use the visualization tools to identify disconnected parts of the wiki
- Update spell documentation when game mechanics change
- Maintain accurate source code references for all spell information
- Use the automated generation tools to keep spell information current
- Extract and update item sprites when new items are added
- Use consistent naming conventions for all extracted sprites
- Ensure all wiki pages have appropriately sized and positioned images
- Encourage community contributions by providing clear documentation on where information can be found