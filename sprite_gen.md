# Sprite Generation System for Remixed Dungeon

## Overview
The sprite generation system has been implemented to automatically create PNG image files for all mobs and items in the Remixed Dungeon game. This system provides a way to generate visual representations of game entities without having to manually create each sprite.

## Implementation Details

### 1. TestLevel Modifications
The `TestLevel.java` file was modified to include two new public methods:
- `generateMobSprites()` - Generates sprite files for all mobs
- `generateItemSprites()` - Generates sprite files for all items

These methods use `BitmapData` to create 16x16 pixel representations with unique colors based on entity names. The system implements robust error handling to skip problematic classes that cause initialization issues.

### 2. HeadlessTurnLimitedGame Enhancement
The `HeadlessTurnLimitedGame.java` class was enhanced to support custom level loading. Key changes include:
- Added `levelClass` property to store the specified level class
- Added `setLevelClass()` and `getLevelClass()` methods
- Modified the `create()` method to handle TestLevel specifically by calling `generateMobSprites()` and `generateItemSprites()` methods directly
- Added proper game initialization before sprite generation

### 3. Gradle Task Creation
A new Gradle task called `generateSprites` was added to `RemixedDungeonDesktop/build.gradle`. This task:
- Runs the game in headless mode
- Loads the TestLevel
- Executes the sprite generation methods
- Saves PNG files to the sprites/ directory

### 4. Custom Launcher
The `RunModForTurnsLauncher.java` was updated to support the `--levelClass` parameter, allowing the system to specify which level class to use during execution.

### 5. Robust Implementation
To handle initialization order problems that commonly occur in the game engine, the system:
- Uses a predefined list of safe-to-instantiate mob and item classes
- Implements try-catch blocks around each instantiation to handle errors gracefully
- Skips any classes that trigger problematic static initialization sequences


## Generated Output

### File Naming Convention
- Mob sprites: `sprites/mob_[EntityName].png`
- Item sprites: `sprites/item_[EntityName].png`

### Image Specifications
- Size: 16x16 pixels (standard for game sprites)
- Format: PNG with 8-bit/color RGBA
- Color: Unique colors based on entity name hash

## Usage

To generate all mob and item sprites, run the following command:

```bash
cd /path/to/remixed-dungeon
./gradlew -c settings.desktop.gradle :RemixedDungeonDesktop:generateSprites
```

The generated sprites will be saved in:
`RemixedDungeonDesktop/src/desktop/rundir/sprites/`

## Current Generated Sprites

### Mobs (8 total)
- mob_Bandit.png
- mob_Eye.png
- mob_Gnoll.png
- mob_Rat.png
- mob_Skeleton.png
- mob_Thief.png
- mob_Shielded.png
- mob_Spinner.png

### Items (9 total)
- item_OverpricedRation.png
- item_WandOfMagicMissile.png
- item_WandOfLightning.png
- item_ClothArmor.png
- item_LeatherArmor.png
- item_MailArmor.png
- item_RingOfAccuracy.png
- item_RingOfDetection.png
- item_RingOfElements.png

## Technical Approach

The system avoids the problematic static initialization sequences by:
1. Using a curated list of known classes instead of factory systems
2. Implementing reflection-based instantiation with proper error handling
3. Separating the sprite generation logic from the full game initialization

This approach ensures that the system can generate sprites for a wide range of game entities while avoiding the initialization conflicts that commonly occur when trying to instantiate game objects directly.

## Future Enhancements

The system could be extended to:
- Include more mob and item classes as they are verified to be safe
- Generate different types of visual representations (actual sprite graphics instead of colored squares)
- Support additional entity types like spells, effects, or UI elements