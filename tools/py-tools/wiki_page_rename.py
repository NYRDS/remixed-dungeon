#!/usr/bin/env python3
"""
Script to rename wiki pages and update all internal links to the renamed page.
This ensures consistency when renaming pages in the wiki.
"""

import os
import re
import sys
import argparse
from pathlib import Path
from typing import List, Tuple


def find_all_wiki_pages(wiki_dir: str) -> List[str]:
    """Find all wiki pages in the wiki directory."""
    pages = []
    for root, dirs, files in os.walk(wiki_dir):
        for file in files:
            if file.endswith('.txt'):
                pages.append(os.path.join(root, file))
    return pages


def extract_page_name(filepath: str) -> str:
    """Extract page name from file path (without extension)."""
    return Path(filepath).stem.lower()


def get_all_links_in_page(page_path: str) -> List[Tuple[str, int, str]]:
    """Extract all internal links from a wiki page.
    
    Returns a list of tuples (link_target, line_number, line_content)
    """
    links = []
    with open(page_path, 'r', encoding='utf-8') as f:
        lines = f.readlines()
    
    for i, line in enumerate(lines):
        # DokuWiki internal link format: [[rpd:page_name|Display Text]] or [[page_name|Display Text]]
        # Also handle links without display text: [[rpd:page_name]] or [[page_name]]
        # This regex matches both formats
        pattern = r'\[\[rpd:([^|\]]+)(?:\|[^]]+)?\]\]|\[\[([^|\]]+)(?:\|[^]]+)?\]\]'
        matches = re.finditer(pattern, line)
        
        for match in matches:
            # Check both capture groups (group 1 for rpd: links, group 2 for regular links)
            link_target = match.group(1) or match.group(2)
            if link_target:
                links.append((link_target.lower(), i + 1, line.strip()))
    
    return links


def update_links_in_page(page_path: str, old_name: str, new_name: str) -> int:
    """Update all links from old_name to new_name in a specific page.

    Returns the number of links updated.
    """
    with open(page_path, 'r', encoding='utf-8') as f:
        content = f.read()

    original_content = content

    # Count the number of replacements that will be made
    old_rpd_with_text_pattern = rf'\[\[rpd:{re.escape(old_name)}\|[^\]]+\]\]'
    old_rpd_without_text_pattern = rf'\[\[rpd:{re.escape(old_name)}\]\]'
    old_regular_with_text_pattern = rf'\[\[{re.escape(old_name)}\|[^\]]+\]\]'
    old_regular_without_text_pattern = rf'(?<!\w)\[\[{re.escape(old_name)}\]\](?!\w)'

    count_rpd_with_text = len(re.findall(old_rpd_with_text_pattern, content, re.IGNORECASE))
    count_rpd_without_text = len(re.findall(old_rpd_without_text_pattern, content, re.IGNORECASE))
    count_regular_with_text = len(re.findall(old_regular_with_text_pattern, content, re.IGNORECASE))
    count_regular_without_text = len(re.findall(old_regular_without_text_pattern, content, re.IGNORECASE))

    total_changes = (count_rpd_with_text + count_rpd_without_text +
                     count_regular_with_text + count_regular_without_text)

    # Update rpd: links with display text (e.g. [[rpd:old_name|Text]] -> [[rpd:new_name|Text]])
    pattern1 = rf'\[\[rpd:{re.escape(old_name)}(\|[^\]]+?)\]\]'
    replacement1 = f'[[rpd:{new_name}\\1]]'
    content = re.sub(pattern1, replacement1, content, flags=re.IGNORECASE)

    # Update rpd: links without display text (e.g. [[rpd:old_name]] -> [[rpd:new_name]])
    pattern2 = rf'\[\[rpd:{re.escape(old_name)}\]\]'
    replacement2 = f'[[rpd:{new_name}]]'
    content = re.sub(pattern2, replacement2, content, flags=re.IGNORECASE)

    # Update regular links with display text (e.g. [[old_name|Text]] -> [[new_name|Text]])
    pattern3 = rf'\[\[{re.escape(old_name)}(\|[^\]]+?)\]\]'
    replacement3 = f'[[{new_name}\\1]]'
    content = re.sub(pattern3, replacement3, content, flags=re.IGNORECASE)

    # Update regular links without display text (e.g. [[old_name]] -> [[new_name]])
    # Use word boundaries to avoid partial matches in longer names
    pattern4 = rf'(?<!\w)\[\[{re.escape(old_name)}\]\](?!\w)'
    replacement4 = f'[[{new_name}]]'
    content = re.sub(pattern4, replacement4, content, flags=re.IGNORECASE)

    # Write back the updated content if there were changes
    if content != original_content:
        with open(page_path, 'w', encoding='utf-8') as f:
            f.write(content)

    return total_changes


def get_pages_linked_to(target_page: str, all_pages: List[str]) -> List[str]:
    """Find all pages that contain links to the target page."""
    linked_pages = []
    
    for page_path in all_pages:
        links_in_page = get_all_links_in_page(page_path)
        for link_target, _, _ in links_in_page:
            if link_target == target_page.lower():
                linked_pages.append(page_path)
                break  # Found a link, no need to check further
    
    return linked_pages


def rename_wiki_page(wiki_dir: str, old_page_name: str, new_page_name: str, dry_run: bool = False) -> bool:
    """Rename a wiki page and update all links to it throughout the wiki.
    
    Args:
        wiki_dir: Path to the wiki directory
        old_page_name: Current name of the page to be renamed
        new_page_name: New name for the page
        dry_run: If True, only show what would be changed without making changes
    
    Returns:
        True if successful, False otherwise
    """
    # Find the page to rename
    old_page_path = None
    for root, dirs, files in os.walk(wiki_dir):
        for file in files:
            if file.lower() == old_page_name.lower() + '.txt':
                old_page_path = os.path.join(root, file)
                break
        if old_page_path:
            break
    
    if not old_page_path:
        print(f"Error: Page '{old_page_name}' not found in wiki directory.")
        return False
    
    # Check if the new page name already exists
    new_page_path = os.path.join(os.path.dirname(old_page_path), new_page_name.lower() + '.txt')
    if os.path.exists(new_page_path):
        print(f"Error: Page '{new_page_name}' already exists.")
        return False
    
    print(f"Renaming page '{old_page_name}' to '{new_page_name}'...")
    
    # Find all pages that link to the old page name
    all_pages = find_all_wiki_pages(wiki_dir)
    pages_with_links = get_pages_linked_to(old_page_name, all_pages)
    
    if pages_with_links:
        print(f"Found {len(pages_with_links)} page(s) that link to '{old_page_name}':")
        for page in pages_with_links:
            print(f"  - {page}")
    else:
        print(f"No pages link to '{old_page_name}'.")
    
    if not dry_run:
        # Update links in all affected pages
        total_updates = 0
        for page_path in pages_with_links:
            updates = update_links_in_page(page_path, old_page_name, new_page_name)
            if updates > 0:
                print(f"Updated {updates} link(s) in '{page_path}'")
                total_updates += updates
        
        # Rename the actual file
        os.rename(old_page_path, new_page_path)
        print(f"Renamed file: '{old_page_name}.txt' -> '{new_page_name}.txt'")
        
        print(f"Successfully renamed page '{old_page_name}' to '{new_page_name}'")
        print(f"Updated {total_updates} links across {len(pages_with_links)} pages")
    else:
        print("DRY RUN MODE: No actual changes were made.")
        print(f"Would update {len(pages_with_links)} page(s) with links to '{old_page_name}'")
    
    return True


def main():
    parser = argparse.ArgumentParser(
        description='Rename wiki pages and update all internal links to them'
    )
    parser.add_argument(
        'old_name',
        help='Current name of the page to rename (without .txt extension)'
    )
    parser.add_argument(
        'new_name',
        help='New name for the page (without .txt extension)'
    )
    parser.add_argument(
        '--wiki-dir', 
        default='wiki-data/pages',
        help='Directory containing wiki pages (default: wiki-data/pages)'
    )
    parser.add_argument(
        '--dry-run',
        action='store_true',
        help='Show what would be changed without making actual changes'
    )
    
    args = parser.parse_args()
    
    # Validate inputs
    if not os.path.exists(args.wiki_dir):
        print(f"Error: Wiki directory '{args.wiki_dir}' does not exist.")
        sys.exit(1)
    
    # Perform the rename operation
    success = rename_wiki_page(
        args.wiki_dir, 
        args.old_name, 
        args.new_name, 
        dry_run=args.dry_run
    )
    
    if not success:
        sys.exit(1)


if __name__ == "__main__":
    main()