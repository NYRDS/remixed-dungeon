#!/usr/bin/env python3
"""
Script to identify unused files in the wiki-data repository.
Checks for images in the media directory that aren't referenced in any wiki pages.
"""

import os
import re
from pathlib import Path

def find_unused_images_improved(wiki_data_dir):
    """Find images in the media directory that aren't referenced in any wiki pages."""
    
    # Get all image files in the media directory
    media_path = Path(wiki_data_dir) / 'media' / 'rpd' / 'images'
    image_files = set()
    
    for file_path in media_path.rglob('*'):
        if file_path.is_file() and file_path.suffix.lower() in ['.png', '.jpg', '.jpeg', '.gif', '.svg', '.bmp']:
            image_files.add(file_path.name)
    
    print(f"Found {len(image_files)} image files in media directory")
    
    # Get all referenced images in wiki pages with multiple pattern matching
    pages_path = Path(wiki_data_dir) / 'pages'
    referenced_images = set()
    
    for file_path in pages_path.rglob('*.txt'):
        if file_path.is_file():
            content = file_path.read_text(encoding='utf-8', errors='ignore')
            
            # Find all image references in wiki format
            # Pattern 1: {{ rpd:images:image_name.png|... }}, {{ ru:rpd:images:image_name.png|... }}, {{ cn:rpd:images:image_name.png|... }}, {{ es:rpd:images:image_name.png|... }}, {{ pt:rpd:images:image_name.png|... }}
            matches1 = re.findall(r'\{\{\s*(?:[a-zA-Z0-9_]+:)*rpd:images:([^\s|}]+)', content)

            # Pattern 2: {{rpd:images:image_name.png|...}} (without spaces), {{ru:rpd:images:image_name.png|...}}, {{es:rpd:images:image_name.png|...}}, {{pt:rpd:images:image_name.png|...}}
            matches2 = re.findall(r'\{\{(?:[a-zA-Z0-9_]+:)*rpd:images:([^\s|}]+)', content)

            # Pattern 3: {{ rpd:images:image_name.png}} (without alt text), {{ ru:rpd:images:image_name.png}}, {{ es:rpd:images:image_name.png}}, {{ pt:rpd:images:image_name.png}}
            matches3 = re.findall(r'\{\{\s*(?:[a-zA-Z0-9_]+:)*rpd:images:([^\s}]+)', content)
            
            # Combine all matches
            all_matches = matches1 + matches2 + matches3
            
            for match in all_matches:
                image_name = match.strip()
                # Sometimes the image might include the rpd:images: prefix, so take just the filename
                if '/' in image_name:
                    image_name = image_name.split('/')[-1]
                if ':' in image_name:
                    image_name = image_name.split(':')[-1]
                referenced_images.add(image_name)
    
    print(f"Found {len(referenced_images)} referenced images in wiki pages")
    
    # Identify unused images
    unused_images = image_files - referenced_images
    
    print(f"\nFound {len(unused_images)} unused images:")
    for img in sorted(unused_images):
        print(f"  - {img}")
    
    return list(unused_images)

def main():
    wiki_data_dir = "wiki-data"

    print("Looking for unused image files (improved detection)...")
    unused_images = find_unused_images_improved(wiki_data_dir)
    
    print(f"\nSUMMARY:")
    print(f"Unused images: {len(unused_images)}")
    
    # Some specific checks
    spell_icons_to_check = [
        'healing_spell_icon.png',
        'ignite_spell_icon.png',
        'wind_gust_spell_icon.png',
        'magic_torch_spell_icon.png',
        'root_spell_icon.png'
    ]
    
    print(f"\nChecking for specific spell icons that should be used:")
    for icon in spell_icons_to_check:
        if icon in unused_images:
            print(f"  - {icon}: NOT referenced (unexpected)")
        else:
            print(f"  - {icon}: IS referenced (expected)")
    
    return unused_images

if __name__ == "__main__":
    main()