# WebServer Functionality Documentation

The WebServer is an Android-only feature that provides a web-based interface for managing mod files in Remixed Dungeon. This functionality is particularly useful for users who want to easily transfer files to and from their Android device without needing to use ADB or file explorer applications.

## Overview

The WebServer runs on port 8080 and provides a simple web interface for:
- Browsing files in the active mod
- Downloading files from the device
- Uploading files to the active mod (with security restrictions)

This feature is especially valuable for modders and advanced users who want to quickly test changes to their mods without complex file transfer procedures.

## Starting the WebServer

The WebServer needs to be enabled in the code. Currently, the instantiation code is commented out in `RemixedDungeonApp.java`:

```java
/*            WebServer server = new WebServer(8080);
            try {
                server.start();
            } catch (IOException e) {
                EventCollector.logException(e,"WebServer");
            }
*/
```

To enable the WebServer, remove the comment markers and rebuild the application.

## Web Interface

Once the WebServer is running, you can access it by navigating to `http://[device-ip]:8080/` in a web browser on the same network.

### Main Dashboard

The main dashboard provides an overview of the current game state and quick access to the main features:
- Game version and active mod information
- Links to file browsing and upload functionality

### File Browser

The file browser allows you to:
- Navigate through directories in the active mod
- Download any file by clicking on its link
- Browse the file structure of your mod

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

## Enabling the WebServer

To enable the WebServer in a custom build:
1. Uncomment the WebServer instantiation code in `RemixedDungeonApp.java`
2. Rebuild the application
3. Launch the game on an Android device
4. Find the device's IP address on the network
5. Access `http://[device-ip]:8080/` from a web browser

Note: The WebServer is not enabled in official builds for security reasons.