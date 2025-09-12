# Remixed Dungeon - Project Overview

## Project Description
Remixed Dungeon is a classic roguelike game with pixel graphics and a simple interface. It is a fork of the famous Pixel Dungeon game, featuring English, Russian, and many more localizations. The game is available on multiple platforms including Android (Google Play, ruStore), desktop (Windows, macOS, Linux), and web browsers.

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
├── RemixedDungeonHtml/          # HTML5/WebGL application module (using TeaVM - work in progress)
│   ├── src/
│   │   ├── html/                # HTML5-specific sources
│   │   └── market_none/         # No-market flavor
│   └── build.gradle             # HTML module build configuration
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

### Web Version (Work in Progress)
- **Language**: Java (intended to be transpiled to JavaScript)
- **Framework**: TeaVM with LibGDX backend
- **Build System**: Gradle with TeaVM plugin
- **Dependencies**:
  - LibGDX TeaVM backend
  - Same core dependencies as other platforms

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

### Web (Work in Progress)
The web version is intended to use TeaVM to compile Java code to JavaScript:
- TeaVM 0.12.3 for Java-to-JavaScript transpilation
- LibGDX TeaVM backend for WebGL rendering
- Intended for browser-based deployment with canvas rendering
- Input handling to be adapted for mouse/keyboard/web events

## Development Environment
- **Java Version**: 10 (source and target compatibility)
- **IDE**: Android Studio or IntelliJ IDEA
- **Build Tools**: Gradle 8.x
- **Version Control**: Git

## Distribution Channels
- **Android**: Google Play, ruStore, F-Droid (planned)
- **Desktop**: VkPlay, direct downloads
- **Web**: Browser-based deployment (planned, not yet available)

## Project Modules
1. **RemixedDungeon**: Main Android application
2. **RemixedDungeonDesktop**: Desktop port using LibGDX
3. **RemixedDungeonHtml**: Web version using TeaVM (work in progress)
4. **annotation**: Custom annotations used in the project
5. **processor**: Annotation processor for code generation
6. **json_clone**: Utilities for JSON object cloning
7. **GameServices**: Integration with various game services

## Key Features
- Classic roguelike gameplay
- Pixel art graphics
- Multiple language localizations
- Cross-platform support (Android, desktop, and web)
- Modular architecture with flavor support
- Extensive modding capabilities
- Multiple distribution channels

## Build Instructions
1. **Android**: 
   ```bash
   ./gradlew :RemixedDungeon:assembleGooglePlayRelease
   ./gradlew :RemixedDungeon:assembleRuStoreRelease
   ./gradlew :RemixedDungeon:assembleFdroidRelease
   ```

2. **Desktop**:
   ```bash
   ./gradlew :RemixedDungeonDesktop:build
   ./gradlew :RemixedDungeonDesktop:packageLinuxX64
   ./gradlew :RemixedDungeonDesktop:packageMacM1
   ./gradlew :RemixedDungeonDesktop:packageWinX64
   ```

3. **Web** (Work in Progress):
   ```bash
   ./gradlew :RemixedDungeonHtml:build
   ./gradlew :RemixedDungeonHtml:generateJavaScript  # Currently fails with NullPointerException
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

## Platform Abstraction
The project implements a platform abstraction layer that allows the game to run on multiple platforms while sharing the majority of the game code. For more details, see [platform.md](platform.md).

## Repository Map
For a detailed map of the repository structure and architecture, see [REPO_MAP.md](REPO_MAP.md).

## Qwen Added Memories
- Key files: Hero.java (hero character logic) at RemixedDungeon/src/main/java/com/watabou/pixeldungeon/actors/hero/Hero.java, CharSprite.java (character sprite rendering) at RemixedDungeon/src/main/java/com/watabou/pixeldungeon/sprites/CharSprite.java, Actor.java (actor system) at RemixedDungeon/src/main/java/com/watabou/pixeldungeon/actors/Actor.java
