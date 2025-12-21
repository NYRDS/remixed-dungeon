#!/usr/bin/env python3
"""
Script to identify and remove redirect pages after fixing all links.
This script identifies pages that have both a base name and a suffixed version
(e.g., warrior and warrior_class) and removes the base name page if it's
a redirect page.
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


def is_redirect_page_content(content):
    """Check if the page content indicates it's a redirect page."""
    content_lower = content.lower()
    
    # Look for common redirect indicators in the content
    redirect_indicators = [
        'redirect',
        'has been moved',
        'please see',
        'this page has been moved',
        'moved to',
        'follow the wiki',
        'naming convention',
        'see also'
    ]
    
    # Check if the content contains redirect indicators and a link to another page
    for indicator in redirect_indicators:
        if indicator in content_lower:
            # Check if there's a DokuWiki link in the content
            if re.search(r'\[\[.*?:.*?\|.*?\]\]', content) or re.search(r'\[\[.*?:.*?\]\]', content):
                return True
    
    return False


def find_redirect_pages(wiki_dir):
    """Find pages that appear to be redirect pages based on content."""
    all_pages = find_all_wiki_pages(wiki_dir)
    redirect_pages = []

    for page in all_pages:
        with open(page, 'r', encoding='utf-8') as f:
            content = f.read()

        if is_redirect_page_content(content):
            page_name = extract_page_name(page)
            redirect_pages.append({
                'page': page_name,
                'file_path': page,
                'content': content[:100] + "..." if len(content) > 100 else content  # Show first 100 chars
            })

    return redirect_pages


def find_pages_with_suffixed_versions(wiki_dir):
    """Find pages that have both base name and suffixed versions."""
    all_pages = find_all_wiki_pages(wiki_dir)
    page_names = [extract_page_name(page) for page in all_pages]
    
    # Separate pages by suffix
    class_pages = [name for name in page_names if name.endswith('_class')]
    mob_pages = [name for name in page_names if name.endswith('_mob')]
    item_pages = [name for name in page_names if name.endswith('_item')]
    spell_pages = [name for name in page_names if name.endswith('_spell')]
    npc_pages = [name for name in page_names if name.endswith('_npc')]
    subclass_pages = [name for name in page_names if name.endswith('_subclass')]
    
    # Find base names that have suffixed versions
    base_names_with_suffixes = set()
    
    for suffixed_page in class_pages:
        base_name = suffixed_page[:-6]  # Remove '_class'
        if base_name in page_names:
            base_names_with_suffixes.add(base_name)
    
    for suffixed_page in mob_pages:
        base_name = suffixed_page[:-4]  # Remove '_mob'
        if base_name in page_names:
            base_names_with_suffixes.add(base_name)
    
    for suffixed_page in item_pages:
        base_name = suffixed_page[:-5]  # Remove '_item'
        if base_name in page_names:
            base_names_with_suffixes.add(base_name)
    
    for suffixed_page in spell_pages:
        base_name = suffixed_page[:-6]  # Remove '_spell'
        if base_name in page_names:
            base_names_with_suffixes.add(base_name)
    
    for suffixed_page in npc_pages:
        base_name = suffixed_page[:-4]  # Remove '_npc'
        if base_name in page_names:
            base_names_with_suffixes.add(base_name)
    
    for suffixed_page in subclass_pages:
        base_name = suffixed_page[:-9]  # Remove '_subclass'
        if base_name in page_names:
            base_names_with_suffixes.add(base_name)
    
    # Get the full file paths for these base name pages
    redirect_candidates = []
    for page_path in all_pages:
        page_name = extract_page_name(page_path)
        if page_name in base_names_with_suffixes:
            redirect_candidates.append(page_path)
    
    return redirect_candidates


def remove_redirect_pages(wiki_dir, dry_run=True):
    """Find and remove redirect pages that have suffixed versions."""
    # Find pages that have both base name and suffixed versions
    redirect_candidates = find_pages_with_suffixed_versions(wiki_dir)
    
    removed_pages = []
    
    for page_path in redirect_candidates:
        with open(page_path, 'r', encoding='utf-8') as f:
            content = f.read()
        
        # Check if the content indicates it's a redirect page
        if is_redirect_page_content(content):
            print(f"Marked for removal: {page_path}")
            if not dry_run:
                os.remove(page_path)
                print(f"  Removed: {page_path}")
            else:
                print(f"  Would remove: {page_path}")
            removed_pages.append(page_path)
        else:
            print(f"Kept (not a redirect): {page_path}")
    
    return removed_pages


if __name__ == "__main__":
    import sys
    
    wiki_data_dir = "wiki-data/pages"
    
    if not os.path.exists(wiki_data_dir):
        print(f"Wiki directory {wiki_data_dir} not found.")
        sys.exit(1)
    
    dry_run = "--dry-run" in sys.argv or "-n" in sys.argv
    
    print(f"{'DRY RUN - ' if dry_run else ''}Finding and removing redirect pages...")
    
    removed_pages = remove_redirect_pages(wiki_data_dir, dry_run=dry_run)
    
    print(f"\nSummary:")
    print(f"  Pages processed for removal: {len(removed_pages)}")
    
    if dry_run:
        print("\nThis was a dry run. No files were actually removed.")
        print("To actually remove the pages, run without --dry-run flag.")