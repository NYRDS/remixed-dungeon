# Remixed Dungeon Snap Build Documentation

This document provides instructions for building and publishing a snap package for the Remixed Dungeon desktop application.

## Prerequisites

To build the snap package for Remixed Dungeon, you will need:

1. **Snapcraft**: The official tool for building snaps
2. **LXD**: Used for building snaps in a clean environment
3. **Java 17**: Required for building the application

### Installing Prerequisites

```bash
# Install snapcraft
sudo snap install snapcraft --classic

# Install LXD for building in containers
sudo snap install lxd

# Initialize LXD (first time only)
lxd init --minimal
```

## Building the Snap

### Step 1: Prepare the Project

Ensure you're in the root directory of the Remixed Dungeon project:

```bash
cd /path/to/remixed-dungeon
```

### Step 2: Build the Snap Package

Run the following command to build the snap:

```bash
snapcraft
```

This will:
1. Build the Remixed Dungeon desktop application using Gradle
2. Package all necessary assets and dependencies
3. Create a runnable snap package

### Step 3: Install the Built Snap (for testing)

After the build completes, you can install the snap locally for testing:

```bash
sudo snap install --dangerous remixed-dungeon_*.snap
```

## Snap Configuration Details

The snap package is configured in `snap/snapcraft.yaml`:

- **Name**: remixed-dungeon
- **Version**: 32.3.alpha.11 (from version.properties)
- **Base**: core22
- **Confinement**: strict
- **Architectures**: amd64

### Dependencies

The snap includes:

- OpenJDK 17 JRE
- LibGDX native dependencies (libasound2, libgtk-3-0, libgl1, libglu1-mesa, libopenal1, libflac8, libvorbisfile3, libudev1, libfreetype6, libfontconfig1)

### Plugs (Permissions)

The application requires the following permissions:

- `desktop`: Access to desktop environment
- `wayland`: Wayland display access
- `x11`: X11 display access
- `opengl`: OpenGL graphics access
- `audio-playback`: Audio output
- `network`: Network access
- `home`: Access to user home directory for saves
- `removable-media`: Access to removable storage for mods

## Running the Application

After installation, run the game with:

```bash
remixed-dungeon
```

## Publishing to Snap Store

### Step 1: Register Snap Name

Register the name in the Snap Store:

```bash
snapcraft register remixed-dungeon
```

### Step 2: Build and Upload

```bash
# Build the snap
snapcraft

# Upload to store
snapcraft upload remixed-dungeon_*.snap
```

### Step 3: Release to Channel

```bash
# Release to a specific channel (e.g., stable, candidate, beta, edge)
snapcraft release remixed-dungeon <revision> stable
```

## Troubleshooting

### Build Issues

If the build fails due to memory constraints, you might need to increase memory limits for LXD containers.

### Runtime Issues

If the application fails to start:

1. Check that all required plugs are included in the snap
2. Verify that the wrapper script has proper permissions
3. Ensure all native libraries are included in stage-packages

### Testing the Snap Build

You can also build without LXD using the --destructive-mode flag if needed:

```bash
snapcraft --destructive-mode
```

Note: This runs the build directly on the host system and should be used with caution.

## Version Management

The snap version is automatically taken from the `version.properties` file in the project root. Update this file to change the version that will be built into the snap.

## Assets and Data

The snap includes:
- Game assets from `RemixedDungeonDesktop/src/desktop/assets`
- Default data from `RemixedDungeonDesktop/src/desktop/d_assets`
- Localization files from `RemixedDungeonDesktop/src/desktop/l10ns`

These are placed in the user's data directory when the application runs.

## Wrapper Script

The snap uses a wrapper script that:
- Sets the proper Java environment
- Configures JVM options needed for the game
- Sets the assets directory to the snap's data location
- Launches the application JAR