# Platform and Market Overlay System in Remixed Dungeon

## Overview

The Remixed Dungeon project implements an overlay system that allows different platforms (Android, Desktop, Web) and markets (Google Play, F-Droid, ruStore) to provide platform-specific and market-specific implementations while sharing a common core codebase. This system uses build tool features (Gradle flavors for Android, source sets for all platforms) to layer implementations.

## Overlay Mechanisms

### 1. Android Platform Overlay System

Android uses Gradle product flavors to create build variants that overlay the core code:

#### Source Set Hierarchy
The Android build uses the following source set hierarchy (in order of precedence):
1. Flavor-specific source sets (googlePlay, fdroid, ruStore, huawei)
2. Platform source set (android)
3. Main source set (shared core)

#### Build Variants
When building for Android, the system creates variants that combine:
- Platform: android
- Market: googlePlay, fdroid, ruStore, or huawei

For example, building for Google Play creates the variant `androidGooglePlay`, which includes code from:
1. `src/googlePlay/java` (highest precedence)
2. `src/android/java` (platform-specific)
3. `src/main/java` (core shared code)

#### Overlay Example: EventCollector
The EventCollector class demonstrates the overlay pattern perfectly:

**Core Game Usage** (in `src/main/java`):
```java
// Core game code imports the abstraction
import com.nyrds.platform.EventCollector;

// Used throughout the codebase
EventCollector.logEvent("game_started");
```

**Market-Specific Implementations**:
- `src/googlePlay/java/com/nyrds/platform/EventCollector.java` - Full Firebase implementation
- `src/fdroid/java/com/nyrds/platform/EventCollector.java` - Empty stub implementation
- `src/ruStore/java/com/nyrds/platform/EventCollector.java` - Minimal logging implementation

The build system automatically selects the correct implementation based on the selected flavor.

### 2. Desktop Platform Overlay System

Desktop uses Gradle source sets to organize overlays:

#### Source Set Hierarchy
1. `src/libgdx/java` (platform-specific LibGDX implementation)
2. `src/desktop/java` (desktop-specific code)
3. `src/market_none/java` (market-specific code, currently minimal)
4. `../RemixedDungeon/src/main/java` (core shared code from Android module)
5. `src/generated/java` (generated code)

#### Overlay Example: Preferences
**Core Game Usage** (in shared code):
```java
import com.nyrds.platform.storage.Preferences;

// Used throughout the codebase
Preferences.INSTANCE.put("music_volume", 0.8);
float volume = Preferences.INSTANCE.getFloat("music_volume", 1.0f);
```

**Platform Implementation**:
- `RemixedDungeon/src/main/java/com/nyrds/platform/storage/CommonPrefs.java` - Shared preference keys
- `RemixedDungeonDesktop/src/libgdx/java/com/nyrds/platform/storage/Preferences.java` - HJSON file-based implementation

### 3. Web Platform Overlay System

Web platform uses source sets with TeaVM:

#### Source Set Hierarchy
1. `src/html/java` (web-specific TeaVM implementation)
2. `../RemixedDungeon/src/main/java` (core shared code)
3. `src/market_none/java` (market-specific code)
4. `build/generated/sources/annotationProcessor/java/main` (generated code)

#### Overlay Example: Preferences
**Platform Implementation**:
- `RemixedDungeon/src/main/java/com/nyrds/platform/storage/CommonPrefs.java` - Shared preference keys
- `RemixedDungeonHtml/src/html/java/com/nyrds/platform/storage/Preferences.java` - Delegates to HtmlPreferences
- `RemixedDungeonHtml/src/html/java/com/nyrds/platform/storage/HtmlPreferences.java` - Uses LibGDX Preferences with localStorage

## Overlay Patterns

### 1. Interface-Based Overlay
Some components use interface-based abstraction where the core defines interfaces and platforms provide implementations.

### 2. Class Replacement Overlay
More commonly used pattern where the core code imports classes by name, and different platforms provide different implementations of the same fully-qualified class name.

### 3. Stub Implementation Overlay
Some markets provide stub implementations of services they don't support (e.g., F-Droid has empty analytics implementations).

## Key Overlay Components

### 1. EventCollector (Analytics)
- **Google Play**: Full Firebase Analytics/Crashlytics integration
- **F-Droid**: Empty stub methods
- **ruStore**: Minimal logging
- **Desktop**: Stub with basic logging
- **Web**: Console-based logging

### 2. Preferences (Storage)
- **Android**: Android SharedPreferences with encryption
- **Desktop**: HJSON file-based storage
- **Web**: localStorage via LibGDX Preferences

### 3. FileSystem (File Operations)
- **Android**: Android storage APIs with SAF support
- **Desktop**: Standard Java file I/O
- **Web**: Browser file APIs via LibGDX

### 4. Audio System
- **Android**: MediaPlayer/SoundPool APIs
- **Desktop**: LibGDX audio system
- **Web**: Web Audio API via LibGDX TeaVM backend

## Build System Integration

### Android Flavor Overlay
```gradle
flavorDimensions "platform", "market"

productFlavors {
    googlePlay {
        dimension "market"
    }
    fdroid {
        dimension "market"
    }
    ruStore {
        dimension "market"
    }
    android {
        dimension "platform"
    }
}
```

### Desktop Source Set Overlay
```gradle
sourceSets {
    main {
        java {
            srcDirs = [
                '../RemixedDungeon/src/main/java',  // Core shared code
                'src/desktop/java',                 // Desktop-specific
                'src/market_none/java',             // Market-specific
                'src/libgdx/java',                  // LibGDX platform layer
                'src/generated/java'                // Generated code
            ]
        }
    }
}
```

### Web Source Set Overlay
```gradle
sourceSets {
    main {
        java {
            srcDirs = [
                'src/html/java',                    // HTML sources (highest priority)
                '../RemixedDungeon/src/main/java',  // Core shared code
                'src/market_none/java',             // Market-specific
                'build/generated/sources/annotationProcessor/java/main'  // Generated code
            ]
        }
    }
}
```

## Benefits of the Overlay System

1. **Code Reuse**: Maximum reuse of core game logic across all platforms
2. **Market Compliance**: Easy compliance with different market requirements (e.g., F-Droid's no-proprietary-dependencies rule)
3. **Maintenance**: Changes to core logic only need to be made once
4. **Flexibility**: Easy to add new markets or platforms
5. **Testing**: Core logic can be tested independently of platform-specific code
6. **Performance**: Platform-specific optimizations without affecting other platforms

## Implementation Details

### Package Structure Consistency
All platform implementations maintain the same package structure to enable seamless overlay:
```
com.nyrds.platform.EventCollector
com.nyrds.platform.storage.Preferences
com.nyrds.platform.audio.MusicManager
```

This consistency allows the core code to import these classes without knowing which platform implementation will be used at runtime.

### Build-Time Resolution
The overlay resolution happens at build time:
1. Android: Gradle flavor merging determines which source files are included
2. Desktop/Web: Source set ordering determines which files take precedence

This means there's no runtime overhead for the overlay system - each built variant contains only the implementation needed for that platform and market.