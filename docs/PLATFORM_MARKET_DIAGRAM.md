# Platform and Market Structure Diagram

```
Remixed Dungeon Project
├── Core Game Logic (Shared)
│   ├── Game Engine (com.watabou.noosa)
│   ├── Core Game Logic (com.watabou.pixeldungeon)
│   ├── Actors, Items, Levels
│   └── UI Components
│
├── Platform Modules
│   │
│   ├── Android (RemixedDungeon)
│   │   ├── Platform Abstraction Layer
│   │   │   ├── Game Lifecycle (Game.java)
│   │   │   ├── Audio System (MusicManager, Sample)
│   │   │   ├── Storage (FileSystem, Preferences)
│   │   │   ├── Input Handling (Touchscreen)
│   │   │   ├── Concurrency (ConcurrencyProvider)
│   │   │   └── Utilities (PUtil)
│   │   │
│   │   ├── Market Flavors (Product Flavors)
│   │   │   ├── googlePlay
│   │   │   │   ├── Firebase Analytics/Crashlytics
│   │   │   │   ├── Google Play Services
│   │   │   │   ├── AdMob Integration
│   │   │   │   └── Google Play Billing
│   │   │   │
│   │   │   ├── fdroid
│   │   │   │   ├── No proprietary dependencies
│   │   │   │   ├── Stub analytics implementation
│   │   │   │   └── F-Droid compliance
│   │   │   │
│   │   │   ├── ruStore
│   │   │   │   ├── Yandex Mobile Ads
│   │   │   │   ├── ruStore Billing
│   │   │   │   └── Russian market features
│   │   │   │
│   │   │   └── huawei (Planned)
│   │   │       ├── Huawei HMS Services
│   │   │       └── AppGallery features
│   │   │
│   │   └── Build System
│   │       ├── Flavor dimensions: platform, market
│   │       └── Conditional dependencies
│   │
│   ├── Desktop (RemixedDungeonDesktop)
│   │   ├── Platform Abstraction Layer
│   │   │   ├── LibGDX integration
│   │   │   ├── LWJGL3 backend
│   │   │   └── Cross-platform file I/O
│   │   │
│   │   ├── Market Flavors (Source Sets)
│   │   │   ├── market_none
│   │   │   └── market_vkplay
│   │   │
│   │   └── Build System
│   │       ├── Source set organization
│   │       ├── Custom packaging tasks
│   │       └── Cross-platform distribution
│   │
│   └── Web (RemixedDungeonHtml) [Work in Progress]
│       ├── Platform Abstraction Layer
│       │   ├── TeaVM integration
│       │   ├── LibGDX TeaVM backend
│       │   └── Browser APIs
│       │
│       ├── Market Flavors (Source Sets)
│       │   └── market_none
│       │
│       └── Build System
│           ├── TeaVM plugin
│           └── JavaScript compilation
│
├── Supporting Modules
│   ├── annotation
│   ├── processor
│   ├── json_clone
│   └── GameServices
│
└── Resources
    ├── modding
    ├── TiledMaps
    └── tools
```

## Build Variants Matrix

| Platform | Market | Build Variant | Key Features |
|----------|--------|---------------|--------------|
| Android | googlePlay | androidGooglePlay | Firebase, AdMob, Google Play Billing |
| Android | fdroid | androidFdroid | No analytics, no proprietary deps |
| Android | ruStore | androidRuStore | Yandex Ads, ruStore Billing |
| Android | huawei | androidHuawei | HMS Services (Planned) |
| Desktop | vkplay | desktop | VK Play distribution |
| Web | none | html | Browser deployment (WIP) |

## Dependency Management by Market

### Google Play
```
dependencies {
    googlePlayImplementation 'com.google.firebase:firebase-analytics'
    googlePlayImplementation 'com.google.firebase:firebase-crashlytics'
    googlePlayImplementation 'com.google.android.gms:play-services-ads'
    googlePlayImplementation 'com.android.billingclient:billing'
    googlePlayImplementation("com.appodeal.ads:sdk:3.7.0.0")
    googlePlayImplementation 'com.yandex.android:mobileads:7.13.0'
}
```

### F-Droid
```
dependencies {
    // No proprietary dependencies
    // All market-specific implementations are stubs
}
```

### ruStore
```
dependencies {
    ruStoreImplementation 'com.yandex.android:mobileads:7.13.0'
    ruStoreImplementation 'ru.rustore.sdk:billingclient:9.1.0'
}
```

## Platform-Specific Implementation Examples

### EventCollector (Analytics Interface)

1. **Google Play Implementation**: Full Firebase integration
2. **F-Droid Implementation**: Empty stub methods
3. **ruStore Implementation**: Minimal logging methods
4. **Desktop Implementation**: Stub with basic logging
5. **Web Implementation**: Console logging only

This structure allows the same core game code to be built for different platforms and markets while meeting the specific requirements of each distribution channel.