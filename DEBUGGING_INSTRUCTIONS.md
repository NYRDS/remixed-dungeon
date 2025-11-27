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