# Remixed Dungeon Repository Map

## Project Structure

```
remixed-dungeon/
├── RemixedDungeon/                 # Android application module
│   ├── src/
│   │   ├── android/                # Android-specific sources
│   │   ├── fdroid/                 # F-Droid flavor
│   │   ├── googlePlay/             # Google Play flavor
│   │   ├── huawei/                 # Huawei flavor
│   │   ├── main/                   # Main application sources
│   │   └── ruStore/                # ruStore flavor
│   └── build.gradle                # Android module build configuration
├── RemixedDungeonDesktop/          # Desktop application module
│   ├── src/
│   │   ├── desktop/                # Desktop-specific sources
│   │   ├── libgdx/                 # LibGDX integration
│   │   ├── market_none/            # No-market flavor
│   │   └── generated/              # Generated sources
│   └── build.gradle                # Desktop module build configuration
├── RemixedDungeonHtml/             # HTML5/WebGL application module (using TeaVM)
│   ├── src/
│   │   ├── html/                   # HTML5-specific sources
│   │   │   └── java/               # Java sources (TeaVM-specific implementations)
│   │   │       └── com/nyrds/platform/app/client/TeaVMLauncher.java  # TeaVM Entry point
│   │   ├── main/                   # Main webapp resources
│   │   └── market_none/            # No-market flavor
│   └── build.gradle                # HTML module build configuration (TeaVM plugin setup)
├── annotation/                     # Annotation definitions
├── processor/                      # Annotation processor
├── json_clone/                     # JSON cloning utilities
├── GameServices/                   # Game services integration
├── modding/                        # Modding resources
├── TiledMaps/                      # Map files
├── tools/                          # Development tools
└── dist/                           # Distribution packages
```

## Core Game Architecture

### 1. Main Game Components

#### Game Engine (`com.watabou.noosa`)
- `Game`: Base game class handling lifecycle, rendering, and input
- `Scene`: Base scene class for different game screens
- `Actor`: Base class for all game entities that participate in the game loop
- `Sprite`: Base class for visual representations of game entities
- `Image`, `Group`, `Camera`: Visual components

#### Core Game Logic (`com.watabou.pixeldungeon`)
- `Dungeon`: Main game state manager
- `GameLoop`: Game loop implementation handling updates and rendering
- `GameScene`: Main gameplay scene
- `PixelScene`: Base class for UI scenes

### 2. Game Entities

#### Characters (`com.watabou.pixeldungeon.actors`)
- `Char`: Base character class with stats, inventory, and behavior
  - `Hero`: Player character implementation
  - `Mob`: Base enemy/NPC class
    - `HeroClass`: Character classes (Warrior, Mage, Rogue, etc.)
    - `HeroSubClass`: Character subclasses (Gladiator, Berserker, etc.)
- `Buff`: Temporary character effects
- `Blob`: Area effects (gases, liquids)

#### Items (`com.watabou.pixeldungeon.items`)
- `Item`: Base item class
  - `EquipableItem`: Base for equippable items
    - `Weapon`: Melee and ranged weapons
    - `Armor`: Protective equipment
  - `Potion`: Consumable potions
  - `Scroll`: Magic scrolls
  - `Wand`: Magic wands
  - `Ring`: Equippable rings
  - `Food`: Consumable food items
  - `Heap`: Item container on the ground

#### Levels (`com.watabou.pixeldungeon.levels`)
- `Level`: Base level class with map data and logic
  - `RegularLevel`: Procedurally generated levels
    - `SewerLevel`, `PrisonLevel`, etc.: Specific dungeon levels
  - `BossLevel`: Special boss fight levels
- `Room`: Room definitions for level generation
- `Terrain`: Terrain type definitions
- `TerrainFlags`: Terrain property flags

#### Visuals (`com.watabou.pixeldungeon.sprites`)
- `CharSprite`: Base sprite for characters
- `HeroSpriteDef`: Hero sprite implementation
- `MobSprite`: Enemy sprite base class
- `ItemSprite`: Item visualization
- `Glowing`: Special sprite effects

### 3. User Interface

#### Scenes (`com.watabou.pixeldungeon.scenes`)
- `TitleScene`: Main menu
- `GameScene`: Main gameplay screen
- `InterlevelScene`: Level transitions
- `PixelScene`: Base UI scene

#### UI Components (`com.watabou.pixeldungeon.ui`)
- `Toolbar`: Main action toolbar
- `QuickSlot`: Quick item access slots
- `StatusPane`: Character status display
- `Inventory`: Item inventory management
- `Windows`: Various dialog windows

### 4. Platform Abstraction

#### Android Implementation (`com.nyrds.platform.game`)
- `Game`: Android-specific game implementation
- `RemixedDungeon`: Main Android activity

#### Desktop Implementation (`com.nyrds.platform.game`)
- `Game`: LibGDX-based game implementation
- `RemixedDungeon`: Desktop game launcher

#### Web Implementation (`com.nyrds.platform.game`) (Work in Progress)
- `Game`: Intended TeaVM-based game implementation using LibGDX backend
- `TeaVMLauncher`: TeaVM entry point class that creates the application listener
- Intended to use TeaVM to compile Java code to JavaScript
- Intended to implement platform-specific adaptations for browser environment

#### Common Platform Services (`com.nyrds.platform`)
- `Audio`: Sound and music management
- `Storage`: File system operations
- `Input`: Input handling
- `EventCollector`: Analytics and error reporting

### 5. Modding System

#### Lua Integration (`com.nyrds.lua`)
- `LuaEngine`: Lua script engine
- `LuaInterface`: Java-Lua bridge

#### Mod Data (`modding/`)
- JSON files for character, item, and level definitions
- Sprite definitions
- Localization files

## Build System

### Gradle Configuration
- `build.gradle`: Root project configuration
- `settings.gradle`: Module inclusion
- Module-specific `build.gradle` files for Android, Desktop, and Web

### Flavors
- `googlePlay`: Google Play Store version with ads/analytics
- `fdroid`: F-Droid version without proprietary dependencies
- `ruStore`: Russian app store version
- `huawei`: Huawei AppGallery version (planned)

### Web Platform (Work in Progress)
- Intended to use TeaVM to compile Java code to JavaScript
- Intended to implement LibGDX TeaVM backend for WebGL rendering
- Intended for browser-based distribution for web play
- Uses Gradle TeaVM plugin for building

## Key Patterns and Concepts

### 1. Entity System
- Each game entity (Hero, Mobs, Items) extends from appropriate base classes
- Entities are identified by class names and can be dynamically created
- JSON configuration files define entity properties

### 2. Game Loop
- Time-based turn system with real-time option
- Actors take turns based on speed and actions
- Asynchronous processing for UI updates

### 3. Level Generation
- Room-based procedural generation
- Different painters for room types
- Terrain and object placement algorithms

### 4. Modding Support
- JSON-based entity definitions
- Lua scripting for behavior customization
- Asset replacement system
- Multiple mod directories

### 5. Platform Abstraction
- Common interfaces for platform-specific functionality
- Separate implementations for Android, Desktop, and Web
- Build flavors for different distribution channels

### 6. Web Platform Implementation (Work in Progress)
The web platform is intended to use TeaVM to compile Java code to JavaScript, enabling the game to run in web browsers.

#### Key Components
- `TeaVMLauncher.java`: Main TeaVM application class that configures and launches the game
- TeaVM configuration in build.gradle that specifies the main class and target file name

#### TeaVM-Specific Files
- **TeaVMLauncher.java**: Entry point class that creates the TeaApplication with the game configuration
- **build.gradle**: Contains TeaVM plugin configuration and dependencies

#### Current Implementation Status
The web platform using TeaVM is currently under development and not yet compilable. The build process currently fails with a NullPointerException during the JavaScript generation phase. Work is ongoing to resolve compilation issues and enable browser-based deployment.