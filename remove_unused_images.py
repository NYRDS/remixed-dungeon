#!/usr/bin/env python3
"""
Script to remove unused images from wiki-data/media/rpd/images
"""
import os
import shutil

def remove_unused_images():
    """
    Remove unused images from the wiki images directory
    """
    # Read the list of unused images from the file created earlier
    with open("/home/mike/StudioProjects/remixed-dungeon/unused_images.txt", "r") as f:
        unused_images = [line.strip() for line in f.readlines() if line.strip()]
    
    images_dir = "/home/mike/StudioProjects/remixed-dungeon/wiki-data/media/rpd/images"
    backup_dir = "/home/mike/StudioProjects/remixed-dungeon/wiki_images_backup"
    
    removed_count = 0
    error_count = 0
    
    print(f"Starting to remove {len(unused_images)} unused images...")
    
    for img in unused_images:
        img_path = os.path.join(images_dir, img)
        
        if os.path.exists(img_path):
            try:
                # First, copy to backup directory as safety measure
                backup_path = os.path.join(backup_dir, img)
                shutil.copy2(img_path, backup_path)
                
                # Then remove the original
                os.remove(img_path)
                print(f"REMOVED: {img}")
                removed_count += 1
            except Exception as e:
                print(f"ERROR removing {img}: {str(e)}")
                error_count += 1
        else:
            print(f"NOT FOUND (already removed?): {img}")
    
    print(f"\nSummary:")
    print(f"  - Images to remove: {len(unused_images)}")
    print(f"  - Successfully removed: {removed_count}")
    print(f"  - Errors: {error_count}")
    print(f"  - Backup location: {backup_dir}")

if __name__ == "__main__":
    remove_unused_images()