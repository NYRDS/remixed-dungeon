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

# Set the JAVA_HOME to the bundled JRE within the snap
export JAVA_HOME=$SNAP/usr/lib/jvm/java-17-openjdk-amd64
export PATH=$JAVA_HOME/bin:$PATH

exec java \
  --add-opens java.base/java.util=ALL-UNNAMED \
  -Dassets.dir="$SNAP/data" \
  -Duser.home="$SNAP_USER_DATA" \
  -jar "$JAR_FILE" \
  "$@"