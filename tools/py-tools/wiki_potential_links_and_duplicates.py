#!/usr/bin/env python3
"""
Script to identify wiki pages that may want to link to the current page
and detect potential duplicate pages.
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

    # Include some key concepts that might indicate similar content
    # Look for certain game terms that indicate similar pages
    game_terms = re.findall(r'\b(mob|spell|item|armor|weapon|ring|potion|scroll|ability|class|subclass|hero|boss|level|area)\b', content_lower)
    keywords.update(game_terms)

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


def find_potential_duplicates(wiki_dir, similarity_threshold=0.6):
    """
    Find potential duplicate pages based on name and content similarity.
    """
    all_pages = find_all_wiki_pages(wiki_dir)

    # Load all page content
    page_contents = {}
    page_keywords = {}
    page_names = {}

    for page in all_pages:
        content = load_page_content(page)
        page_contents[page] = content
        page_keywords[page] = extract_content_keywords(content)
        page_names[page] = extract_page_name(page)

    potential_duplicates = []

    # Compare each page with every other page
    for i, page1 in enumerate(all_pages):
        for page2 in all_pages[i+1:]:
            name1 = page_names[page1]
            name2 = page_names[page2]
            
            # Calculate name similarity
            name_similarity = simple_string_similarity(name1, name2)
            
            # Calculate content similarity using Jaccard similarity
            keywords1 = page_keywords[page1]
            keywords2 = page_keywords[page2]
            content_similarity = jaccard_similarity(keywords1, keywords2)
            
            # Combine both similarities to get an overall score
            combined_similarity = (name_similarity + content_similarity) / 2
            
            if combined_similarity >= similarity_threshold:
                # Check if the content is also similar by comparing full content
                content1 = page_contents[page1].lower()
                content2 = page_contents[page2].lower()
                
                # Use a simplified text overlap measure
                words1 = set(content1.split())
                words2 = set(content2.split())
                
                content_overlap = jaccard_similarity(words1, words2)
                
                if content_overlap >= similarity_threshold:
                    potential_duplicates.append({
                        'page1': name1,
                        'page2': name2,
                        'name_similarity': name_similarity,
                        'content_similarity': content_similarity,
                        'content_overlap': content_overlap,
                        'combined_similarity': combined_similarity,
                        'file1': page1,
                        'file2': page2
                    })

    # Sort by combined similarity score
    potential_duplicates.sort(key=lambda x: x['combined_similarity'], reverse=True)
    
    return potential_duplicates


def find_duplicates_by_naming_pattern(wiki_dir):
    """
    Find potential duplicates based on naming patterns.
    E.g., pages with similar names but different formats (snake_case vs camelCase, with/without suffixes).
    """
    all_pages = find_all_wiki_pages(wiki_dir)
    page_names = [extract_page_name(page) for page in all_pages]
    
    # Create a mapping of normalized names to original names
    # This helps identify pages that are essentially the same but with different naming
    normalized_map = defaultdict(list)
    
    for page_name in page_names:
        # Create a normalized version by removing common suffixes and underscores
        normalized = page_name.replace('_mob', '').replace('_spell', '').replace('_', '').lower()
        normalized_map[normalized].append(page_name)
    
    # Find entries with multiple pages (potential duplicates)
    potential_naming_duplicates = []
    for normalized, original_names in normalized_map.items():
        if len(original_names) > 1:
            potential_naming_duplicates.append({
                'normalized_name': normalized,
                'original_names': original_names,
                'count': len(original_names)
            })
    
    # Sort by number of potential duplicates (descending)
    potential_naming_duplicates.sort(key=lambda x: x['count'], reverse=True)
    
    return potential_naming_duplicates


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


def print_potential_duplicates_report(potential_duplicates):
    """Print a report of potential duplicate pages."""
    if not potential_duplicates:
        print("\nNo potential duplicate pages found.")
        return

    print(f"\nFound {len(potential_duplicates)} potential duplicate page pairs:")
    print("=" * 80)
    
    for dup in potential_duplicates[:10]:  # Show top 10
        print(f"\nPotential Duplicates:")
        print(f"  Page 1: {dup['page1']}")
        print(f"  Page 2: {dup['page2']}")
        print(f"  Name Similarity: {dup['name_similarity']:.2f}")
        print(f"  Content Similarity: {dup['content_similarity']:.2f}")
        print(f"  Content Overlap: {dup['content_overlap']:.2f}")
        print(f"  Combined Similarity: {dup['combined_similarity']:.2f}")


def print_naming_duplicates_report(naming_duplicates):
    """Print a report of potential duplicates by naming pattern."""
    if not naming_duplicates:
        print("\nNo potential naming pattern duplicates found.")
        return

    print(f"\nFound {len(naming_duplicates)} potential naming pattern duplicates:")
    print("=" * 80)
    
    for dup in naming_duplicates[:10]:  # Show top 10
        print(f"\nNormalized name: {dup['normalized_name']}")
        print(f"  Original names: {', '.join(dup['original_names'])}")
        print(f"  Count: {dup['count']}")


def save_combined_report_to_file(potential_links, potential_duplicates, naming_duplicates, output_file):
    """Save a combined report of potential links and duplicates to a file."""
    with open(output_file, 'w', encoding='utf-8') as f:
        f.write("Wiki Potential Links and Duplicates Report\n")
        f.write("=" * 80)
        f.write("\n\n")

        # Write potential links report
        f.write("POTENTIAL LINKS\n")
        f.write("-" * 40)
        f.write("\n\n")
        
        for page, potentials in potential_links.items():
            f.write(f"Pages that may want to link to '{page}':\n")
            if potentials:
                for potential in potentials[:5]:  # Show top 5 suggestions
                    f.write(f"  - {potential['page']} (score: {potential['score']:.2f}, "
                            f"name similarity: {potential['name_similarity']:.2f}, "
                            f"keyword similarity: {potential['keyword_similarity']:.2f})\n")
            else:
                f.write("  (no potential linking pages)\n")
            f.write("\n")

        # Write potential duplicates report
        f.write("\n\nPOTENTIAL DUPLICATES (Content-Based)\n")
        f.write("-" * 40)
        f.write("\n\n")
        
        if potential_duplicates:
            for dup in potential_duplicates[:10]:  # Show top 10
                f.write(f"Potential Duplicates:\n")
                f.write(f"  Page 1: {dup['page1']}\n")
                f.write(f"  Page 2: {dup['page2']}\n")
                f.write(f"  Name Similarity: {dup['name_similarity']:.2f}\n")
                f.write(f"  Content Similarity: {dup['content_similarity']:.2f}\n")
                f.write(f"  Content Overlap: {dup['content_overlap']:.2f}\n")
                f.write(f"  Combined Similarity: {dup['combined_similarity']:.2f}\n")
                f.write("\n")
        else:
            f.write("No potential duplicate pages found based on content analysis.\n\n")

        # Write naming pattern duplicates report
        f.write("\n\nPOTENTIAL DUPLICATES (Naming Pattern)\n")
        f.write("-" * 40)
        f.write("\n\n")
        
        if naming_duplicates:
            for dup in naming_duplicates[:10]:  # Show top 10
                f.write(f"Normalized name: {dup['normalized_name']}\n")
                f.write(f"  Original names: {', '.join(dup['original_names'])}\n")
                f.write(f"  Count: {dup['count']}\n\n")
        else:
            f.write("No potential duplicate pages found based on naming patterns.\n\n")

    print(f"Combined report saved to {output_file}")


def main():
    parser = argparse.ArgumentParser(description='Find potential linking pages and duplicate pages in wiki content')
    parser.add_argument('--wiki-dir', default='wiki-data/pages', help='Directory containing wiki pages')
    parser.add_argument('--threshold', type=float, default=0.3, help='Similarity threshold for potential links (default: 0.3)')
    parser.add_argument('--dup-threshold', type=float, default=0.6, help='Similarity threshold for duplicate detection (default: 0.6)')
    parser.add_argument('--target-page', help='Analyze potential links for a specific page')
    parser.add_argument('--random-pages', type=int, help='Analyze potential links for random pages')
    parser.add_argument('--output-file', default='wiki_analysis_report.txt',
                        help='Output file path (default: wiki_analysis_report.txt)')
    parser.add_argument('--analysis', choices=['links', 'duplicates', 'both'], default='both',
                        help='Type of analysis to perform: links, duplicates, or both (default: both)')

    args = parser.parse_args()

    wiki_data_dir = args.wiki_dir

    if not os.path.exists(wiki_data_dir):
        print(f"Wiki directory {wiki_data_dir} not found. Looking for .txt files in current directory...")
        wiki_data_dir = "."

    print("Analyzing wiki content...")

    potential_links = {}
    potential_duplicates = []
    naming_duplicates = []

    if args.analysis in ['links', 'both']:
        # Determine which potential links analysis to run
        if args.target_page:
            potential_links = find_potential_links_for_page(args.target_page, wiki_data_dir, args.threshold)
            print(f"Analyzing potential links for page: {args.target_page}")
        elif args.random_pages:
            potential_links = find_potential_links_for_random_pages(args.random_pages, wiki_data_dir, args.threshold)
            print(f"Analyzing potential links for {args.random_pages} random pages")
        else:
            potential_links = find_all_potential_links(wiki_data_dir, args.threshold)
            print("Analyzing potential links for all pages")

    if args.analysis in ['duplicates', 'both']:
        print("Analyzing potential duplicates...")
        potential_duplicates = find_potential_duplicates(wiki_data_dir, args.dup_threshold)
        naming_duplicates = find_duplicates_by_naming_pattern(wiki_data_dir)

    # Print reports based on the type of analysis
    if args.analysis in ['links', 'both'] and potential_links:
        print("\nPotential Links Report:")
        print("="*50)
        print_potential_links_report(potential_links)

    if args.analysis in ['duplicates', 'both']:
        print("\nPotential Duplicates Report:")
        print("="*50)
        print_potential_duplicates_report(potential_duplicates)
        print_naming_duplicates_report(naming_duplicates)

    # Save to file as well
    save_combined_report_to_file(potential_links, potential_duplicates, naming_duplicates, args.output_file)


if __name__ == "__main__":
    main()