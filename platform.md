# Platform Abstraction Layer Documentation

## Overview

The Remixed Dungeon project implements a platform abstraction layer that allows the game to run on multiple platforms (Android, Desktop, and Web) while sharing the majority of the game code. This abstraction layer separates platform-specific implementations from the core game logic, enabling a single codebase to target multiple platforms.

## Architecture

The platform abstraction is implemented through:

1. **Common Interface Layer**: Shared interfaces and base classes that define the contract for platform-specific functionality
2. **Platform-Specific Implementations**: Separate implementations for each target platform (Android, Desktop, Web)
3. **Build System Integration**: Gradle modules and source sets that include the appropriate platform implementation at build time

## Key Platform Abstraction Components

### 1. Game Lifecycle Management

**Android Implementation**: 
- Extends `Activity` and implements `GLSurfaceView.Renderer`
- Manages OpenGL context, activity lifecycle, and system events
- Located in `RemixedDungeon/src/android/java/com/nyrds/platform/game/Game.java`

**Desktop Implementation**:
- Extends LibGDX `ApplicationListener` and `InputProcessor`
- Uses LWJGL backend for OpenGL rendering
- Located in `RemixedDungeonDesktop/src/libgdx/java/com/nyrds/platform/game/Game.java`

**Web Implementation** (Work in Progress):
- Uses LibGDX backend with TeaVM
- Intended to transpile Java code to JavaScript using TeaVM
- Located in `RemixedDungeonHtml/src/html/java/com/nyrds/platform/app/client/TeaVMLauncher.java`

### 2. Audio System

**Music Management**:
- Android: Uses `MediaPlayer` API for background music
- Desktop: Uses LibGDX audio system
- Web: Intended to use Web Audio API via LibGDX TeaVM backend

**Sound Effects**:
- Android: Uses `SoundPool` API for sound effects
- Desktop: Uses LibGDX sound system
- Web: Intended to use Web Audio API via LibGDX TeaVM backend

Both platforms implement the same interface through `MusicManager` and `Sample` classes.

### 3. Storage and File System

**Android Implementation**:
- Uses Android's internal and external storage APIs
- Manages file permissions and Android-specific storage locations
- Located in `RemixedDungeon/src/android/java/com/nyrds/platform/storage/FileSystem.java`

**Desktop Implementation**:
- Uses standard Java file I/O
- Works with the local file system
- Located in `RemixedDungeonDesktop/src/libgdx/java/com/nyrds/platform/storage/FileSystem.java`

**Web Implementation** (Work in Progress):
- Intended to use browser localStorage via LibGDX Preferences API
- Located in `RemixedDungeonHtml/src/html/java/com/nyrds/platform/storage/HtmlPreferences.java`

### 4. Preferences and Settings

**Android Implementation**:
- Uses Android `SharedPreferences` system
- Implements data encryption for sensitive information
- Located in `RemixedDungeon/src/android/java/com/nyrds/platform/storage/Preferences.java`

**Desktop Implementation**:
- Uses HJSON file-based storage
- Stores preferences in a local file
- Located in `RemixedDungeonDesktop/src/libgdx/java/com/nyrds/platform/storage/Preferences.java`

**Web Implementation** (Work in Progress):
- Intended to use browser localStorage via LibGDX Preferences API
- Intended to store preferences in JSON format
- Located in `RemixedDungeonHtml/src/html/java/com/nyrds/platform/storage/Preferences.java`

### 5. Input Handling

**Touch and Pointer Events**:
- Android: Processes Android `MotionEvent` objects
- Desktop: Processes LibGDX pointer events
- Web: Intended to process browser touch/mouse events via LibGDX TeaVM backend

**Keyboard Input**:
- Android: Handles Android `KeyEvent` objects
- Desktop: Uses LibGDX input processor
- Web: Intended to use browser keyboard events via LibGDX TeaVM backend

### 6. Networking and Analytics

**Event Collection**:
- Android (Google Play): Integrates with Firebase Analytics and Crashlytics
- Android (F-Droid): Provides stub implementation without analytics
- Desktop: Provides stub implementation
- Web: Intended to provide stub implementation

Located in flavor-specific directories:
- `RemixedDungeon/src/googlePlay/java/com/nyrds/platform/EventCollector.java`
- `RemixedDungeon/src/fdroid/java/com/nyrds/platform/EventCollector.java`
- `RemixedDungeonDesktop/src/libgdx/java/com/nyrds/platform/EventCollector.java`
- `RemixedDungeonHtml/src/html/java/com/nyrds/platform/EventCollector.java`

### 7. Concurrency and Threading

**Android Implementation**:
- Uses standard Java concurrency utilities
- Thread pools for background tasks
- Atomic integers and concurrent collections

**Desktop Implementation**:
- Uses standard Java concurrency utilities
- Thread pools for background tasks
- Atomic integers and concurrent collections

**Web Implementation** (Work in Progress):
- TeaVM is single-threaded, so all operations will be synchronous
- Concurrent collections will be replaced with single-threaded equivalents
- Atomic integers will be replaced with regular integers with synchronized access
- Located in `RemixedDungeonHtml/src/html/java/com/nyrds/platform/ConcurrencyProvider.java`

### 8. Utilities

**Platform Utilities**:
- Android: Uses Android logging, connectivity, and system APIs
- Desktop: Uses LibGDX utilities and standard Java APIs
- Web: Intended to use LibGDX utilities and browser APIs

## Build System Configuration

### Android

The Android version uses Gradle product flavors to provide different implementations:

1. **googlePlay**: Includes Google Play Services, Firebase, and analytics
2. **fdroid**: F-Droid compatible version without proprietary dependencies
3. **ruStore**: Version for the Russian app store
4. **huawei**: Planned support for Huawei AppGallery

### Desktop

The desktop version uses LibGDX as the platform abstraction layer:
- LWJGL3 backend for OpenGL rendering
- Cross-platform file I/O
- Standard Java APIs for system integration

### Web

The web version uses TeaVM with LibGDX backend (Work in Progress):
- TeaVM 0.12.3 for Java-to-JavaScript transpilation
- LibGDX TeaVM backend for graphics, audio, and input
- Single-threaded execution model
- Browser localStorage for persistence

## Implementation Patterns

### 1. Interface-Based Abstraction

Most platform-specific functionality is abstracted through interfaces or base classes that are implemented differently on each platform.

### 2. Static Method Abstraction

Many platform services are implemented as static methods that delegate to platform-specific implementations.

### 3. Singleton Pattern

Several platform services use the singleton pattern to ensure consistent access across the application.

### 4. Flavor-Specific Implementations

Android uses Gradle flavors to provide different implementations of the same classes for different distribution channels.

## Adding New Platform Support

To add support for a new platform:

1. Create a new module or source set for the platform
2. Implement the required platform abstraction interfaces
3. Configure the build system to include the appropriate sources
4. Implement platform-specific features as needed
5. For web platforms, consider using TeaVM for Java-to-JavaScript compilation
6. For mobile platforms, consider using platform-specific UI frameworks alongside the core game logic

## Key Abstraction Classes

- `com.nyrds.platform.game.Game`: Main game lifecycle management
- `com.nyrds.platform.audio.MusicManager`: Background music management
- `com.nyrds.platform.audio.Sample`: Sound effects management
- `com.nyrds.platform.storage.FileSystem`: File system operations
- `com.nyrds.platform.storage.Preferences`: User preferences storage
- `com.nyrds.platform.EventCollector`: Analytics and error reporting
- `com.nyrds.platform.util.PUtil`: Platform utilities
- `com.nyrds.platform.input.Touchscreen`: Touch input handling
- `com.nyrds.platform.ConcurrencyProvider`: Platform-specific concurrency handling

## Current Status

### Web Platform Implementation Status
The web platform using TeaVM is currently under development and not yet compilable. The build process currently fails with a NullPointerException during the JavaScript generation phase. Work is ongoing to resolve compilation issues and enable browser-based deployment.

## Benefits

1. **Code Reuse**: The majority of game logic is shared across platforms
2. **Maintainability**: Changes to core game logic only need to be made once
3. **Consistency**: Ensures consistent behavior across platforms
4. **Flexibility**: Easy to add new platforms or modify existing platform implementations
5. **Testing**: Core game logic can be tested independently of platform-specific code
6. **Web Deployment**: Web version will allow playing the game directly in browsers (when complete)