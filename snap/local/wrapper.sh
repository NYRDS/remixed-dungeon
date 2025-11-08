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

# Use the system Java from the snap's stage packages
export PATH=$SNAP/usr/lib/jvm/java-17-openjdk-amd64/bin:$PATH

# Audio environment setup for ALSA
export ALSA_PCM_CARD=0
export ALSA_PCM_DEVICE=0
export PULSE_SERVER="unix:/run/user/$(id -u)/pulse/native"

# Try to set up ALSA configuration if available
if [ -f "/usr/share/alsa/alsa.conf" ]; then
    export ALSA_CONFIG_PATH="/usr/share/alsa/alsa.conf"
fi

exec java \
  --add-opens java.base/java.util=ALL-UNNAMED \
  -Dassets.dir="$SNAP/data" \
  -Duser.home="$SNAP_USER_DATA" \
  -Djava.library.path="$SNAP/usr/lib/x86_64-linux-gnu:$SNAP/usr/lib/jni:$SNAP/usr/lib/jvm/java-17-openjdk-amd64/lib" \
  -Djavax.sound.sampled.AudioSystem.provider="com.sun.media.sound.DirectAudioDeviceProvider" \
  -Dsun.java2d.opengl=true \
  -Dsun.java2d.xrender=true \
  -jar "$JAR_FILE" \
  "$@"