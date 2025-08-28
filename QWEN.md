# Remixed Dungeon - Project Overview

## Project Description
Remixed Dungeon is a classic roguelike game with pixel graphics and a simple interface. It is a fork of the famous Pixel Dungeon game, featuring English, Russian, and many more localizations. The game is available on multiple platforms including Android (Google Play, ruStore) and desktop (Windows, macOS, Linux).

## Repository Information
- **Repository**: https://github.com/NYRDS/remixed-dungeon
- **License**: See LICENSE.txt
- **Build Status**: 
  - Bitrise: [![Bitrise](https://app.bitrise.io/app/e26fee6867be46dc/status.svg?token=6vQccAuFDO9IBcSGhQbwSg&branch=master)](https://app.bitrise.io/app/e26fee6867be46dc)
  - Codacy: [![Codacy Badge](https://app.codacy.com/project/badge/Grade/c4cb66f961fe4df2ba8e3a4ddf744e2e)](https://www.codacy.com/gh/NYRDS/remixed-dungeon/dashboard)
  - CodeFactor: [![CodeFactor](https://www.codefactor.io/repository/github/nyrds/remixed-dungeon/badge)](https://www.codefactor.io/repository/github/nyrds/remixed-dungeon)

## Project Structure
```
remixed-dungeon/
├── RemixedDungeon/              # Android application module
│   ├── src/
│   │   ├── android/             # Android-specific sources
│   │   ├── fdroid/              # F-Droid flavor
│   │   ├── googlePlay/          # Google Play flavor
│   │   ├── huawei/              # Huawei flavor
│   │   ├── main/                # Main application sources
│   │   └── ruStore/             # ruStore flavor
│   └── build.gradle             # Android module build configuration
├── RemixedDungeonDesktop/       # Desktop application module
│   ├── src/
│   │   ├── desktop/             # Desktop-specific sources
│   │   ├── libgdx/              # LibGDX integration
│   │   ├── market_none/         # No-market flavor
│   │   └── generated/           # Generated sources
│   └── build.gradle             # Desktop module build configuration
├── annotation/                  # Annotation definitions
├── processor/                   # Annotation processor
├── json_clone/                  # JSON cloning utilities
├── GameServices/                # Game services integration
└── dist/                        # Distribution packages
```

## Technology Stack

### Android Version
- **Language**: Java
- **Framework**: Android SDK
- **Target SDK**: 34
- **Min SDK**: 23 (Android 6.0)
- **Build System**: Gradle with Android Gradle Plugin 8.9.2
- **Dependencies**:
  - Gson for JSON parsing
  - LuaJ for scripting
  - Hjson for configuration
  - Commons-IO for file operations
  - AndroidX AppCompat
  - Google Play Services (for Google Play flavor)
  - Firebase (for Google Play flavor)
  - Appodeal SDK (for Google Play flavor)
  - Yandex Mobile Ads (for ruStore flavor)
  - Huawei HMS (planned support)

### Desktop Version
- **Language**: Java
- **Framework**: LibGDX
- **Build System**: Gradle with Shadow plugin for packaging
- **Dependencies**:
  - LibGDX core libraries
  - Same core dependencies as Android version

## Build Configuration

### Android
The Android version supports multiple product flavors:
- **googlePlay**: Google Play Store version with ads and analytics
- **ruStore**: ruStore version with Yandex billing
- **fdroid**: F-Droid version without proprietary dependencies
- **huawei**: Huawei AppGallery version (planned)

### Desktop
The desktop version uses LibGDX for cross-platform compatibility and includes:
- Packaging for Windows (.exe), macOS, and Linux
- Bundled JDK minimization for distribution
- Cross-platform asset packaging

## Development Environment
- **Java Version**: 10 (source and target compatibility)
- **IDE**: Android Studio or IntelliJ IDEA
- **Build Tools**: Gradle 8.x
- **Version Control**: Git

## Distribution Channels
- **Android**: Google Play, ruStore, F-Droid (planned)
- **Desktop**: VkPlay, direct downloads
- **Web**: Potential future support

## Project Modules
1. **RemixedDungeon**: Main Android application
2. **RemixedDungeonDesktop**: Desktop port using LibGDX
3. **annotation**: Custom annotations used in the project
4. **processor**: Annotation processor for code generation
5. **json_clone**: Utilities for JSON object cloning
6. **GameServices**: Integration with various game services

## Key Features
- Classic roguelike gameplay
- Pixel art graphics
- Multiple language localizations
- Cross-platform support (Android and desktop)
- Modular architecture with flavor support
- Extensive modding capabilities
- Multiple distribution channels

## Build Instructions
1. **Android**: 
   ```bash
   ./gradlew assembleGooglePlayRelease
   ./gradlew assembleRuStoreRelease
   ./gradlew assembleFdroidRelease
   ```

2. **Desktop**:
   ```bash
   ./gradlew :RemixedDungeonDesktop:build
   ./gradlew :RemixedDungeonDesktop:packageLinuxX64
   ./gradlew :RemixedDungeonDesktop:packageMacM1
   ./gradlew :RemixedDungeonDesktop:packageWinX64
   ```

## Project Links
- [Google Play](https://play.google.com/store/apps/details?id=com.nyrds.pixeldungeon.ml)
- [ruStore](https://apps.rustore.ru/app/com.nyrds.pixeldungeon.ml)
- [VkPlay](https://vkplay.ru/play/game/remixed-dungeon-pixel-rogue/)
- [Translation Project](https://www.transifex.com/projects/p/remixed-dungeon/)
- [Wiki](https://wiki.nyrds.net/)
- [YouTube](https://www.youtube.com/c/NYRDS)
- [Telegram](https://t.me/RemixedDungeon)
- [Discord](https://discord.gg/AMXrhQZ)

## Qwen Added Memories
- Key files: Hero.java (hero character logic) at RemixedDungeon/src/main/java/com/watabou/pixeldungeon/actors/hero/Hero.java, CharSprite.java (character sprite rendering) at RemixedDungeon/src/main/java/com/watabou/pixeldungeon/sprites/CharSprite.java, Actor.java (actor system) at RemixedDungeon/src/main/java/com/watabou/pixeldungeon/actors/Actor.java
