#!/bin/bash

# Wrapper script to launch Remixed Dungeon
# This script is copied to the snap during the build process

# Set up the application environment
export ASSETS_DIR="$SNAP/data"

# Find the JAR file
JAR_FILE=$(find $SNAP/bin -name "remixed-dungeon.jar" -type f)

if [ -z "$JAR_FILE" ]; then
  echo "Error: Could not find Remixed Dungeon JAR file"
  exit 1
fi

# Use the Java from the snap's stage packages
export JAVA_HOME="$SNAP/usr/lib/jvm/java-17-openjdk-amd64"
export PATH="$JAVA_HOME/bin:$PATH"

# GPU: The snap layout maps /usr/lib/x86_64-linux-gnu/dri -> $SNAP/.../dri
# so Mesa's hardcoded search path finds the bundled DRI drivers automatically.
export __EGL_VENDOR_LIBRARY_DIRS=$SNAP/usr/share/glvnd/egl_vendor.d

# Audio: Use PulseAudio via ALSA plugin (alsa-launch already sets up asound.conf)
export ALSA_CONFIG_PATH=$SNAP/etc/asound.conf

# LWJGL: Extract natives to a writable directory inside snap confinement
NATIVES_DIR="$SNAP_USER_DATA/.lwjgl-natives"
mkdir -p "$NATIVES_DIR"
export LWJGL_EXTRACT_DIR="$NATIVES_DIR"

# Temp dir must also be writable
export TMPDIR="$SNAP_USER_DATA/tmp"
mkdir -p "$TMPDIR"

# The game resolves assets relative to CWD via Gdx.files.internal()
# Search paths include: data/mods/Remixed/, mods/Remixed/, ./
# CWD must be the snap root so data/mods/Remixed/ resolves correctly
cd "$SNAP"

java \
  --add-opens java.base/java.util=ALL-UNNAMED \
  -Dassets.dir="$SNAP/data" \
  -Duser.home="$SNAP_USER_DATA" \
  -Djava.library.path="$NATIVES_DIR:$SNAP/usr/lib/x86_64-linux-gnu:$SNAP/usr/lib/jni:$SNAP/usr/lib/jvm/java-17-openjdk-amd64/lib" \
  -Djava.io.tmpdir="$TMPDIR" \
  -Dorg.lwjgl.librarypath="$NATIVES_DIR" \
  -Dsun.java2d.opengl=true \
  -Dsun.java2d.xrender=true \
  -jar "$JAR_FILE" \
  --windowed \
  "$@"

EXIT_CODE=$?
if [ $EXIT_CODE -ne 0 ]; then
  echo "Remixed Dungeon exited with code $EXIT_CODE."
fi
exit $EXIT_CODE