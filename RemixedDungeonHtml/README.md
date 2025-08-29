# Remixed Dungeon HTML Build

This module contains the HTML build of Remixed Dungeon using LibGDX's GWT backend.

## Current Status

**NOT COMPILABLE** - As of August 29, 2025, the HTML build fails to compile with 100 compilation errors. See BUILD_ANALYSIS_REPORT.md for details.

The basic structure has been set up with:
- GWT module definitions
- A placeholder HTML launcher
- Build configuration for GWT compilation
- Implementation of many platform abstraction classes

## Recent Improvements

Latest commits have improved the build configuration:
- Include `build/generated/sources/annotationProcessor/java/main` in html sourceSets so generated classes (e.g., BundleHelper) are compiled
- Added compileJava dependency on `:processor:compileJava` to ensure annotation processing runs before HTML compilation
- Retained generateBuildConfig and codegen dependencies

## What's Missing

To have a fully functional HTML build, the following would need to be implemented:
1. HTML-specific implementations of all platform abstractions (storage, audio, input, etc.)
2. Resolution of dependencies that are not compatible with GWT
3. Implementation of Android-specific code that's not available in the browser environment
4. Asset packaging for web delivery
5. Implement all missing methods identified in compilation errors

## Building

**Current Status**: Build fails with 100 compilation errors.

To build the HTML version of the game, run:

```
./gradlew :RemixedDungeonHtml:compileGwt
```

The output would be generated in the `build/gwt/out` directory (when fully implemented).

## Running

**Current Status**: Cannot run due to compilation failures.

To run the HTML version in development mode, run:

```
./gradlew :RemixedDungeonHtml:gwtSuperDev
```

Then open http://localhost:8080 in your browser (when fully implemented).

## Deployment

To deploy the HTML version, copy the contents of `build/gwt/out` to your web server (when fully implemented).

## Limitations

Note that the HTML version has some limitations compared to the desktop version:
- File system access is limited to the browser's local storage
- Some audio features may not work the same way as in the desktop version
- Performance may be lower than the desktop version
- Some platform-specific features are not available

## Documentation

For detailed information about the current status, see:
- BUILD_ANALYSIS_REPORT.md - Summary of build analysis
- PROGRESS_SUMMARY.md - Detailed progress report
- DOCUMENTATION.md - General documentation
- COMPILATION_ERRORS.md - Detailed compilation error analysis