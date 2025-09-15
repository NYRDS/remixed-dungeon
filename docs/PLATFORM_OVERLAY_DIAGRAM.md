# Platform and Market Overlay System Diagram

## Visual Representation of Overlay Mechanisms

```
REMIXED DUNGEON OVERLAY SYSTEM
=============================

ANDROID PLATFORM OVERLAY
------------------------
Build Variants: androidGooglePlay, androidFdroid, androidRuStore, androidHuawei

┌─────────────────────────────────────────────────────────────┐
│                    FLAVOR-SPECIFIC CODE                     │
│  src/googlePlay    src/fdroid    src/ruStore    src/huawei  │
│  (Firebase impl)   (Stub impl)   (Yandex impl)  (HMS impl)  │
└─────────────────────────┬───────────────────────────────────┘
                          │
┌─────────────────────────▼───────────────────────────────────┐
│                    PLATFORM CODE                            │
│                 src/android/java                            │
│       (Android-specific implementations)                    │
└─────────────────────────┬───────────────────────────────────┘
                          │
┌─────────────────────────▼───────────────────────────────────┐
│                     CORE CODE                               │
│                  src/main/java                              │
│         (Shared game logic and abstractions)                │
└─────────────────────────────────────────────────────────────┘

Example: EventCollector Class Resolution
Core imports: com.nyrds.platform.EventCollector
Build for Google Play → Uses src/googlePlay/.../EventCollector.java
Build for F-Droid    → Uses src/fdroid/.../EventCollector.java
Build for ruStore    → Uses src/ruStore/.../EventCollector.java


DESKTOP PLATFORM OVERLAY
------------------------
Build Configuration: Single variant with source set hierarchy

┌─────────────────────────────────────────────────────────────┐
│                 PLATFORM-SPECIFIC CODE                      │
│              src/libgdx/java                                │
│         (LibGDX implementations)                            │
└─────────────────────────┬───────────────────────────────────┘
                          │
┌─────────────────────────▼───────────────────────────────────┐
│                 DESKTOP-SPECIFIC CODE                       │
│              src/desktop/java                               │
│         (Desktop-specific features)                         │
└─────────────────────────┬───────────────────────────────────┘
                          │
┌─────────────────────────▼───────────────────────────────────┐
│                  MARKET-SPECIFIC CODE                       │
│              src/market_none/java                           │
│         (Market-specific features)                          │
└─────────────────────────┬───────────────────────────────────┘
                          │
┌─────────────────────────▼───────────────────────────────────┐
│                     CORE CODE                               │
│         ../RemixedDungeon/src/main/java                     │
│         (Shared game logic and abstractions)                │
└─────────────────────────┬───────────────────────────────────┘
                          │
┌─────────────────────────▼───────────────────────────────────┐
│                  GENERATED CODE                             │
│              src/generated/java                             │
│         (Generated implementations)                         │
└─────────────────────────────────────────────────────────────┘

Example: Preferences Class Resolution
Core imports: com.nyrds.platform.storage.Preferences
Build process → Uses src/libgdx/.../Preferences.java


WEB PLATFORM OVERLAY
--------------------
Build Configuration: Single variant with source set hierarchy

┌─────────────────────────────────────────────────────────────┐
│                 PLATFORM-SPECIFIC CODE                      │
│              src/html/java                                  │
│         (TeaVM/HTML5 implementations)                       │
└─────────────────────────┬───────────────────────────────────┘
                          │
┌─────────────────────────▼───────────────────────────────────┐
│                     CORE CODE                               │
│         ../RemixedDungeon/src/main/java                     │
│         (Shared game logic and abstractions)                │
└─────────────────────────┬───────────────────────────────────┘
                          │
┌─────────────────────────▼───────────────────────────────────┐
│                  MARKET-SPECIFIC CODE                       │
│              src/market_none/java                           │
│         (Market-specific features)                          │
└─────────────────────────┬───────────────────────────────────┘
                          │
┌─────────────────────────▼───────────────────────────────────┐
│                  GENERATED CODE                             │
│  build/generated/sources/annotationProcessor/java/main      │
│         (Generated implementations)                         │
└─────────────────────────────────────────────────────────────┘

Example: Preferences Class Resolution
Core imports: com.nyrds.platform.storage.Preferences
Build process → Uses src/html/.../Preferences.java


OVERLAY RESOLUTION MECHANISMS
-----------------------------

Android (Flavor-Based Resolution)
┌─────────────────────────────────────────────────────────────┐
│  Build Time: Gradle selects source files based on flavor    │
│                                                             │
│  Core Code Import: import com.nyrds.platform.EventCollector │
│                                                             │
│  Build for googlePlay → src/googlePlay/.../EventCollector   │
│  Build for fdroid     → src/fdroid/.../EventCollector       │
│  Build for ruStore    → src/ruStore/.../EventCollector      │
└─────────────────────────────────────────────────────────────┘

Desktop/Web (Source Set Ordering)
┌─────────────────────────────────────────────────────────────┐
│  Build Time: Compiler uses first matching file in path      │
│                                                             │
│  Core Code Import: import com.nyrds.platform.storage.Preferences
│                                                             │
│  Source Set Order:                                          │
│  1. src/html/java         ← First match used                │
│  2. src/libgdx/java       ← Fallback if not in #1           │
│  3. src/main/java         ← Shared core code                │
└─────────────────────────────────────────────────────────────┘


EXAMPLE IMPLEMENTATION OVERLAYS
-------------------------------

EventCollector (Analytics Interface)
┌─────────────────────┬──────────────────────────────────────┐
│ Platform/Market     │ Implementation                       │
├─────────────────────┼──────────────────────────────────────┤
│ Google Play         │ Firebase Analytics/Crashlytics       │
│ F-Droid             │ Empty stub methods                   │
│ ruStore             │ Minimal logging                      │
│ Desktop             │ Stub with basic logging              │
│ Web                 │ Console-based logging                │
└─────────────────────┴──────────────────────────────────────┘

Preferences (Storage Interface)
┌─────────────────────┬──────────────────────────────────────┐
│ Platform/Market     │ Implementation                       │
├─────────────────────┼──────────────────────────────────────┤
│ Android             │ SharedPreferences with encryption    │
│ Desktop             │ HJSON file-based storage             │
│ Web                 │ localStorage via LibGDX              │
└─────────────────────┴──────────────────────────────────────┘

Audio System
┌─────────────────────┬──────────────────────────────────────┐
│ Platform/Market     │ Implementation                       │
├─────────────────────┼──────────────────────────────────────┤
│ Android             │ MediaPlayer/SoundPool APIs           │
│ Desktop             │ LibGDX audio system                  │
│ Web                 │ Web Audio API via LibGDX TeaVM       │
└─────────────────────┴──────────────────────────────────────┘
```

## Key Benefits of This Overlay System

1. **Maximum Code Reuse**: Core game logic is shared across all platforms
2. **Market Compliance**: Easy compliance with different market requirements
3. **Build-Time Resolution**: No runtime overhead for overlay resolution
4. **Easy Maintenance**: Changes to core logic only need to be made once
5. **Flexible Extension**: Easy to add new platforms or markets
6. **Independent Testing**: Core logic can be tested separately from platform code

This overlay system is the foundation that allows Remixed Dungeon to be distributed on multiple platforms and markets while maintaining a single, cohesive codebase.