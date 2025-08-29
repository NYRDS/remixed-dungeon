# Remixed Dungeon HTML Build

This module contains the HTML build of Remixed Dungeon using LibGDX's GWT backend.

## Current Status

The HTML build is currently a work in progress. The basic structure has been set up with:
- GWT module definitions
- A placeholder HTML launcher
- Build configuration for GWT compilation

## What's Missing

To have a fully functional HTML build, the following would need to be implemented:
1. HTML-specific implementations of all platform abstractions (storage, audio, input, etc.)
2. Resolution of dependencies that are not compatible with GWT
3. Implementation of Android-specific code that's not available in the browser environment
4. Asset packaging for web delivery

## Building

To build the HTML version of the game, run:

```
./gradlew :RemixedDungeonHtml:compileGwt
```

The output would be generated in the `build/gwt/out` directory (when fully implemented).

## Running

To run the HTML version in development mode, run:

```
./gradlew :RemixedDungeonHtml:superDev
```

Then open http://localhost:8080 in your browser (when fully implemented).

## Deployment

To deploy the HTML version, copy the contents of `build/gwt/out` to your web server (when fully implemented).

Note that the HTML version has some limitations compared to the desktop version:
- File system access is limited to the browser's local storage
- Some audio features may not work the same way as in the desktop version
- Performance may be lower than the desktop version
- Some platform-specific features are not available