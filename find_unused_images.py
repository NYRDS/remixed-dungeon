#!/usr/bin/env python3
"""
Script to identify all images currently referenced in wiki pages
and compare with images in the wiki directory to find unused ones
"""
import os
import re

def get_referenced_images(wiki_pages_dir):
    """
    Extract all image references from wiki pages
    """
    referenced_images = set()
    
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
                    referenced_images.add(image_ref)
    
    return referenced_images

def get_all_images_in_directory(images_dir):
    """
    Get all image files in the images directory
    """
    all_images = set()
    
    for file in os.listdir(images_dir):
        if file.endswith('.png'):
            all_images.add(file)
    
    return all_images

def find_unused_images(referenced_images, all_images):
    """
    Find images in the directory that are not referenced in any wiki page
    """
    unused_images = []
    
    for image in all_images:
        if image not in referenced_images:
            unused_images.append(image)
    
    return unused_images

if __name__ == "__main__":
    wiki_pages_dir = "/home/mike/StudioProjects/remixed-dungeon/wiki-data/pages/rpd"
    images_dir = "/home/mike/StudioProjects/remixed-dungeon/wiki-data/media/rpd/images"
    
    print("Getting referenced images from wiki pages...")
    referenced_images = get_referenced_images(wiki_pages_dir)
    print(f"Found {len(referenced_images)} referenced images")
    
    print("\nGetting all images in directory...")
    all_images = get_all_images_in_directory(images_dir)
    print(f"Found {len(all_images)} images in directory")
    
    print("\nFinding unused images...")
    unused_images = find_unused_images(referenced_images, all_images)
    print(f"Found {len(unused_images)} unused images")
    
    if unused_images:
        print("\nUnused images:")
        for img in sorted(unused_images):
            print(f"  - {img}")
    else:
        print("\nNo unused images found!")
    
    # Save the list of unused images to a file
    with open("/home/mike/StudioProjects/remixed-dungeon/unused_images.txt", "w") as f:
        for img in sorted(unused_images):
            f.write(img + "\n")
    
    print(f"\nList of unused images saved to /home/mike/StudioProjects/remixed-dungeon/unused_images.txt")