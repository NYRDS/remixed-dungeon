# Platform Abstraction Layer Documentation

## Overview

The Remixed Dungeon project implements a platform abstraction layer that allows the game to run on multiple platforms (Android and Desktop) while sharing the majority of the game code. This abstraction layer separates platform-specific implementations from the core game logic, enabling a single codebase to target multiple platforms.

## Architecture

The platform abstraction is implemented through:

1. **Common Interface Layer**: Shared interfaces and base classes that define the contract for platform-specific functionality
2. **Platform-Specific Implementations**: Separate implementations for each target platform (Android, Desktop)
3. **Build System Integration**: Gradle flavors and source sets that include the appropriate platform implementation at build time

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

### 2. Audio System

**Music Management**:
- Android: Uses `MediaPlayer` API for background music
- Desktop: Uses LibGDX audio system

**Sound Effects**:
- Android: Uses `SoundPool` API for sound effects
- Desktop: Uses LibGDX sound system

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

### 4. Preferences and Settings

**Android Implementation**:
- Uses Android `SharedPreferences` system
- Implements data encryption for sensitive information
- Located in `RemixedDungeon/src/android/java/com/nyrds/platform/storage/Preferences.java`

**Desktop Implementation**:
- Uses HJSON file-based storage
- Stores preferences in a local file
- Located in `RemixedDungeonDesktop/src/libgdx/java/com/nyrds/platform/storage/Preferences.java`

### 5. Input Handling

**Touch and Pointer Events**:
- Android: Processes Android `MotionEvent` objects
- Desktop: Processes LibGDX pointer events

**Keyboard Input**:
- Android: Handles Android `KeyEvent` objects
- Desktop: Uses LibGDX input processor

### 6. Networking and Analytics

**Event Collection**:
- Android (Google Play): Integrates with Firebase Analytics and Crashlytics
- Android (F-Droid): Provides stub implementation without analytics
- Desktop: Provides stub implementation

Located in flavor-specific directories:
- `RemixedDungeon/src/googlePlay/java/com/nyrds/platform/EventCollector.java`
- `RemixedDungeon/src/fdroid/java/com/nyrds/platform/EventCollector.java`
- `RemixedDungeonDesktop/src/libgdx/java/com/nyrds/platform/EventCollector.java`

### 7. Utilities

**Platform Utilities**:
- Android: Uses Android logging, connectivity, and system APIs
- Desktop: Uses LibGDX utilities and standard Java APIs

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

## Key Abstraction Classes

- `com.nyrds.platform.game.Game`: Main game lifecycle management
- `com.nyrds.platform.audio.MusicManager`: Background music management
- `com.nyrds.platform.audio.Sample`: Sound effects management
- `com.nyrds.platform.storage.FileSystem`: File system operations
- `com.nyrds.platform.storage.Preferences`: User preferences storage
- `com.nyrds.platform.EventCollector`: Analytics and error reporting
- `com.nyrds.platform.util.PUtil`: Platform utilities
- `com.nyrds.platform.input.Touchscreen`: Touch input handling

## Benefits

1. **Code Reuse**: The majority of game logic is shared across platforms
2. **Maintainability**: Changes to core game logic only need to be made once
3. **Consistency**: Ensures consistent behavior across platforms
4. **Flexibility**: Easy to add new platforms or modify existing platform implementations
5. **Testing**: Core game logic can be tested independently of platform-specific code