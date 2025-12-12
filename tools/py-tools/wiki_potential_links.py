#!/usr/bin/env python3
"""
Script to identify wiki pages that may want to link to the current page.
This is based on semantic similarity between page names and content.
"""

import os
import re
from pathlib import Path
from difflib import SequenceMatcher


def find_all_wiki_pages(wiki_dir):
    """Find all wiki pages in the wiki directory."""
    pages = []
    for root, dirs, files in os.walk(wiki_dir):
        for file in files:
            if file.endswith('.txt'):
                pages.append(os.path.join(root, file))
    return pages


def extract_page_name(filepath):
    """Extract page name from file path (without extension)."""
    return Path(filepath).stem.lower()


def load_page_content(filepath):
    """Load content of a page."""
    with open(filepath, 'r', encoding='utf-8') as f:
        return f.read()


def extract_content_keywords(content):
    """Extract keywords from page content that might suggest related pages."""
    # Find words that could be related to other pages
    # Look for capitalized words, game terms, or references to other entities
    words = re.findall(r'\b([A-Z][a-z]+(?:[A-Z][a-z]*)*)\b', content)
    # Convert CamelCase to snake_case for comparison
    keywords = []
    for word in words:
        # Convert CamelCase to snake_case (e.g., "IceElemental" -> "ice_elemental")
        s1 = re.sub('(.)([A-Z][a-z]+)', r'\1_\2', word)
        snake_case = re.sub('([a-z0-9])([A-Z])', r'\1_\2', s1).lower()
        keywords.extend([snake_case, word.lower()])
    
    # Include individual lowercase words that might be meaningful
    content_lower = content.lower()
    additional_keywords = re.findall(r'\b[a-z_]{3,}\b', content_lower)
    keywords.extend(additional_keywords)
    
    # Remove duplicates and return
    return list(set(keywords))


def calculate_similarity(str1, str2):
    """Calculate similarity between two strings."""
    return SequenceMatcher(None, str1.lower(), str2.lower()).ratio()


def find_potential_linking_pages(target_page, wiki_dir, threshold=0.3):
    """Find pages that might want to link to the target page based on semantic similarity."""
    all_pages = find_all_wiki_pages(wiki_dir)
    target_page_name = extract_page_name(target_page)
    target_content = load_page_content(target_page)
    target_keywords = extract_content_keywords(target_content)
    
    potential_linkers = []
    
    for page in all_pages:
        page_name = extract_page_name(page)
        if page_name == target_page_name:
            continue
            
        content = load_page_content(page)
        content_keywords = extract_content_keywords(content)
        
        # Calculate similarity based on:
        # 1. Similarity between page names
        name_similarity = calculate_similarity(target_page_name, page_name)
        
        # 2. Overlap in content keywords
        keyword_overlap = len(set(target_keywords) & set(content_keywords))
        
        # 3. Check if target page name appears in content of other page
        content_mention_score = 0
        content_lower = content.lower()
        if target_page_name.replace('_', '') in content_lower.replace('_', ''):
            content_mention_score = 0.5
        
        # Combine scores to determine if page might want to link
        combined_score = max(name_similarity, keyword_overlap / max(len(target_keywords), len(content_keywords)) if target_keywords and content_keywords else 0) + content_mention_score
        
        if combined_score >= threshold:
            potential_linkers.append({
                'page': page_name,
                'score': combined_score,
                'name_similarity': name_similarity,
                'keyword_overlap': keyword_overlap
            })
    
    # Sort by score descending
    potential_linkers.sort(key=lambda x: x['score'], reverse=True)
    
    return potential_linkers


def find_all_potential_links(wiki_dir, threshold=0.3):
    """Find potential linking pages for all pages."""
    all_pages = find_all_wiki_pages(wiki_dir)
    all_potential_links = {}
    
    for page in all_pages:
        page_name = extract_page_name(page)
        potentials = find_potential_linking_pages(page, wiki_dir, threshold)
        all_potential_links[page_name] = potentials
    
    return all_potential_links


def print_potential_links_report(potential_links):
    """Print a report of all potential linking pages."""
    for page, potentials in potential_links.items():
        if potentials:
            print(f"\nPages that may want to link to '{page}':")
            for potential in potentials[:5]:  # Show top 5 suggestions
                print(f"  - {potential['page']} (score: {potential['score']:.2f}, "
                      f"name similarity: {potential['name_similarity']:.2f}, "
                      f"keyword overlap: {potential['keyword_overlap']})")
        else:
            print(f"\nNo potential linking pages found for '{page}'")


def save_potential_links_to_file(potential_links, output_file):
    """Save potential links report to a file."""
    with open(output_file, 'w', encoding='utf-8') as f:
        for page, potentials in potential_links.items():
            f.write(f"\nPages that may want to link to '{page}':\n")
            if potentials:
                for potential in potentials[:5]:  # Show top 5 suggestions
                    f.write(f"  - {potential['page']} (score: {potential['score']:.2f}, "
                            f"name similarity: {potential['name_similarity']:.2f}, "
                            f"keyword overlap: {potential['keyword_overlap']})\n")
            else:
                f.write("  (no potential linking pages)\n")
    
    print(f"Potential links report saved to {output_file}")


if __name__ == "__main__":
    wiki_data_dir = "wiki-data/pages"

    if not os.path.exists(wiki_data_dir):
        print(f"Wiki directory {wiki_data_dir} not found. Looking for .txt files in current directory...")
        wiki_data_dir = "."
    
    print("Finding potential linking pages...")
    potential_links = find_all_potential_links(wiki_data_dir)
    
    print("\nPotential Links Report:")
    print("="*50)
    print_potential_links_report(potential_links)
    
    # Save to file as well
    save_potential_links_to_file(potential_links, "wiki_potential_links_report.txt")