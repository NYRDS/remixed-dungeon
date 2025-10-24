#!/bin/bash

# Script to update snapcraft.yaml with version from version.properties
set -e

echo "Updating snapcraft.yaml with version from version.properties..."

# Get the directory where this script is located
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

# Read the version name from version.properties (two levels up from snap/local)
VERSION_NAME=$(grep '^VERSION_NAME=' "${SCRIPT_DIR}/../../version.properties" | cut -d'=' -f2-)

if [ -z "$VERSION_NAME" ]; then
    echo "Error: Could not extract version from version.properties"
    exit 1
fi

echo "Found version: $VERSION_NAME"

# Create a temporary file for the updated content
temp_file=$(mktemp)

# Read through the original file (one level up from snap/local)
while IFS= read -r line; do
    if [[ $line =~ ^version:\  ]]; then
        echo "version: '$VERSION_NAME'" >> "$temp_file"
    else
        echo "$line" >> "$temp_file"
    fi
done < "${SCRIPT_DIR}/../snapcraft.yaml"

# Move the temporary file to replace the original
mv "$temp_file" "${SCRIPT_DIR}/../snapcraft.yaml"

echo "Successfully updated snapcraft.yaml with version: $VERSION_NAME"