# Web Server Debugging Quick Start

## Files to Reference

### Primary Documentation
- `debug_webserver_plan.md` - Current debugging plan with status and next steps
- `WEB_SERVER_GUIDE.md` - Instructions for running and testing the server

### Key Source Files
- `RemixedDungeon/src/main/java/com/nyrds/platform/app/BaseWebServer.java` - Base implementation with debugging logs
- `RemixedDungeonDesktop/src/libgdx/java/com/nyrds/platform/app/WebServer.java` - Desktop-specific implementation with debug endpoint
- `RemixedDungeon/src/main/java/com/nyrds/platform/app/FilesystemAccess.java` - Filesystem abstraction class

### Current Issue
- **Problem**: Directories like "scripts" appear empty due to path filtering in `listDirectoryContents` method
- **Root Cause**: ModdingMode.listResources returns direct child names, but filtering expects full paths
- **Debug Endpoint**: `/debug-list/scripts` shows 15 resources found but 0 after filtering

## Next Steps

1. ~~Fix WebServer's `listDirectoryContents` method filtering logic~~ - COMPLETED
2. ~~Test that directories now show their contents properly~~ - COMPLETED
3. ~~Verify all existing functionality still works~~ - COMPLETED

## Final Status - ISSUE RESOLVED

The WebServer directory listing issue has been successfully resolved:

- ✅ Path filtering logic in WebServer's `listDirectoryContents` method has been fixed
- ✅ Directories like "scripts" now properly show their contents (15 subdirectories) instead of appearing empty
- ✅ ModdingMode.listResources returns direct child names (like "actors"), which are now properly handled
- ✅ Fixed duplicate upload links issue in directory listing
- ✅ Debug endpoint `/debug-list/scripts` now shows 15 resources found and 15 after filtering (instead of 0)
- ✅ All existing functionality preserved (JSON editing, file downloads, etc.)

## New Lua Editor Functionality

The WebServer now includes comprehensive Lua editing capabilities:

### Features
- ✅ Lua files open in dedicated web-based editor with syntax highlighting via Ace Editor
- ✅ Lua auto-format functionality included
- ✅ Save functionality for Lua files with proper error handling
- ✅ Separate edit and download links in directory listings
- ✅ `/raw/` endpoint for efficient content loading by editors
- ✅ Download parameter support (`?download=1`) for direct file downloads
- ✅ Consistent implementation for both JSON and Lua editors
- ✅ Full support for both Android and Desktop WebServer implementations

### Endpoints
- `/edit-lua?file=path/to/file.lua` - Open Lua editor for specific file
- `/api/save-lua` - Save Lua file content (POST request)
- `/raw/path/to/file.lua` - Get raw file content without redirects
- `/fs/path/to/file.lua?download=1` - Force file download

### Directory Listing Format
Lua and JSON files now show as:
```
📄 filename.lua (edit) (download)
```
Where:
- Main link opens the editor
- Edit link also opens the editor (for consistency)
- Download link forces direct file download

### Editor Features
- Lua syntax highlighting through Ace Editor
- Auto-format button with custom Lua formatter
- Save button to persist changes
- Back to directory button
- Error handling for file operations

## PixelCraft Editor Debugging

### Current Issues Identified:
- **Board initialization**: Proxy shows `originalBoard exists: false` indicating the board setter is not being called
- **Image loading**: API call to `/api/get_texture?file=crab.png` is successful but image fails to load with `checkForImport: Error loading image`
- **Canvas access**: `"display", window.board.canvas.style is undefined` error indicates canvas properties can't be accessed
- **Flow issue**: The board is only created when user enters dimensions and clicks OK, but image loading happens before then

### Debugging Logs Analysis:
- ✅ Integration script executes properly (remixed logs appear)
- ✅ API endpoint works (200 OK for image fetch)
- ✅ PixelCraft UI fully initializes (board, canvas, ctx all true)
- ✅ Image fetch succeeds from `/api/get_texture?file=crab.png`
- ❌ Board's originalBoard is never set via setter
- ❌ Canvas access fails due to board not being properly initialized
- ❌ Image loading still fails despite successful API call

### Root Cause:
The PixelCraft editor needs to have dimensions entered before the board is created, but the image loading process tries to access canvas elements before the board exists.

### Key Debug Traces:
1. `Remixed Dungeon integration: Basic log test` - Shows integration starts
2. `Board property getter called, returning proxy` - Shows proxy mechanism works
3. `originalBoard exists: false` - Key issue: setter never called
4. `checkForImport: Error loading image` - Image loading fails
5. `can't access property "display", window.board.canvas.style is undefined` - Canvas access issue
6. `waitForPixelCraftAndLoad: PixelCraft is fully initialized, loading image and creating save button` - PixelCraft does initialize but image load still fails
7. `checkForImport: Fetch response received, ok: true status: 200` - API call succeeds
8. `checkForImport: Blob received, creating image` - Blob is received
9. `checkForImport: Error loading image` - Final error in image loading process

## PixelCraft Editor - ISSUE RESOLVED

### Current Status:
The PixelCraft editor has been successfully enhanced with improved image loading and editing capabilities:

- ✅ **Image Loading**: Images are now properly loaded based on URL parameters without requiring manual dimension entry
- ✅ **Automatic Board Creation**: Board dimensions automatically match loaded image dimensions (e.g., 256x16 for crab.png)
- ✅ **Proper Pixel Mapping**: Image pixels are correctly mapped to the internal board.data array
- ✅ **Pixel-Level Editing**: Individual pixels can be edited at the original resolution
- ✅ **Correct Saving**: Images save at original resolution (not zoomed-up canvas size)
- ✅ **API Integration**: Works with the `/api/get_texture` endpoint to load images directly
- ✅ **Visual Display**: Loaded images appear correctly in the editor at proper scale
- ✅ **Save Functionality**: Save functionality properly captures original pixel dimensions
- ✅ **UI Integration**: "SAVE TO MOD" button properly saves edited images back to the server

### Enhanced Features:
- **Skip Dimension Dialog**: When loading an image via `?edit_file=filename.png`, the dimension dialog is skipped
- **Auto-Board Creation**: Board is automatically created with dimensions matching the loaded image
- **Proper Pixel Data Sync**: Internal grid data and visual canvas are properly synchronized
- **Correct Resolution Saving**: Save functionality creates images at original resolution, not zoomed-up display resolution

### Endpoints Used:
- `/web/pixelcraft/?edit_file=filename.png` - Load and edit specific image directly
- `/api/get_texture?file=filename.png` - Fetch image data from server
- `/api/save_texture` - Save edited image back to server

### Implementation Strategy:
1. **JSON Parsing**: API returns JSON with base64-encoded image data, properly handled and decoded
2. **Temporary Canvas**: Used to properly extract pixel data from loaded images
3. **Data Synchronization**: Internal board data and visual canvas properly synchronized
4. **Save Override**: Save functionality overridden to maintain original resolution
5. **Event Handling**: Proper handling of image loading and board initialization sequence

## Quick Test Commands

```bash
# Build server
./gradlew -c settings.desktop.gradle :RemixedDungeonDesktop:webServerShadowJar

# Run server
cd RemixedDungeonDesktop/src/desktop/rundir
java -jar ../../../build/libs/RemixedDungeon-WebServer-headless.jar --webserver=8082 --mod=Remixed

# Test endpoints
curl http://localhost:8082/
curl http://localhost:8082/debug-list/scripts
curl http://localhost:8082/fs/scripts/
```

## Testing Prompt for New Sessions

To test the enhanced PixelCraft editor functionality:

1. **Clear browser console** (F12 → Console tab → Clear button)
2. **Navigate to** `http://localhost:8082/web/pixelcraft/?edit_file=crab.png`
3. **Observe** console logs to see execution flow
4. **Confirm** board automatically created with correct dimensions (e.g., 256x16 for crab.png)
5. **Verify** original image properly loaded and displayed in editor
6. **Test** pixel-level editing functionality
7. **Test** "SAVE TO MOD" button to save edited image
8. **Confirm** saved image maintains original resolution