# Platform and Market Separation in Remixed Dungeon

## Overview

Remixed Dungeon implements a sophisticated platform and market separation architecture that allows the game to target multiple platforms (Android, Desktop, Web) and distribution channels (Google Play, F-Droid, ruStore, etc.) while sharing the majority of core game logic. This architecture is built on a platform abstraction layer that cleanly separates platform-specific implementations from the core game code.

## Platform Architecture

### 1. Multi-Module Structure

The project is organized into distinct modules for each platform:

1. **RemixedDungeon** - Android application module
2. **RemixedDungeonDesktop** - Desktop application module using LibGDX
3. **RemixedDungeonHtml** - Web application module using TeaVM (work in progress)

### 2. Platform Abstraction Layer

The platform abstraction is implemented through:
- Common interfaces and base classes that define contracts for platform-specific functionality
- Platform-specific implementations that fulfill these contracts
- Build system integration that includes appropriate implementations at build time

## Market Separation

### Android Market Flavors

The Android module uses Gradle product flavors to support different markets:

#### Flavor Dimensions
- **platform**: `android` (currently the only platform flavor)
- **market**: `googlePlay`, `fdroid`, `ruStore`, `huawei` (partially implemented but commented out)

#### Market Implementations

1. **Google Play**
   - Includes Google Play Services, Firebase Analytics, and Crashlytics
   - Integrates with Google AdMob for advertising
   - Full analytics and crash reporting capabilities
   - Billing through Google Play Billing Library

2. **F-Droid**
   - Completely free of proprietary dependencies
   - No analytics or crash reporting
   - Stub implementations for all market-specific services
   - Compatible with F-Droid's inclusion requirements

3. **ruStore**
   - Uses Yandex Mobile Ads for advertising
   - Integrates with ruStore billing system
   - Limited analytics capabilities
   - Russian market specific features

4. **Huawei** (Partially implemented but commented out)
   - Uses Huawei HMS services (implementation available but currently commented out)
   - Huawei AppGallery specific features
   - Currently commented out in build configuration

### Desktop Market Flavors

The desktop module uses source set organization for market separation:

1. **market_none** - Basic desktop implementation without market-specific features
2. **market_vkplay** - VK Play specific implementation (as seen in BuildConfig)

### Web Market Flavors

The web module currently uses:
1. **market_none** - Basic web implementation without market-specific features

## Key Platform Abstraction Components

### EventCollector
This is the most prominent example of market separation. Each market has its own implementation:

1. **Google Play**: Full Firebase integration with analytics, crash reporting, and event tracking
2. **F-Droid**: Empty stub implementation with no tracking capabilities
3. **ruStore**: Minimal implementation with basic logging
4. **Desktop**: Stub implementation similar to F-Droid
5. **Web**: Console-based logging implementation

### Build Configuration

Each platform has distinct build configurations:

#### Android
- Uses Gradle product flavors for market separation
- Flavor-specific dependencies in build.gradle
- Conditional plugin application based on selected flavor

#### Desktop
- Uses Gradle source sets for platform and market separation
- All dependencies included in single build configuration
- Custom tasks for packaging platform-specific distributions

#### Web
- Uses TeaVM plugin for Java-to-JavaScript compilation
- Source set organization for platform-specific implementations
- Work in progress with current compilation issues

## Implementation Patterns

### 1. Interface-Based Abstraction
Most platform-specific functionality is abstracted through interfaces or base classes that are implemented differently on each platform.

### 2. Static Method Abstraction
Many platform services are implemented as static methods that delegate to platform-specific implementations.

### 3. Flavor-Specific Implementations
Android uses Gradle flavors to provide different implementations of the same classes for different distribution channels.

### 4. Source Set Organization
Desktop and Web platforms use source set organization to include platform-specific code.

## Build System Configuration

### Android Build Variants
The Android module supports multiple build variants combining platform and market flavors:
- androidGooglePlayDebug/Release
- androidFdroidDebug/Release
- androidRuStoreDebug/Release

### Desktop Packaging
The desktop module includes custom packaging tasks for different platforms:
- Linux (packageLinuxX64)
- macOS (packageMacM1)
- Windows (packageWinX64)

### Web Compilation
The web module uses TeaVM for Java-to-JavaScript compilation, though currently non-functional.

## Benefits of This Architecture

1. **Code Reuse**: The majority of game logic is shared across platforms
2. **Maintainability**: Changes to core game logic only need to be made once
3. **Consistency**: Ensures consistent behavior across platforms
4. **Flexibility**: Easy to add new platforms or modify existing platform implementations
5. **Compliance**: Allows meeting distribution requirements for different markets (e.g., F-Droid's no-proprietary-dependencies rule)
6. **Testing**: Core game logic can be tested independently of platform-specific code

## Current Status

### Web Platform
The web platform using TeaVM is currently under development and not yet compilable. The build process currently fails with a NullPointerException during the JavaScript generation phase.

### Huawei Support
Huawei market support was partially implemented but is currently commented out in the build configuration.