#!/usr/bin/env python3
"""
Script to identify wiki pages that may want to link to the current page.
This is based on semantic similarity between page names and content.
Optimized version with caching and faster algorithms.
"""

import os
import re
import random
import argparse
from pathlib import Path
from collections import defaultdict


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
    keywords = set()
    for word in words:
        # Convert CamelCase to snake_case (e.g., "IceElemental" -> "ice_elemental")
        s1 = re.sub('(.)([A-Z][a-z]+)', r'\1_\2', word)
        snake_case = re.sub('([a-z0-9])([A-Z])', r'\1_\2', s1).lower()
        keywords.add(snake_case)
        keywords.add(word.lower())

    # Include individual lowercase words that might be meaningful
    content_lower = content.lower()
    additional_keywords = re.findall(r'\b[a-z_]{3,}\b', content_lower)
    keywords.update(additional_keywords)

    return keywords


def jaccard_similarity(set1, set2):
    """Calculate Jaccard similarity between two sets."""
    if not set1 and not set2:
        return 1.0
    if not set1 or not set2:
        return 0.0

    intersection = len(set1 & set2)
    union = len(set1 | set2)
    return intersection / union


def simple_string_similarity(str1, str2):
    """Calculate a simple similarity between two strings based on common characters."""
    if not str1 or not str2:
        return 0.0
    if str1 == str2:
        return 1.0

    # More efficient similarity calculation than SequenceMatcher
    # Use a simple approach: longest common substring ratio
    len1, len2 = len(str1), len(str2)
    max_len = max(len1, len2)
    if max_len == 0:
        return 1.0

    # Find length of longest common subsequence (simplified)
    # Using a faster approximation
    common_chars = set(str1) & set(str2)
    max_common = len(common_chars)
    return max_common / max(len1, len2, 1)


def find_potential_linking_pages(target_page, wiki_dir, page_cache, keyword_cache, threshold=0.3):
    """Find pages that might want to link to the target page based on semantic similarity."""
    all_pages = list(page_cache.keys())
    target_page_name = extract_page_name(target_page)
    target_content = page_cache[target_page]
    target_keywords = keyword_cache[target_page]

    potential_linkers = []
    seen_pages = set()  # Track pages by name to avoid duplicates

    for page in all_pages:
        page_name = extract_page_name(page)
        if page_name == target_page_name:
            continue

        # Skip if we've already processed a page with this name
        if page_name in seen_pages:
            continue

        content = page_cache[page]
        content_keywords = keyword_cache[page]

        # Calculate similarity based on:
        # 1. Similarity between page names
        name_similarity = simple_string_similarity(target_page_name, page_name)

        # 2. Overlap in content keywords using Jaccard similarity
        keyword_similarity = jaccard_similarity(target_keywords, content_keywords)

        # 3. Check if target page name appears in content of other page
        content_mention_score = 0
        content_lower = content.lower()
        target_name_clean = target_page_name.replace('_', '')
        if target_name_clean in content_lower.replace('_', ''):
            content_mention_score = 0.5

        # Combine scores to determine if page might want to link
        combined_score = max(name_similarity, keyword_similarity) + content_mention_score

        if combined_score >= threshold:
            potential_linkers.append({
                'page': page_name,
                'score': combined_score,
                'name_similarity': name_similarity,
                'keyword_similarity': keyword_similarity
            })
            seen_pages.add(page_name)  # Mark this page name as processed

    # Sort by score descending
    potential_linkers.sort(key=lambda x: x['score'], reverse=True)

    return potential_linkers


def find_all_potential_links(wiki_dir, threshold=0.3):
    """Find potential linking pages for all pages with caching."""
    all_pages = find_all_wiki_pages(wiki_dir)

    # Pre-load all page content to avoid repeated file reads
    page_cache = {}
    keyword_cache = {}

    for page in all_pages:
        content = load_page_content(page)
        page_cache[page] = content
        keyword_cache[page] = extract_content_keywords(content)

    all_potential_links = {}

    for page in all_pages:
        page_name = extract_page_name(page)
        potentials = find_potential_linking_pages(page, wiki_dir, page_cache, keyword_cache, threshold)
        all_potential_links[page_name] = potentials

    return all_potential_links


def find_potential_links_for_page(target_page_name, wiki_dir, threshold=0.3):
    """Find potential linking pages for a specific page."""
    all_pages = find_all_wiki_pages(wiki_dir)

    # Find the target page file path
    target_page_path = None
    for page_path in all_pages:
        if extract_page_name(page_path) == target_page_name.lower():
            target_page_path = page_path
            break

    if not target_page_path:
        print(f"Page '{target_page_name}' not found in wiki directory.")
        return None

    # Pre-load all page content to avoid repeated file reads
    page_cache = {}
    keyword_cache = {}

    for page in all_pages:
        content = load_page_content(page)
        page_cache[page] = content
        keyword_cache[page] = extract_content_keywords(content)

    # Find potential linking pages for the target page
    potentials = find_potential_linking_pages(target_page_path, wiki_dir, page_cache, keyword_cache, threshold)

    return {target_page_name: potentials}


def find_potential_links_for_random_pages(num_pages, wiki_dir, threshold=0.3):
    """Find potential linking pages for a random selection of pages."""
    all_pages = find_all_wiki_pages(wiki_dir)

    # To avoid selecting pages with duplicate names (like 'start' in different langs)
    # we'll ensure unique page names in our random selection
    page_names = {}
    for page in all_pages:
        page_name = extract_page_name(page)
        if page_name not in page_names:
            page_names[page_name] = []
        page_names[page_name].append(page)

    # Now select unique page names randomly
    unique_page_names = list(page_names.keys())
    selected_names = random.sample(unique_page_names, min(num_pages, len(unique_page_names)))

    # Select one file per selected name (prefer files in root rather than subfolders)
    selected_pages = []
    for name in selected_names:
        pages_with_this_name = page_names[name]
        # Prefer page in root directory or shorter path
        preferred_page = min(pages_with_this_name, key=lambda p: p.count(os.sep))
        selected_pages.append(preferred_page)

    # Pre-load all page content to avoid repeated file reads
    page_cache = {}
    keyword_cache = {}

    for page in all_pages:
        content = load_page_content(page)
        page_cache[page] = content
        keyword_cache[page] = extract_content_keywords(content)

    random_potential_links = {}

    for page in selected_pages:
        page_name = extract_page_name(page)
        potentials = find_potential_linking_pages(page, wiki_dir, page_cache, keyword_cache, threshold)
        random_potential_links[page_name] = potentials

    return random_potential_links


def print_potential_links_report(potential_links):
    """Print a report of all potential linking pages."""
    for page, potentials in potential_links.items():
        if potentials:
            print(f"\nPages that may want to link to '{page}':")
            for potential in potentials[:5]:  # Show top 5 suggestions
                print(f"  - {potential['page']} (score: {potential['score']:.2f}, "
                      f"name similarity: {potential['name_similarity']:.2f}, "
                      f"keyword similarity: {potential['keyword_similarity']:.2f})")
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
                            f"keyword similarity: {potential['keyword_similarity']:.2f})\n")
            else:
                f.write("  (no potential linking pages)\n")

    print(f"Potential links report saved to {output_file}")


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description='Find potential linking pages in wiki content')
    parser.add_argument('--wiki-dir', default='wiki-data/pages', help='Directory containing wiki pages')
    parser.add_argument('--threshold', type=float, default=0.3, help='Similarity threshold (default: 0.3)')
    parser.add_argument('--target-page', help='Analyze potential links for a specific page')
    parser.add_argument('--random-pages', type=int, help='Analyze potential links for random pages')
    parser.add_argument('--output-file', default='wiki_potential_links_report_optimized.txt',
                        help='Output file path (default: wiki_potential_links_report_optimized.txt)')

    args = parser.parse_args()

    wiki_data_dir = args.wiki_dir

    if not os.path.exists(wiki_data_dir):
        print(f"Wiki directory {wiki_data_dir} not found. Looking for .txt files in current directory...")
        wiki_data_dir = "."

    print("Finding potential linking pages...")

    # Determine which analysis to run
    if args.target_page:
        potential_links = find_potential_links_for_page(args.target_page, wiki_data_dir, args.threshold)
        print(f"Analyzing potential links for page: {args.target_page}")
    elif args.random_pages:
        potential_links = find_potential_links_for_random_pages(args.random_pages, wiki_data_dir, args.threshold)
        print(f"Analyzing potential links for {args.random_pages} random pages")
    else:
        potential_links = find_all_potential_links(wiki_data_dir, args.threshold)
        print("Analyzing potential links for all pages")

    print("\nPotential Links Report:")
    print("="*50)
    print_potential_links_report(potential_links)

    # Save to file as well
    save_potential_links_to_file(potential_links, args.output_file)