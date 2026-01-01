#!/usr/bin/env python3
"""
Script to detect redirect pages (e.g., warrior -> warrior_class), 
automatically fix links in all wiki pages, and delete the redirect page.
"""

import os
import re
import shutil
from pathlib import Path
import argparse


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
    redirect_pattern = r'^\s*redirect\s+([a-zA-Z0-9_:]+)(?:\s+[^\n]*)?\s*$'

    match = re.match(redirect_pattern, content_lower, re.MULTILINE | re.IGNORECASE)
    if match:
        return match.group(1).split(':')[-1]  # Return the target page name without namespace

    return None


def find_redirect_pages_by_content(wiki_dir):
    """Find redirect pages based on their content (DokuWiki redirect syntax)."""
    all_pages = find_all_wiki_pages(wiki_dir)
    redirect_pages = []

    for page in all_pages:
        with open(page, 'r', encoding='utf-8') as f:
            content = f.read()

        target_page = is_redirect_page(content)
        if target_page:
            page_name = extract_page_name(page)
            redirect_pages.append({
                'source_page': page_name,
                'target_page': target_page,
                'file_path': page,
                'content': content.strip()
            })

    return redirect_pages


def find_redirect_pages_by_naming_convention(wiki_dir):
    """Find pages that might be redirects based on naming conventions.

    In the documentation, there are examples of pages with different naming
    conventions that might be redirects or duplicates (like warrior -> warrior_class).
    """
    all_pages = find_all_wiki_pages(wiki_dir)
    page_names = [extract_page_name(page) for page in all_pages]

    # Look for naming patterns that might indicate redirects
    # For example, pages without suffix that redirect to pages with suffix
    redirect_patterns = []
    processed_redirects = set()  # To avoid duplicates

    # Pattern: base name redirects to base_name_class (e.g., warrior -> warrior_class)
    class_pages = [name for name in page_names if name.endswith('_class')]
    regular_pages = [name for name in page_names if not name.endswith(('_class', '_mob', '_item', '_spell', '_npc', '_level', '_mechanic', '_skill', '_talent', '_buff', '_trap', '_script', '_level_object', '_config', '_quest', '_subclass'))]

    for class_page in class_pages:
        base_name = class_page[:-6]  # Remove '_class'
        if base_name in regular_pages:
            redirect_key = (base_name, class_page)
            if redirect_key not in processed_redirects:
                redirect_patterns.append({
                    'source_page': base_name,
                    'target_page': class_page
                })
                processed_redirects.add(redirect_key)

    # Pattern: base name redirects to base_name_mob (e.g., shaman -> shaman_mob)
    mob_pages = [name for name in page_names if name.endswith('_mob')]
    for mob_page in mob_pages:
        base_name = mob_page[:-4]  # Remove '_mob'
        if base_name in regular_pages:
            redirect_key = (base_name, mob_page)
            if redirect_key not in processed_redirects:
                redirect_patterns.append({
                    'source_page': base_name,
                    'target_page': mob_page
                })
                processed_redirects.add(redirect_key)

    # Pattern: base name redirects to base_name_spell (e.g., heal -> heal_spell)
    spell_pages = [name for name in page_names if name.endswith('_spell')]
    for spell_page in spell_pages:
        base_name = spell_page[:-6]  # Remove '_spell'
        if base_name in regular_pages:
            redirect_key = (base_name, spell_page)
            if redirect_key not in processed_redirects:
                redirect_patterns.append({
                    'source_page': base_name,
                    'target_page': spell_page
                })
                processed_redirects.add(redirect_key)

    # Pattern: base name redirects to base_name_item (e.g., ankh -> ankh_item)
    item_pages = [name for name in page_names if name.endswith('_item')]
    for item_page in item_pages:
        base_name = item_page[:-5]  # Remove '_item'
        if base_name in regular_pages:
            redirect_key = (base_name, item_page)
            if redirect_key not in processed_redirects:
                redirect_patterns.append({
                    'source_page': base_name,
                    'target_page': item_page
                })
                processed_redirects.add(redirect_key)

    # Pattern: base name redirects to base_name_npc (e.g., shopkeeper -> shopkeeper_npc)
    npc_pages = [name for name in page_names if name.endswith('_npc')]
    for npc_page in npc_pages:
        base_name = npc_page[:-4]  # Remove '_npc'
        if base_name in regular_pages:
            redirect_key = (base_name, npc_page)
            if redirect_key not in processed_redirects:
                redirect_patterns.append({
                    'source_page': base_name,
                    'target_page': npc_page
                })
                processed_redirects.add(redirect_key)

    # Pattern: base name redirects to base_name_subclass (e.g., shaman -> shaman_subclass)
    subclass_pages = [name for name in page_names if name.endswith('_subclass')]
    for subclass_page in subclass_pages:
        base_name = subclass_page[:-9]  # Remove '_subclass'
        if base_name in regular_pages:
            redirect_key = (base_name, subclass_page)
            if redirect_key not in processed_redirects:
                redirect_patterns.append({
                    'source_page': base_name,
                    'target_page': subclass_page
                })
                processed_redirects.add(redirect_key)

    return redirect_patterns


def update_links_in_page(page_path, old_page_name, new_page_name, namespace=None):
    """Update all links from old_page_name to new_page_name in a specific wiki page."""
    with open(page_path, 'r', encoding='utf-8') as f:
        content = f.read()

    # Define the patterns to match links to the old page name
    # This handles both [[namespace:old_name|Display Text]] and [[namespace:old_name]] formats
    # Using word boundaries to avoid partial matches in longer page names
    patterns = []

    if namespace:
        # If namespace is specified, match only that namespace
        patterns.append(rf'\[\[({namespace}:){re.escape(old_page_name)}(\|[^[\]]*)?\]\]')
    else:
        # If no namespace specified, match any namespace or no namespace
        patterns.append(rf'\[\[([a-zA-Z0-9_]+:)?{re.escape(old_page_name)}(\|[^[\]]*)?\]\]')

    # Always match links without namespace
    patterns.append(rf'\[\[{re.escape(old_page_name)}(\|[^[\]]*)?\]\]')

    updated_content = content
    links_updated = 0

    for pattern in patterns:
        # Find all matches before replacement to report
        matches = re.findall(pattern, content, re.IGNORECASE)
        links_updated += len(matches)

        # Replace with new page name, preserving namespace and display text
        def replace_link(match):
            full_match = match.group(0)
            # Extract the namespace part if present
            namespace_match = match.group(1) if match.group(1) else None
            # Extract the display text if present
            display_text_match = match.group(2) if match.group(2) else None

            if namespace_match:
                # Extract the actual namespace part (without the colon)
                actual_namespace = namespace_match[:-1]  # Remove the colon
                if display_text_match:
                    return f'[[{actual_namespace}:{new_page_name}{display_text_match}]]'
                else:
                    return f'[[{actual_namespace}:{new_page_name}]]'
            else:
                if display_text_match:
                    return f'[[{new_page_name}{display_text_match}]]'
                else:
                    return f'[[{new_page_name}]]'

        updated_content = re.sub(pattern, replace_link, updated_content, flags=re.IGNORECASE)

    # Only write back if changes were made
    if updated_content != content:
        with open(page_path, 'w', encoding='utf-8') as f:
            f.write(updated_content)
        print(f"    Updated {links_updated} links in {page_path}")
        return True, links_updated
    else:
        return False, 0


def fix_redirects_and_update_links(wiki_dir, dry_run=False):
    """Find redirect pages and update links in all wiki pages."""
    # Find both content-based and naming convention redirects
    content_redirects = find_redirect_pages_by_content(wiki_dir)
    naming_redirects = find_redirect_pages_by_naming_convention(wiki_dir)

    all_redirects = content_redirects + [{'source_page': r['source_page'], 'target_page': r['target_page'], 'file_path': None, 'content': 'Naming convention redirect'} for r in naming_redirects]

    # Get all wiki pages to update
    all_pages = find_all_wiki_pages(wiki_dir)

    total_links_updated = 0
    total_pages_updated = 0
    redirects_processed = []

    print(f"Found {len(content_redirects)} content-based redirects and {len(naming_redirects)} naming convention redirects")

    for redirect in all_redirects:
        source_page = redirect['source_page']
        target_page = redirect['target_page']
        source_file_path = redirect['file_path']

        print(f"Processing redirect: {source_page} -> {target_page}")

        # Update links in all wiki pages
        pages_for_redirect = 0
        links_for_redirect = 0
        for page_path in all_pages:
            # Skip the redirect page itself if it exists
            if source_file_path and page_path == source_file_path:
                continue

            updated, links_count = update_links_in_page(page_path, source_page, target_page)

            if updated:
                pages_for_redirect += 1
                links_for_redirect += links_count

        total_pages_updated += pages_for_redirect
        total_links_updated += links_for_redirect

        # Track processed redirect
        redirects_processed.append(redirect)

        # If this is a content-based redirect, delete the redirect page after updating links
        if source_file_path:
            if not dry_run:
                os.remove(source_file_path)
                print(f"  Deleted redirect page: {source_file_path}")
            else:
                print(f"  Would delete redirect page: {source_file_path}")

    return redirects_processed, total_pages_updated, total_links_updated


def main():
    parser = argparse.ArgumentParser(description="Detect and fix wiki redirects by updating links and deleting redirect pages")
    parser.add_argument("--wiki-dir", default="wiki-data/pages", help="Path to wiki pages directory (default: wiki-data/pages)")
    parser.add_argument("--dry-run", action="store_true", help="Show what would be changed without making actual changes")
    
    args = parser.parse_args()
    
    wiki_data_dir = args.wiki_dir

    if not os.path.exists(wiki_data_dir):
        print(f"Wiki directory {wiki_data_dir} not found.")
        return

    print(f"{'DRY RUN - ' if args.dry_run else ''}Finding and fixing redirect pages in {wiki_data_dir}...")
    
    redirects_processed, pages_updated, links_updated = fix_redirects_and_update_links(wiki_data_dir, dry_run=args.dry_run)
    
    print(f"\nSummary:")
    print(f"  Redirects processed: {len(redirects_processed)}")
    print(f"  Pages updated: {pages_updated}")
    print(f"  Links updated: {links_updated}")
    
    if args.dry_run:
        print("\nThis was a dry run. No files were actually modified.")


if __name__ == "__main__":
    main()