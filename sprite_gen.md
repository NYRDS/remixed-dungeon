# Sprite Generation System for Remixed Dungeon

## Overview
The sprite generation system has been implemented to automatically create PNG image files for all mobs and items in the Remixed Dungeon game. This system provides a way to generate visual representations of game entities by extracting actual texture data instead of generating colored placeholders.

## Implementation Details

### 1. Texture Management Changes
#### 1.1 Static Flag for Bitmap Disposal Control
Three platform-specific Texture classes (Android, Desktop, HTML) were updated with a static flag to control bitmap data disposal:
- `autoDisposeBitmapData` static flag controls whether bitmap data is automatically disposed after texture upload
- Methods `setAutoDisposeBitmapData()` and `getAutoDisposeBitmapData()` added to control the flag globally
- Default behavior remains to dispose of bitmap data after upload (preserving original behavior in most cases)

#### 1.2 Public Access to Bitmap Data
- Added public `getBitmapData()` method to all platform-specific Texture classes
- Added same method to the `SmartTexture` class
- This allows direct access to the current bitmap data associated with a texture

### 2. GameScene Changes
#### 2.1 Forced Sprite Creation Allowance
- Added private static boolean `forceAllowSpriteCreation` field to GameScene
- Added public static method `setForceAllowSpriteCreation(boolean)` to control the forced allowance
- Modified `mayCreateSprites()` method to return `true` if either a scene is available OR force allowance is enabled
- This allows sprite generation in contexts where no game scene is active, such as the sprite generation utility

### 3. FactorySpriteGenerator Improvements
#### 3.1 Updated Generation Methods
- `generateAllMobsSpritesFromFactory()` now uses `MobFactory.allMobs()` instead of accessing internal factory fields through reflection
- `generateAllItemsSpritesFromFactory()` now uses `ItemFactory.allItems()` instead of accessing internal factory fields through reflection
- This provides a cleaner, more maintainable approach to accessing all game entities

#### 3.2 Simplified Item Generation Logic
- Removed fallback mechanisms that created ItemSprite instances to extract texture data
- The system now relies only on direct texture atlas access using `item.imageFile()` and `item.image()` properties
- This simplifies the sprite generation process and eliminates alternative code paths

#### 3.3 Enhanced Texture Data Extraction
- The `extractBitmapDataFromImage()` helper method was improved to use the new `getBitmapData()` functionality
- The method now prioritizes extracting actual bitmap data directly from the texture using the new method
- Falls back to the original approach only if direct access fails

#### 3.4 Bitmap Clearing Before Rendering
- Added `result.eraseColor(0x00000000)` calls before copying texture data to ensure clean state
- This clears the bitmap with transparent color before rendering, preventing visual artifacts
- Applied to both item generation and bitmap extraction processes

### 4. Gradle Task Updates
- Updated `generateSpritesFromFactories` task to use the new FactorySpriteGenerator implementation
- The task now reflects the simplified sprite generation logic without fallback mechanisms
- Proper texture data preservation is ensured during the generation process using the static disposal flag

## Generated Output

### File Naming Convention
- Mob sprites: `sprites/mob_[EntityName].png`
- Item sprites: `sprites/item_[EntityName].png`
- Spell sprites: `sprites/spell_[EntityName].png`
- Buff icons: `sprites/buff_[EntityName].png`
- Hero class sprites: `sprites/hero_[HeroClassName].png`
- Hero subclass sprites: `sprites/hero_subclass_[HeroSubClassName].png`
- Hero class+subclass sprites: `sprites/hero_[HeroClassName]_[HeroSubClassName].png`

### Image Specifications
- Size: 16x16 pixels (standard for game sprites)
- Format: PNG with 8-bit/color RGBA
- Content: Actual game sprite graphics extracted from texture atlases rather than colored placeholders

## Usage

To generate all mob, item, spell, buff, and hero sprites, run the following command:

```bash
cd /path/to/remixed-dungeon
./gradlew -c settings.desktop.gradle :RemixedDungeonDesktop:generateSpritesFromFactories
```

The generated sprites will be saved in:
`sprites/` (relative to the project root)

## Generated Output

### File Naming Convention
- Mob sprites: `sprites/mob_[EntityName].png`
- Item sprites: `sprites/item_[EntityName].png`
- Spell sprites: `sprites/spell_[EntityName].png`
- Buff icons: `sprites/buff_[EntityName].png`
- Hero class sprites: `sprites/hero_[HeroClassName].png`
- Hero subclass sprites: `sprites/hero_subclass_[HeroSubClassName].png`
- Hero class+subclass sprites: `sprites/hero_[HeroClassName]_[HeroSubClassName].png`

## Current Generated Sprites

### Mobs (All available mobs)
- All mobs from the game are now supported via `MobFactory.allMobs()`
- Each mob generates a proper sprite file with actual game graphics

### Items (All available items)
- All items from the game are now supported via `ItemFactory.allItems()`
- Each item generates a proper sprite file with actual game graphics

### Spells (All available spells)
- All spells from the game are now supported via `SpellFactory.getAllSpells()`
- Each spell generates a proper sprite file with actual game graphics
- File naming convention: `spell_[SpellName].png`

### Buffs (All available buff icons)
- All buff icons from the game are now supported via `BuffFactory.getAllBuffsNames()`
- Each buff generates a proper icon file with actual game graphics
- File naming convention: `buff_[BuffName].png`

### Heroes (All hero classes, subclasses, and VALID combinations)
- All hero classes from the game are now supported via `HeroClass.values()`
- All hero subclasses from the game are now supported via `HeroSubClass.values()`
- ONLY VALID combinations of hero classes and subclasses are generated based on `WndClass.java` definitions
- Valid combinations include:
  - Warrior: Gladiator, Berserker
  - Mage: BattleMage, Warlock
  - Rogue: FreeRunner, Assassin
  - Huntress: Sniper, Warden
  - Elf: Scout, Shaman
  - Necromancer: Lich
  - Gnoll: Guardian, WitchDoctor
  - Priest and Doctor: No subclasses (skipped for combination generation)
- File naming convention:
  - Hero classes: `hero_[HeroClassName].png`
  - Hero subclasses: `hero_subclass_[HeroSubClassName].png`
  - Valid hero class+subclass combinations: `hero_[HeroClassName]_[HeroSubClassName].png`
  - Invalid combinations: SKIPPED during generation

## Technical Approach

The system now follows these steps for sprite generation:
1. Sets the static `autoDisposeBitmapData` flag to false to preserve bitmap data during generation
2. Uses factory methods (`allMobs()`, `allItems()`) to get all entities instead of reflection
3. For hero generation, temporarily allows sprite creation using `GameScene.setForceAllowSpriteCreation(true)` to resolve "scene not ready for Hero" errors
4. For hero class+subclass combinations, validates against `WndClass.java` definitions to only generate valid combinations
5. For valid hero combinations, equips the appropriate class armor and random weapons based on class archetype
6. Extracts actual sprite data from the source texture atlases using frame coordinates
7. Clears each bitmap with transparent color before rendering new content
8. Sets the static `autoDisposeBitmapData` flag back to true to restore normal behavior
9. Sets `GameScene.setForceAllowSpriteCreation(false)` to restore normal scene requirements after hero generation

This approach ensures that the system can generate sprites for all game entities while maintaining proper memory management, avoiding initialization conflicts, and ensuring that only valid class|subclass combinations are created with their appropriate equipment.

## Code Examples

Two example classes were added demonstrating:
- `SmartTextureExample.java`: Shows how to use the static control of bitmap disposal, proper bitmap clearing before rendering, and best practices for texture management
- `GameSceneExample.java`: Shows how to use the `setForceAllowSpriteCreation()` method to temporarily allow sprite creation during utility operations like sprite generation

## Benefits of Changes

1. **Cleaner Code**: Removed reflection-based access in favor of public factory methods
2. **Better Performance**: More direct texture extraction without fallback logic
3. **Improved Quality**: Actual game sprites instead of colored placeholders
4. **Proper Cleanup**: Added bitmap clearing to prevent visual artifacts
5. **Maintainability**: Cleaner, more readable implementation
6. **Consistency**: Unified approach across all platforms (Android, Desktop, HTML)
7. **Memory Safety**: Static flag ensures proper disposal behavior restoration after generation
8. **Validation**: Only valid class|subclass combinations are generated as per `WndClass.java` definitions
9. **Enhanced Visuals**: Valid combinations include appropriate class armor and random weapons based on class archetype
10. **Fixed GameScene Issue**: Added `setForceAllowSpriteCreation()` method to `GameScene` to allow sprite creation during generation, resolving the "scene not ready for Hero" error that prevented hero class+subclass combination sprites from being generated
11. **Fixed Equipment Process Exceptions**: Added proper exception handling around equipment processes during sprite generation to catch and handle NullPointerExceptions that occurred when equipping items without proper level context, allowing generation to continue despite individual equipment failures
12. **Fixed Visual Consistency**: Enhanced both individual hero class sprites and hero class+subclass combination sprites with appropriate class armor and weapons to ensure visual completeness and consistency with game appearances

## Current Status

### Achieved Functionality
- Individual hero class sprites are successfully generated (e.g., `hero_WARRIOR.png`, `hero_MAGE.png`, etc.)
- Hero class+subclass combination sprites are now successfully generated (e.g., `hero_WARRIOR_GLADIATOR.png`, `hero_MAGE_WARLOCK.png`, etc.)
- All hero class sprites now include appropriate class armor and basic items for visual completeness
- All hero class+subclass combination sprites include appropriate class armor and class-appropriate weapons
- The NullPointerExceptions during equipment process have been handled with proper exception handling
- The "scene not ready for Hero" error has been resolved with the `setForceAllowSpriteCreation()` method

### Technical Status
- The GameScene fix has successfully enabled sprite generation for both individual hero classes and class+subclass combinations
- All other sprite types (mobs, items, spells, buffs) continue to generate correctly
- The hero generation code in FactorySpriteGenerator now includes proper exception handling around equipment calls
- The hero class sprites are now equipped with class-specific armor and basic weapons for visual consistency
- Memory management and disposal behaviors are properly handled during generation

## Future Enhancements

The system could be extended to:
- Support additional entity types like effects or UI elements
- Add options for different sprite export formats or resolutions
- Add validation to ensure generated sprites meet quality standards
- Add batch processing options for specific entity types
- Include Lua-based items (like shields) in hero sprite equipment
- Add more sophisticated weapon selection algorithms based on class abilities
- Enhance the visual accuracy by refining armor and weapon selection logic for specific class/subclass combinations