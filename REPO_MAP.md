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
├── RemixedDungeonHtml/             # HTML5/WebGL application module (using GWT)
│   ├── src/
│   │   ├── html/                   # HTML5-specific sources
│   │   │   └── java/               # Java sources (GWT-specific implementations)
│   │   │       ├── com/nyrds/platform/app/RemixedDungeonApp.java  # GWT Application class
│   │   │       └── com/nyrds/platform/app/client/HtmlLauncher.java  # GWT Entry point
│   │   ├── main/                   # Main webapp resources
│   │   │   └── resources/          # GWT module definition files
│   │   │       ├── com/nyrds/pixeldungeon/html/GdxDefinition.gwt.xml  # Main GWT module
│   │   │       ├── com/nyrds/pixeldungeon/html/GdxDefinitionSuperdev.gwt.xml  # SuperDev mode module
│   │   │       └── com/nyrds/platform/Platform.gwt.xml  # Platform module
│   │   └── market_none/            # No-market flavor
│   └── build.gradle                # HTML module build configuration (GWT plugin setup)
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

#### HTML Implementation (`com.nyrds.platform.game`)
- `Game`: GWT-based game implementation using LibGDX HTML backend
- `RemixedDungeon`: HTML game entry point
- Uses GWT (Google Web Toolkit) to compile Java to JavaScript
- Implements platform-specific adaptations for browser environment

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
- Module-specific `build.gradle` files for Android and Desktop

### Flavors
- `googlePlay`: Google Play Store version with ads/analytics
- `fdroid`: F-Droid version without proprietary dependencies
- `ruStore`: Russian app store version
- `huawei`: Huawei AppGallery version (planned)

### HTML Platform
- Uses GWT (Google Web Toolkit) to compile Java code to JavaScript
- Implements LibGDX HTML backend for WebGL rendering
- Browser-based distribution for web play
- Uses Gradle GWT plugin for building
- GWT module definition files control compilation
- SuperDev mode support for debugging

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
- Separate implementations for Android, Desktop, and HTML
- Build flavors for different distribution channels

### 6. HTML Platform Implementation
The HTML platform uses GWT (Google Web Toolkit) to compile Java code to JavaScript, enabling the game to run in web browsers.

#### Key Components
- `Game`: GWT-based game implementation extending LibGDX's ApplicationListener
- `RemixedDungeonApp`: Main GWT application class that extends GwtApplication
- `HtmlLauncher`: GWT entry point class that creates the application listener
- GWT module definitions for compilation (GdxDefinition.gwt.xml, etc.)

#### GWT-Specific Files
- **RemixedDungeonApp.java**: Main GWT application class that configures and launches the game
- **HtmlLauncher.java**: Entry point class that implements GwtApplication and creates the application listener
- **GdxDefinition.gwt.xml**: Main GWT module definition file that inherits LibGDX backend
- **GdxDefinitionSuperdev.gwt.xml**: GWT module for SuperDev mode debugging
- **Platform.gwt.xml**: Platform-specific GWT module definition

#### Current Implementation Status
- Java compilation successful after removing duplicate class definitions
- GWT module files have been restored and are found by the compiler
- GWT compilation in progress but has classpath issues
- Browser-based deployment with canvas rendering
- Input handling adapted for mouse/keyboard/web events