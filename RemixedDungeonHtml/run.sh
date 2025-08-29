#!/bin/bash

# Script to run the HTML version in superdev mode

echo "Starting Remixed Dungeon HTML superdev server..."

# Generate build config
./gradlew :RemixedDungeonHtml:generateBuildConfig

# Start superdev server
./gradlew :RemixedDungeonHtml:superDev

echo "Superdev server started!"
echo "Open http://localhost:8080 in your browser to play"