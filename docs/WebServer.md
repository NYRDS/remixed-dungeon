# WebServer Functionality Documentation

The WebServer is an Android-only feature that provides a web-based interface for managing mod files in Remixed Dungeon. This functionality is particularly useful for users who want to easily transfer files to and from their Android device without needing to use ADB or file explorer applications.

## Overview

The WebServer runs on port 8080 and provides a simple web interface for:
- Browsing files in the active mod
- Downloading files from the device
- Uploading files to the active mod (with security restrictions)

This feature is especially valuable for modders and advanced users who want to quickly test changes to their mods without complex file transfer procedures.

## Enabling the WebServer

Unlike what was previously documented, the WebServer is not enabled by uncommenting code in `RemixedDungeonApp.java`. Instead, it's enabled through a hidden feature in the AboutScene:

1. Navigate to the About section in the game (usually accessible from the main menu)
2. Find the small icon next to the main logo (it's a very faint item icon)
3. Click this icon 4 times in quick succession
4. If successful, you'll see a toast message "dev mode enabled" followed by "WebServer started on port 8080"

This hidden method was implemented to prevent casual users from accidentally enabling the WebServer while still making it accessible to developers and advanced users who need it.

## Web Interface

Once the WebServer is running, you can access it by navigating to `http://[device-ip]:8080/` in a web browser on the same network. The IP address of the device will be displayed in the AboutScene once the WebServer is started.

### Main Dashboard

The main dashboard provides an overview of the current game state and quick access to the main features:
- Game version and active mod information
- Links to file browsing and upload functionality

### File Browser

The file browser allows you to:
- Navigate through directories in the active mod
- Download any file by clicking on its link
- Browse the file structure of your mod
- Upload files to any directory using the upload links provided

### File Upload

The upload interface allows you to:
- Select files from your computer to upload to the active mod
- Specify a subdirectory path for organizing uploaded files
- Receive confirmation when uploads are successful

## Security Features

### Main Mod Protection

For security reasons, file uploads to the main "Remixed" mod are disabled. This prevents accidental or malicious modification of core game files. Users can only upload files to custom mods.

### Path Sanitization

The WebServer implements path sanitization to prevent directory traversal attacks. Dangerous path sequences like `../` are automatically removed from file paths.

## Technical Implementation

The WebServer is built using NanoHTTPD, a lightweight HTTP server library. It integrates with the existing modding system to provide seamless access to mod files.

Key implementation details:
- Runs on port 8080
- Uses the existing `ModdingMode` system to determine the active mod
- Leverages `FileSystem` utilities for file operations
- Implements proper error handling and logging

The WebServer implementation can be found in:
- `/RemixedDungeon/src/android/java/com/nyrds/platform/app/WebServer.java` (Android implementation)
- `/RemixedDungeonDesktop/src/libgdx/java/com/nyrds/platform/app/WebServer.java` (Desktop placeholder)

### Directory Detection Fix

A recent fix was implemented to properly handle directory detection for both the main "Remixed" mod (which stores files as APK assets) and third-party mods (which store files in external storage):

1. For the main "Remixed" mod, directories are detected by checking if asset paths can be listed
2. For third-party mods, directories are detected by checking the file system
3. This ensures both mod types display their directory structure correctly in the web interface

## Use Cases

### Mod Development

Modders can use the WebServer to:
- Quickly transfer updated script files to their Android device
- Test changes without needing to rebuild and reinstall the entire application
- Debug mod issues by examining file contents directly

### Content Creation

Content creators can:
- Transfer custom graphics and sound files to their device
- Organize their mod files through the web interface
- Share mod files with other users

### Troubleshooting

Advanced users can use the WebServer to:
- Examine game logs and configuration files
- Backup important game data
- Diagnose mod-related issues

## Limitations

1. **Android Only**: This feature is only available on Android devices as desktop users can directly access the file system.
2. **Network Required**: Both the Android device and the computer need to be on the same network.
3. **Performance**: Large file transfers may be slower than direct file system access.
4. **Security**: The WebServer should only be used on trusted networks as it provides file system access.
5. **Hidden Feature**: The WebServer is not obvious to enable, requiring knowledge of the hidden button in AboutScene.

## Accessing the WebServer

To access the WebServer once it's running:
1. Ensure your Android device and computer are on the same network
2. Find the IP address displayed in the AboutScene after enabling the WebServer
3. Access `http://[device-ip]:8080/` from a web browser
4. Use the web interface to browse and manage files

Note: The WebServer is not enabled in official builds by default, but can be enabled through the hidden button method described above. This design choice balances accessibility for developers with security for general users.