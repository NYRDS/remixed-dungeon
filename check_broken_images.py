#!/usr/bin/env python3
"""
Script to find broken image references in wiki pages
"""
import os
import re

def find_broken_image_refs(wiki_pages_dir, wiki_images_dir):
    """
    Find image references in wiki pages that don't match existing image files
    """
    broken_refs = []
    
    # Get all image filenames in the images directory
    image_files = set(os.listdir(wiki_images_dir))
    
    # Walk through all text files in the wiki pages directory
    for root, dirs, files in os.walk(wiki_pages_dir):
        for file in files:
            if file.endswith('.txt'):
                file_path = os.path.join(root, file)
                
                with open(file_path, 'r', encoding='utf-8') as f:
                    content = f.read()
                    
                # Find all image references using regex
                # Pattern matches {{ rpd:images:imagename.png|alt text }} or similar
                image_refs = re.findall(r'\{\{\s*rpd:images:([a-zA-Z0-9_.-]+)\s*(?:\|(.*?))?\s*\}\}', content)
                
                for image_ref, alt_text in image_refs:
                    # Check if the referenced image exists
                    if image_ref not in image_files:
                        broken_refs.append((file_path, image_ref))
                        print(f"BROKEN: {file_path} references {image_ref}")
    
    return broken_refs

if __name__ == "__main__":
    wiki_pages_dir = "/home/mike/StudioProjects/remixed-dungeon/wiki-data/pages/rpd"
    wiki_images_dir = "/home/mike/StudioProjects/remixed-dungeon/wiki-data/media/rpd/images"
    
    broken_refs = find_broken_image_refs(wiki_pages_dir, wiki_images_dir)
    print(f"\nFound {len(broken_refs)} broken image references")