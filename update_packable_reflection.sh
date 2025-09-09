#!/bin/bash
# Script to update ReflectionConfig.java with classes that use @Packable annotation

echo "Updating ReflectionConfig.java with @Packable classes..."

# Run the Python script
python3 /home/mike/StudioProjects/remixed-dungeon/update_packable_reflection_config.py

echo "Done!"