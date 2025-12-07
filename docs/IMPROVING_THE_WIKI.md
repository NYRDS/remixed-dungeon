# Improving the Remixed Dungeon Wiki

This document provides guidance on how to enhance and maintain the Remixed Dungeon wiki by leveraging information sources within the game code.

## Overview

The Remixed Dungeon wiki (located in the `wiki-data` submodule) can be significantly improved by extracting information directly from the game's source code. This document outlines the various sources of information available and how to use them to enhance wiki content.

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

## Useful Commands

### Finding Relevant Classes
```bash
# Find all item classes
find RemixedDungeon/src/main/java -name "*.java" -exec grep -l "extends Item" {} \;

# Find all mob classes  
# Find all buff classes
find RemixedDungeon/src/main/java -name "*.java" -exec grep -l "extends Buff" {} \;

# Find methods related to specific mechanics
grep -r "hunger" RemixedDungeon/src/main/java/
```

### Running Wiki Analysis Tools
```bash
# Generate wiki map
python find_red_links.py --output all
dot -Tpng fixed_wiki_map.dot -o wiki_map.png
```

## Maintaining Consistency

- Keep the wiki updated with each game release
- Create new pages for new content in each update
- Use the visualization tools to identify disconnected parts of the wiki
- Encourage community contributions by providing clear documentation on where information can be found