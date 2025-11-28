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
├── RemixedDungeonHtml/             # HTML5/WebGL application module (using TeaVM - work in progress)
│   ├── src/
│   │   ├── html/                   # HTML5-specific sources
│   │   │   └── java/               # Java sources (TeaVM-specific implementations)
│   │   │       └── com/nyrds/platform/app/client/TeaVMLauncher.java  # TeaVM Entry point
│   │   ├── main/                   # Main webapp resources
│   │   └── market_none/            # No-market flavor
│   └── build.gradle                # HTML module build configuration (TeaVM plugin setup)
├── snap/                           # Snap packaging configuration
│   ├── snapcraft.yaml              # Snap build configuration
│   └── local/                      # Snap helper scripts
│       ├── wrapper.sh              # Wrapper script for snap execution
│       └── update_version.sh       # Version update script
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
- `GameScene`: Main gameplay scene
- `PixelScene`: Base class for UI scenes with pixel-perfect rendering
- `AboutScene`: About/credits screen with WebServer information

#### UI Components (`com.watabou.pixeldungeon.ui`)
- `Button`: Base button class
- `RedButton`: Styled button with red theme
- `TextButton`: Button with text label
- `Window`: Modal dialog windows
- `ScrollPane`: Scrollable container
- `Toolbar`: Player action toolbar
- `StatusPane`: Player status display

#### UI Framework (`com.watabou.noosa.ui`)
- `Component`: Base UI component with layout capabilities
- `Group`: Container for other UI elements
- `Image`: Visual image elements
- `Text`: Text rendering elements
- `NinePatch`: Scalable UI elements

### 4. Platform Abstraction Layer

#### Game Management (`com.nyrds.platform.game`)
- `Game`: Platform-specific game lifecycle management
- `RemixedDungeon`: Main application class

#### Audio (`com.nyrds.platform.audio`)
- `MusicManager`: Background music management
- `Sample`: Sound effects management

#### Storage (`com.nyrds.platform.storage`)
- `FileSystem`: File system operations
- `Preferences`: User preferences storage
- `Assets`: Asset management

#### Input (`com.nyrds.platform.input`)
- `Touchscreen`: Touch input handling
- `PInputProcessor`: Platform-specific input processor

#### Utilities (`com.nyrds.platform.util`)
- `PUtil`: Platform utilities
- `ModdingMode`: Modding system management

#### Graphics (`com.nyrds.platform.gfx`)
- `BitmapData`: Cross-platform bitmap manipulation
- `PTextureFilm`: Texture management

#### Concurrency (`com.nyrds.platform`)
- `ConcurrencyProvider`: Platform-specific concurrency handling

#### Analytics (`com.nyrds.platform`)
- `EventCollector`: Analytics and error reporting

#### Platform Support (`com.nyrds.pixeldungeon.support`)
- `PlayGamesAdapter`: Interface for platform-specific game services (Google Play Games, etc.)
  - No-op implementation exists for market_none flavor (desktop/vanilla versions)

### 5. Modding System

#### Mod Management (`com.nyrds.util`)
- `ModdingMode`: Mod selection and resource management
- `ModdingBase`: Base modding functionality

#### Scripting (`com.nyrds.lua`)
- `LuaInterface`: Lua scripting interface
- `LuaEngine`: Lua script execution

### 6. Web Server (Android Only)

#### Web Server (`com.nyrds.platform.app`)
- `WebServer`: HTTP server for mod file management
- Located in `RemixedDungeon/src/android/java/com/nyrds/platform/app/WebServer.java`

Features:
- File browsing and downloading
- File uploading (with security restrictions)
- Directory navigation
- Cross-platform mod management

## Build System

### Gradle Modules

1. **RemixedDungeon**: Android application with flavor support
2. **RemixedDungeonDesktop**: Desktop application using LibGDX
3. **RemixedDungeonHtml**: Web application using TeaVM (work in progress)
4. **annotation**: Custom annotation definitions
5. **processor**: Annotation processor for code generation
6. **json_clone**: JSON cloning utilities

### Android Flavors

- **googlePlay**: Google Play Store version with analytics and ads
- **fdroid**: F-Droid version without proprietary dependencies
- **ruStore**: ruStore version with Yandex billing
- **huawei**: Planned Huawei AppGallery support

## Documentation

### Platform Documentation
- `platform.md`: Platform abstraction layer documentation
- `docs/PLATFORM_MARKET_STRUCTURE.md`: Platform and market separation architecture
- `docs/PLATFORM_MARKET_DIAGRAM.md`: Visual diagram of platform structure
- `docs/PLATFORM_OVERLAY_SYSTEM.md`: Platform and market overlay mechanisms
- `docs/PLATFORM_OVERLAY_DIAGRAM.md`: Visual diagram of overlay system

### UI Documentation
- `docs/UI_Composition_Principles.md`: UI component architecture and composition patterns

### Utilities Documentation
- `docs/BitmapData.md`: Cross-platform bitmap manipulation interface

### Web Server Documentation
- `docs/WebServer.md`: Web server functionality for mod management

### Snap Build Documentation
- `docs/SNAP_BUILD.md`: Snap build configuration and packaging instructions

## Resources

### Assets
- Graphics, sounds, and other media files
- Organized by type and module

### Maps
- Tiled map files for level design
- Located in `TiledMaps/` directory

### Modding Resources
- Template files and examples for mod creators
- Located in `modding/` directory

## Tools

### Development Tools
- Build scripts and utilities
- Located in `tools/` directory

### Annotation Processing
- Custom annotations for code generation
- Processor for generating boilerplate code

## Distribution

### Build Outputs
- Android APKs for different markets
- Desktop executables for Windows, macOS, and Linux
- Web deployment files (when complete)

### Packaging
- Gradle tasks for platform-specific packaging
- Distribution scripts for release management