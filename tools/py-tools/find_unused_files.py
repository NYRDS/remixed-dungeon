#!/usr/bin/env python3
"""
Script to identify unused files in the wiki-data repository.
Checks for images in the media directory that aren't referenced in any wiki pages.
"""

import os
import re
from pathlib import Path

def find_unused_images(wiki_data_dir):
    """Find images in the media directory that aren't referenced in any wiki pages."""
    
    # Get all image files in the media directory
    media_path = Path(wiki_data_dir) / 'media' / 'rpd' / 'images'
    image_files = set()
    
    for file_path in media_path.rglob('*'):
        if file_path.is_file() and file_path.suffix.lower() in ['.png', '.jpg', '.jpeg', '.gif', '.svg', '.bmp']:
            image_files.add(file_path.name)
    
    print(f"Found {len(image_files)} image files in media directory")
    
    # Get all referenced images in wiki pages
    pages_path = Path(wiki_data_dir) / 'pages'
    referenced_images = set()
    
    for file_path in pages_path.rglob('*.txt'):
        if file_path.is_file():
            content = file_path.read_text(encoding='utf-8', errors='ignore')
            
            # Find all image references in wiki format
            # Looking for patterns like {{ rpd:images:image_name.png|... }}
            matches = re.findall(r'{{\s*rpd:images:([^|}]+)', content)
            
            for match in matches:
                image_name = match.strip()
                # Sometimes the image might include additional formatting, so take just the filename
                if '/' in image_name:
                    image_name = image_name.split('/')[-1]
                referenced_images.add(image_name)
    
    print(f"Found {len(referenced_images)} referenced images in wiki pages")
    
    # Identify unused images
    unused_images = image_files - referenced_images
    
    print(f"\nFound {len(unused_images)} unused images:")
    for img in sorted(unused_images):
        print(f"  - {img}")
    
    return list(unused_images)

def find_unused_pages(wiki_data_dir):
    """Find wiki pages that might not be linked from other pages."""
    
    pages_path = Path(wiki_data_dir) / 'pages'
    all_pages = set()
    linked_pages = set()
    
    # Extract all page names
    for file_path in pages_path.rglob('*.txt'):
        if file_path.is_file():
            # Get relative path from pages directory
            rel_path = file_path.relative_to(pages_path)
            # Convert to wiki page reference format (without .txt extension)
            page_ref = str(rel_path).replace('.txt', '').replace(os.sep, ':')
            all_pages.add(page_ref)
            
            # Also add with 'rpd:' prefix if it's in the rpd namespace
            if page_ref.startswith('rpd:'):
                all_pages.add(page_ref[4:])  # Without rpd: prefix
    
    print(f"Found {len(all_pages)} total pages")
    
    # Find all linked pages by looking for wiki links
    for file_path in pages_path.rglob('*.txt'):
        if file_path.is_file():
            content = file_path.read_text(encoding='utf-8', errors='ignore')
            
            # Find all wiki links (in format [[namespace:page_name|Display Text]])
            matches = re.findall(r'\[\[([^\]|]+)', content)
            
            for match in matches:
                # Remove namespace prefix and display text if present
                if '|' in match:
                    link = match.split('|')[0]
                else:
                    link = match
                
                # Clean up the link
                link = link.strip()
                linked_pages.add(link)
                
                # If it has a namespace, also add the name without namespace
                if ':' in link:
                    linked_pages.add(link.split(':', 1)[1])
    
    print(f"Found {len(linked_pages)} referenced pages in wiki links")
    
    # Some links might be to non-wiki pages, so filter to only actual pages
    actual_linked = set()
    for link in linked_pages:
        if link in all_pages or f"rpd:{link}" in all_pages:
            actual_linked.add(link)
    
    print(f"Found {len(actual_linked)} referenced pages that exist")
    
    # Pages that aren't linked from any other page
    unlinked_pages = all_pages - set(actual_linked)
    
    # Filter out namespace directories (like 'rpd')
    actual_unlinked_pages = []
    for page in unlinked_pages:
        # Check if this is a real page file that exists
        page_file = pages_path / f"{page.replace(':', os.sep)}.txt"
        if page_file.exists():
            actual_unlinked_pages.append(page)
    
    print(f"\nFound {len(actual_unlinked_pages)} unlinked pages:")
    for page in sorted(actual_unlinked_pages):
        print(f"  - {page}")
    
    return actual_unlinked_pages

def main():
    wiki_data_dir = "/home/mike/StudioProjects/remixed-dungeon_fix/wiki-data"
    
    print("Looking for unused image files...")
    unused_images = find_unused_images(wiki_data_dir)
    
    print("\n" + "="*60 + "\n")
    
    print("Looking for unlinked pages...")
    unlinked_pages = find_unused_pages(wiki_data_dir)
    
    print("\n" + "="*60 + "\n")
    
    print(f"SUMMARY:")
    print(f"Unused images: {len(unused_images)}")
    print(f"Unlinked pages: {len(unlinked_pages)}")
    
    return unused_images, unlinked_pages

if __name__ == "__main__":
    main()