#!/bin/bash

# Build script for Remixed Dungeon HTML version

echo "Building Remixed Dungeon HTML version..."

# Generate build config
./gradlew :RemixedDungeonHtml:generateBuildConfig

echo "Build configuration generated."
echo "Note: The HTML build is not yet fully implemented."
echo "To fully implement it, HTML-specific platform abstractions need to be created."