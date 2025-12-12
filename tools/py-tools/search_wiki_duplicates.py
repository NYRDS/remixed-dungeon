#!/usr/bin/env python3
"""
Script to search for possible duplicate wiki pages based on name and content.
The tool looks for pages with similar names and overlapping content.
"""

import os
import re
from difflib import SequenceMatcher
from pathlib import Path


def normalize_name(name):
    """
    Normalize a wiki page name for comparison by removing extensions and underscores.
    """
    # Remove .txt extension if present
    if name.endswith('.txt'):
        name = name[:-4]
    
    # Convert to lowercase and remove underscores
    normalized = name.lower().replace('_', '')
    return normalized


def calculate_similarity(text1, text2):
    """
    Calculate similarity ratio between two text strings.
    """
    return SequenceMatcher(None, text1, text2).ratio()


def find_similar_names(page_names):
    """
    Find pairs of page names that are similar (case-insensitive, ignoring underscores).
    """
    similar_pairs = []
    
    for i, name1 in enumerate(page_names):
        norm_name1 = normalize_name(name1)
        
        for j, name2 in enumerate(page_names[i+1:], i+1):
            norm_name2 = normalize_name(name2)
            
            # Check if names are similar when normalized
            if norm_name1 == norm_name2:
                similar_pairs.append((name1, name2))
                
            # Also check for partial matches (e.g., 'air_elemental' vs 'airelemental')
            elif (norm_name1 in norm_name2 or norm_name2 in norm_name1) and len(norm_name1) > 3 and len(norm_name2) > 3:
                similar_pairs.append((name1, name2))
    
    return similar_pairs


def find_content_overlaps(pages_dir, threshold=0.5):
    """
    Find pages with overlapping content based on text similarity.
    """
    content_overlaps = []
    page_files = [f for f in os.listdir(pages_dir) if f.endswith('.txt')]
    
    # Dictionary to store content of each file
    page_contents = {}
    
    for filename in page_files:
        filepath = os.path.join(pages_dir, filename)
        try:
            with open(filepath, 'r', encoding='utf-8') as f:
                content = f.read()
                page_contents[filename] = content
        except Exception as e:
            print(f"Error reading {filepath}: {e}")
    
    # Compare each pair of pages
    for i, name1 in enumerate(page_files):
        for j, name2 in enumerate(page_files[i+1:], i+1):
            content1 = page_contents[name1]
            content2 = page_contents[name2]
            
            similarity = calculate_similarity(content1, content2)
            
            if similarity >= threshold:
                content_overlaps.append((name1, name2, similarity))
    
    return content_overlaps


def find_duplicate_candidates(wiki_pages_dir):
    """
    Main function to find possible duplicate wiki pages.
    """
    page_files = [f for f in os.listdir(wiki_pages_dir) if f.endswith('.txt')]
    
    print("Searching for wiki page duplicates...")
    print(f"Found {len(page_files)} wiki pages to analyze\n")
    
    # Find similar names
    print("Checking for similar page names:")
    similar_names = find_similar_names(page_files)
    
    if similar_names:
        for name1, name2 in similar_names:
            print(f"  Possible name duplicate: {name1} <-> {name2}")
    else:
        print("  No similar names found.")
    
    print()
    
    # Find content overlaps
    print("Checking for content overlaps (threshold: 50%):")
    content_overlaps = find_content_overlaps(wiki_pages_dir, threshold=0.5)
    
    if content_overlaps:
        for name1, name2, similarity in content_overlaps:
            print(f"  Content overlap: {name1} <-> {name2} ({similarity:.2f})")
    else:
        print("  No significant content overlaps found.")
    
    print()
    
    # Extra check for content overlaps with lower threshold
    print("Checking for potential content overlaps (lower threshold: 30%):")
    potential_overlaps = find_content_overlaps(wiki_pages_dir, threshold=0.3)
    
    # Filter out the ones already found with higher threshold
    filtered_overlaps = [(n1, n2, sim) for n1, n2, sim in potential_overlaps 
                         if not any((n1 == nn1 and n2 == nn2) for nn1, nn2, _ in content_overlaps)]
    
    if filtered_overlaps:
        for name1, name2, similarity in filtered_overlaps:
            print(f"  Potential content overlap: {name1} <-> {name2} ({similarity:.2f})")
    else:
        print("  No potential content overlaps found.")
    
    # Summary
    print(f"\nSummary:")
    print(f"  Similar names found: {len(similar_names)}")
    print(f"  Content overlaps found (50%+): {len(content_overlaps)}")
    print(f"  Potential content overlaps (30%+): {len(filtered_overlaps)}")


def main():
    # Default path to wiki pages directory
    wiki_pages_dir = "/home/mike/StudioProjects/remixed-dungeon/wiki-data/pages/rpd"
    
    if not os.path.isdir(wiki_pages_dir):
        print(f"Wiki pages directory not found: {wiki_pages_dir}")
        return
    
    find_duplicate_candidates(wiki_pages_dir)


if __name__ == "__main__":
    main()