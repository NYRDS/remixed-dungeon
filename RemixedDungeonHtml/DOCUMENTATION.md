# HTML Build for Remixed Dungeon

This document explains how to build and run the HTML version of Remixed Dungeon.

## Prerequisites

- Java 10 or higher
- Gradle 8.x
- Python 3.x with lxml package

To install the required Python dependencies:
```
pip3 install lxml
```

## Building the HTML Version

To build the HTML version of the game, run:

```
./gradlew :RemixedDungeonHtml:compileGwt
```

This will compile the game for the web using GWT (Google Web Toolkit) and LibGDX's HTML backend.

The output will be generated in the `build/gwt/out` directory.

## Running in Development Mode

To run the HTML version in development mode with superdev server:

```
./gradlew :RemixedDungeonHtml:gwtSuperDev
```

Then open http://localhost:8080 in your browser.

## Deployment

To deploy the HTML version, copy the contents of `build/gwt/out` to your web server.

## Limitations

The HTML version has some limitations compared to the desktop version:

1. File system access is limited to the browser's local storage
2. Some audio features may not work the same way as in the desktop version
3. Performance may be lower than the desktop version
4. Some platform-specific features are not available
5. Save games are stored in browser local storage and may be cleared if the user clears their browser data

## Technical Details

The HTML build uses:
- LibGDX's GWT backend for WebGL rendering
- GWT 2.8.2 for Java to JavaScript compilation
- The same core game code as the desktop version
- Platform-specific implementations for HTML in the `src/html/java` directory

The build process:
1. Compiles the Java code to JavaScript using GWT
2. Packages all assets and resources
3. Generates the necessary HTML and JavaScript files for the web application

## Current Status

The HTML version is currently in development and not yet fully compilable. We are working on implementing the necessary platform abstraction layer to make the game compile and run in a web browser.

As of August 29, 2025, the build fails with 100 compilation errors. See COMPILATION_ERRORS.md for detailed error information.

### Recent Improvements

Recent commits have improved the build configuration:
- Include `build/generated/sources/annotationProcessor/java/main` in html sourceSets so generated classes (e.g., BundleHelper) are compiled
- Added compileJava dependency on `:processor:compileJava` to ensure annotation processing runs before HTML compilation
- Retained generateBuildConfig and codegen dependencies

### Work Completed So Far

1. Updated platform-specific classes to match the desktop version's API:
   - `StringsManager` - Added missing methods like `getVar(int)` and `getVars(int)`
   - `ModdingMode` - Added missing methods like `isResourceExistInMod` and `isAssetExist`
   - `Preferences` - Converted to enum with INSTANCE and added missing methods like `getDouble`
   - `Sample` - Added missing `play` method with 4 parameters
   - `SaveUtils` - Added missing methods like `slotUsed`, `deleteLevels`, etc.
   - `FileSystem` - Added missing methods like `getInternalStorageFileHandle`, `exists`, etc.
   - `MusicManager` - Added missing `volume` method
   - `NoosaScript` - Fixed implementation to match desktop version
   - `Program` - Fixed implementation to match desktop version
   - `Shader` - Fixed implementation to match desktop version
   - `Attribute` - Added missing class
   - `Uniform` - Added missing class
   - `EventCollector` - Fixed duplicate methods

2. Fixed class conflicts:
   - Updated `Texture` class to match desktop version implementation instead of extending LibGDX Texture
   - Updated `PlatformLuajavaLib` to match desktop version implementation

3. Removed manually created `BundleHelper` to let the annotation processor generate it

4. Configured build to include generated sources in compilation

### Remaining Issues

There are still several compilation errors that need to be addressed, primarily related to:
1. Missing methods in various platform abstraction classes
2. Missing constructors for UI components like `SystemText`
3. Missing methods in game-specific classes
4. Missing methods in event collection and analytics classes
5. Missing methods in modding and file system classes
6. Missing methods in platform abstraction classes (Preferences, ModdingMode, Game, Sample, MusicManager, etc.)
7. Android-specific references that need to be removed or stubbed
8. Method signature incompatibilities between HTML and Desktop implementations
9. Missing abstract method implementations

## Build Process Analysis

### Code Generation Steps
The build process successfully completes these steps:
1. `codegen` - Generates R.java and localization JSON files
2. `generateBuildConfig` - Generates BuildConfig.java with HTML-specific settings

### Failed Step
The build fails during `compileJava` with 100 compilation errors.

## Next Steps

To make the HTML version compilable, the following work is needed:

1. Implement missing methods in platform abstraction classes (Preferences, ModdingMode, Game, Sample, MusicManager, etc.)
2. Remove or stub Android-specific references (KeyEvent constants, AndroidSAF methods, etc.)
3. Add HTML-specific implementations for UI event handling
4. Fix method signature incompatibilities in graphics/texture classes
5. Implement missing abstract methods in HTML-specific classes
6. Address all issues documented in COMPILATION_ERRORS.md

## Testing the Build

To test the current state of the HTML build:

1. Ensure all prerequisites are installed
2. Include the HTML module in settings.gradle:
   ```
   include ':RemixedDungeonHtml'
   ```
3. Run one of the build commands:
   ```
   ./gradlew :RemixedDungeonHtml:compileGwt
   # or
   ./gradlew :RemixedDungeonHtml:gwtSuperDev
   ```

Both commands will currently fail with compilation errors.

## Error Analysis

See COMPILATION_ERRORS.md for a detailed analysis of the 100 compilation errors encountered during the build process. The errors are categorized by type and root cause to help prioritize the implementation work.