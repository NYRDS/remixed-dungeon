#!/usr/bin/env python3
"""
Script to identify redirect pages in Remixed Dungeon wiki.
DokuWiki uses specific syntax for redirects, typically a line like:
"redirect [namespace:]page_name [optional display text]"
"""

import os
import re
from pathlib import Path


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


def is_redirect_page(content):
    """Check if the page is a redirect page based on DokuWiki syntax.
    
    DokuWiki redirects typically follow the format:
    redirect [namespace:]page_name [optional display text]
    """
    content_lower = content.strip().lower()
    
    # Check if the entire content of the page is just a redirect directive
    # This is a common pattern for DokuWiki redirect pages
    redirect_pattern = r'^\s*redirect\s+([a-zA-Z0-9_:]+)(?:\s+[^\n]*)?\s*$'
    
    match = re.match(redirect_pattern, content_lower, re.MULTILINE | re.IGNORECASE)
    return match is not None


def find_redirect_pages(wiki_dir):
    """Find all redirect pages in the wiki directory."""
    all_pages = find_all_wiki_pages(wiki_dir)
    redirect_pages = []
    
    for page in all_pages:
        with open(page, 'r', encoding='utf-8') as f:
            content = f.read()
        
        if is_redirect_page(content):
            page_name = extract_page_name(page)
            redirect_pages.append({
                'page': page_name,
                'file_path': page,
                'content': content.strip()
            })
    
    return redirect_pages


def identify_pages_with_same_content(wiki_dir):
    """Identify pages that have identical content (possible redirects or duplicates)."""
    all_pages = find_all_wiki_pages(wiki_dir)
    page_contents = {}
    
    # Load all page contents
    for page in all_pages:
        with open(page, 'r', encoding='utf-8') as f:
            content = f.read().strip()
            page_contents[extract_page_name(page)] = content
    
    # Group pages by content
    content_groups = {}
    for page_name, content in page_contents.items():
        if content in content_groups:
            content_groups[content].append(page_name)
        else:
            content_groups[content] = [page_name]
    
    # Find groups with more than one page (duplicates)
    duplicates = {content: pages for content, pages in content_groups.items() if len(pages) > 1}
    
    return duplicates


def find_redirects_by_naming_convention(wiki_dir):
    """Find pages that might be redirects based on naming conventions.
    
    In the documentation, there are examples of pages with different naming 
    conventions that might be redirects or duplicates (like lich_mob vs lich).
    """
    all_pages = find_all_wiki_pages(wiki_dir)
    page_names = [extract_page_name(page) for page in all_pages]
    
    # Look for naming patterns that might indicate redirects
    # For example, pages with and without '_mob' suffix
    mob_pages = [name for name in page_names if name.endswith('_mob')]
    regular_pages = [name for name in page_names if not name.endswith('_mob') and not name.endswith('_spell')]
    
    potential_redirects = []
    
    for mob_page in mob_pages:
        base_name = mob_page[:-4]  # Remove '_mob'
        if base_name in regular_pages:
            potential_redirects.append({
                'possible_redirect': mob_page,
                'target_page': base_name
            })
    
    # Look for other patterns like pages with and without suffixes
    spell_pages = [name for name in page_names if name.endswith('_spell')]
    regular_pages_no_spell = [name for name in regular_pages if not name.endswith('_spell')]
    
    for spell_page in spell_pages:
        base_name = spell_page[:-7]  # Remove '_spell'
        if base_name in regular_pages_no_spell:
            potential_redirects.append({
                'possible_redirect': spell_page,
                'target_page': base_name
            })
    
    return potential_redirects


def print_redirect_report(redirect_pages, duplicate_groups, naming_redirects):
    """Print a report of all identified redirect-related pages."""
    print("\nExplicit Redirect Pages:")
    print("-" * 30)
    if redirect_pages:
        for redirect in redirect_pages:
            print(f"  - {redirect['page']}: {redirect['content']}")
    else:
        print("  No explicit redirect pages found")
    
    print("\nPages with Duplicate Content:")
    print("-" * 30)
    if duplicate_groups:
        for content, pages in duplicate_groups.items():
            if len(pages) > 1:  # Only show groups with multiple pages
                print(f"  Pages with identical content: {', '.join(pages)}")
    else:
        print("  No duplicate content found")
    
    print("\nPotential Redirects by Naming Convention:")
    print("-" * 45)
    if naming_redirects:
        for redirect in naming_redirects:
            print(f"  - {redirect['possible_redirect']} might redirect to {redirect['target_page']}")
    else:
        print("  No potential redirects found by naming convention")


def save_redirect_report(redirect_pages, duplicate_groups, naming_redirects, output_file):
    """Save redirect report to a file."""
    with open(output_file, 'w', encoding='utf-8') as f:
        f.write("Explicit Redirect Pages:\n")
        f.write("-" * 30 + "\n")
        if redirect_pages:
            for redirect in redirect_pages:
                f.write(f"  - {redirect['page']}: {redirect['content']}\n")
        else:
            f.write("  No explicit redirect pages found\n")
        
        f.write("\nPages with Duplicate Content:\n")
        f.write("-" * 30 + "\n")
        if duplicate_groups:
            for content, pages in duplicate_groups.items():
                if len(pages) > 1:  # Only show groups with multiple pages
                    f.write(f"  Pages with identical content: {', '.join(pages)}\n")
        else:
            f.write("  No duplicate content found\n")
        
        f.write("\nPotential Redirects by Naming Convention:\n")
        f.write("-" * 45 + "\n")
        if naming_redirects:
            for redirect in naming_redirects:
                f.write(f"  - {redirect['possible_redirect']} might redirect to {redirect['target_page']}\n")
        else:
            f.write("  No potential redirects found by naming convention\n")
    
    print(f"Redirect report saved to {output_file}")


if __name__ == "__main__":
    wiki_data_dir = "wiki-data/pages"

    if not os.path.exists(wiki_data_dir):
        print(f"Wiki directory {wiki_data_dir} not found. Looking for .txt files in current directory...")
        wiki_data_dir = "."
    
    print("Finding redirect pages...")
    redirect_pages = find_redirect_pages(wiki_data_dir)
    
    print("Finding duplicate content pages...")
    duplicate_groups = identify_pages_with_same_content(wiki_data_dir)
    
    print("Finding potential redirects by naming convention...")
    naming_redirects = find_redirects_by_naming_convention(wiki_data_dir)
    
    print("\nRedirect Pages Report:")
    print("="*50)
    print_redirect_report(redirect_pages, duplicate_groups, naming_redirects)
    
    # Save to file as well
    save_redirect_report(redirect_pages, duplicate_groups, naming_redirects, "wiki_redirects_report.txt")