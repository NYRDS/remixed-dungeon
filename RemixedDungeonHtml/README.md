# Remixed Dungeon HTML Build

This module contains the HTML build of Remixed Dungeon using LibGDX's GWT backend.

## Current Status

**PARTIALLY COMPILABLE** - As of August 30, 2025, significant progress has been made:
- Java compilation is now successful
- GWT compilation is failing with missing class errors (down from 110 compilation errors)
- The Chrome theme issue has been resolved

See COMPILATION_ERRORS.md for details on remaining issues.

The basic structure has been set up with:
- GWT module definitions
- A placeholder HTML launcher
- Build configuration for GWT compilation
- Implementation of many platform abstraction classes

## Recent Improvements

Latest work has improved the build configuration:
- Resolved the Chrome theme inheritance issue by creating a modified JAR file
- Updated build.gradle to use the modified JAR with Chrome theme inheritance removed
- Created minimal Chrome theme files to satisfy GWT compiler requirements
- Java compilation now succeeds
- Reduced compilation errors from 110 to a smaller set of missing class issues

## What's Missing

To have a fully functional HTML build, the following would need to be implemented:
1. HTML-specific implementations of all platform abstractions (storage, audio, input, etc.)
2. Resolution of dependencies that are not compatible with GWT
3. Implementation of Android-specific code that's not available in the browser environment
4. Asset packaging for web delivery
5. Implement all missing methods identified in compilation errors

## Building

**Current Status**: Java compilation succeeds, GWT compilation fails with missing class errors.

To build the HTML version of the game, run:

```
./gradlew :RemixedDungeonHtml:compileGwt
```

The output would be generated in the `build/gwt/out` directory (when fully implemented).

**Progress**: The Chrome theme issue that was causing 110 compilation errors has been resolved. The remaining errors are related to GWT module inheritance and missing class paths.

## Running

**Current Status**: Cannot run due to GWT compilation failures.

To run the HTML version in development mode, run:

```
./gradlew :RemixedDungeonHtml:gwtSuperDev
```

Then open http://localhost:8080 in your browser (when fully implemented).

**Progress**: The underlying Java compilation now succeeds, which is a significant step forward. Once the GWT compilation issues are resolved, the superdev server should work.

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
- PROGRESS_SUMMARY.md - Detailed progress report
- DOCUMENTATION.md - General documentation
- COMPILATION_ERRORS.md - Detailed compilation error analysis
- BUILD_STATUS.md - Current build status and progress report
- NEXT_STEPS.md - Detailed instructions for next steps
- SUMMARY.md - Summary of work completed and remaining tasks