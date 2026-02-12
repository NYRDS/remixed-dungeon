#!/usr/bin/env python3
"""
Script to check wiki pages for language consistency in internal links.

This script verifies that:
1. All internal links on a page should lead to pages in the same language
2. The only exception is links to the same page in other languages, which should be placed at the bottom of the page

Usage Examples:
    # Check all pages for language consistency
    python check_language_links.py

    # Check specific language pages (e.g., Russian)
    python check_language_links.py --language ru

    # Check specific page
    python check_language_links.py --page ru:rpd:warrior_mob

    # Check with verbose output
    python check_language_links.py --verbose
"""

import os
import re
import argparse
from pathlib import Path
from typing import List, Tuple, Dict, Set
from collections import defaultdict


def extract_wiki_links(content: str) -> List[Tuple[str, str, int]]:
    """
    Extract wiki links from content with line numbers.

    Returns a list of tuples (target, display_text, line_number) where target is the
    page being linked to and display_text is the text shown for the link.
    """
    # Pattern to match [[target|display_text]] or [[target]]
    # Handles both internal links (e.g., rpd:elementals) and internal page links
    pattern = r'\[\[([^\]]+)\]\]'

    links = []
    lines = content.split('\n')
    current_line = 1

    for line in lines:
        for match in re.finditer(pattern, line):
            link_content = match.group(1)
            if '|' in link_content:
                target, display_text = link_content.split('|', 1)
                target = target.strip()
                display_text = display_text.strip()
            else:
                target = link_content.strip()
                display_text = target

            # Skip external links (those starting with http:// or https://)
            if not (target.lower().startswith('http://') or target.lower().startswith('https://')):
                links.append((target, display_text, current_line))

        current_line += 1

    return links


def get_page_language(page_name: str) -> str:
    """
    Extract the language from a page name.

    Examples:
    - rpd:warrior_mob -> 'en' (English, default language)
    - ru:rpd:warrior_mob -> 'ru'
    - en:rpd:warrior_mob -> 'en'
    """
    parts = page_name.split(':')
    # Define common language codes
    language_codes = {'ru', 'en', 'cn', 'de', 'fr', 'es', 'it', 'pt', 'ja', 'ko', 'zh', 'pl', 'uk', 'hu', 'tr', 'el', 'in', 'ms'}

    if len(parts) >= 2 and parts[0] in language_codes:
        return parts[0]
    else:
        return 'en'  # Default language is English when no language prefix is present


def is_language_link(target: str, page_name: str) -> bool:
    """
    Check if a link is a link to the same page in another language.

    Examples:
    - From 'ru:rpd:warrior_mob', a link to 'rpd:warrior_mob' or 'en:rpd:warrior_mob' would be a language link
    """
    # Get the language of the current page
    page_language = get_page_language(page_name)

    # Get the language of the target page
    target_language = get_page_language(target)

    # If both have language codes, check if they're different languages but same content
    if page_language and target_language:
        # If languages are different, check if the rest of the page name is the same
        if page_language != target_language:
            # Remove language prefixes and compare the rest
            page_content = page_name[len(page_language)+1:] if page_name.startswith(page_language + ':') else page_name
            target_content = target[len(target_language)+1:] if target.startswith(target_language + ':') else target
            return page_content == target_content
    elif page_language and target_language == 'en':  # Current page has language prefix, target is English (no prefix)
        # Check if target is same as page content without prefix
        page_content = page_name[len(page_language)+1:] if page_name.startswith(page_language + ':') else page_name
        return page_content == target
    elif page_language == 'en' and target_language:  # Current page is English (no prefix), target has prefix
        # Check if page content is same as target without prefix
        target_content = target[len(target_language)+1:] if target.startswith(target_language + ':') else target
        return page_name == target_content

    return False



def check_page_language_consistency(file_path: Path, pages_dir: Path) -> List[Tuple[str, str, int, str]]:
    """
    Check a single wiki page for language consistency in internal links.
    
    Returns a list of violations: (page_name, link_target, line_number, violation_type)
    """
    violations = []
    
    # Determine the page name from the file path
    rel_path = file_path.relative_to(pages_dir)
    parts = rel_path.parts
    if len(parts) == 1:
        page_name = parts[0][:-4]  # Remove .txt
    elif len(parts) == 2:
        namespace = parts[0]
        page = parts[1][:-4]  # Remove .txt
        page_name = f"{namespace}:{page}"
    elif len(parts) >= 3:
        namespace_path = ':'.join(parts[:-1])
        page = parts[-1][:-4]  # Remove .txt
        page_name = f"{namespace_path}:{page}"
    
    current_language = get_page_language(page_name)
    
    # Read the file content
    try:
        with open(file_path, 'r', encoding='utf-8') as f:
            content = f.read()
    except UnicodeDecodeError:
        print(f"Warning: Could not read {file_path} due to encoding issues")
        return violations
    
    # Extract all links
    links = extract_wiki_links(content)
    
    # Check each link
    for target, display_text, line_num in links:
        # Skip special namespaces like wiki: and doku>
        if target.startswith(('wiki:', 'doku>')):
            continue

        # Skip special relative links like :start and :sidebar
        if target.startswith(':'):
            continue

        # Skip special namespaces like playground: and some:
        if target.startswith(('playground:', 'some:')):
            continue

        # Check if this is a language link (link to same page in different language)
        if is_language_link(target, page_name):
            # This is a language link - check if it's at the bottom of the page
            lines = content.split('\n')
            bottom_start_line = max(0, len(lines) - 20)

            if line_num <= bottom_start_line:
                # Language link is NOT at the bottom - this is a violation
                violations.append((page_name, target, line_num, "language_link_not_at_bottom"))
        else:
            # This is a regular internal link - check if it's in the same language
            target_language = get_page_language(target)
            
            if current_language and target_language and current_language != target_language:
                # Different language link that's not a language link (i.e., not to same page in different language)
                violations.append((page_name, target, line_num, "cross_language_link"))
    
    return violations


def main():
    parser = argparse.ArgumentParser(description="Check wiki pages for language consistency in internal links")
    parser.add_argument("--dir", default="wiki-data", help="Wiki data directory (default: wiki-data)")
    parser.add_argument("--language", help="Check only pages in specific language (e.g., ru)")
    parser.add_argument("--page", help="Check only a specific page (e.g., ru:rpd:warrior_mob)")
    parser.add_argument("--verbose", action="store_true", help="Show detailed output")

    args = parser.parse_args()

    wiki_data_dir = Path(args.dir)
    pages_dir = wiki_data_dir / 'pages'

    if not wiki_data_dir.exists():
        print(f"Error: Wiki data directory {wiki_data_dir} does not exist")
        return

    if not pages_dir.exists():
        print(f"Error: Pages directory {pages_dir} does not exist")
        return

    print(f"Checking wiki data in {wiki_data_dir} for language consistency...")
    
    # Find all .txt files in the pages directory
    all_files = []
    for root, dirs, files in os.walk(pages_dir):
        for file in files:
            if file.endswith('.txt'):
                file_path = Path(root) / file
                all_files.append(file_path)

    # Filter files based on language or specific page if specified
    files_to_check = []
    for file_path in all_files:
        # Determine the page name from the file path
        rel_path = file_path.relative_to(pages_dir)
        parts = rel_path.parts
        if len(parts) == 1:
            page_name = parts[0][:-4]  # Remove .txt
        elif len(parts) == 2:
            namespace = parts[0]
            page = parts[1][:-4]  # Remove .txt
            page_name = f"{namespace}:{page}"
        elif len(parts) >= 3:
            namespace_path = ':'.join(parts[:-1])
            page = parts[-1][:-4]  # Remove .txt
            page_name = f"{namespace_path}:{page}"
        
        # Check if this page matches our criteria
        should_check = True
        
        if args.language:
            page_language = get_page_language(page_name)
            if page_language != args.language:
                should_check = False
        
        if args.page and page_name != args.page:
            should_check = False
        
        if should_check:
            files_to_check.append(file_path)

    # Check each file for language consistency
    all_violations = []
    for file_path in files_to_check:
        violations = check_page_language_consistency(file_path, pages_dir)
        all_violations.extend(violations)
        
        if args.verbose and violations:
            print(f"Violations found in {file_path}:")
            for page_name, target, line_num, violation_type in violations:
                print(f"  Line {line_num}: {violation_type} -> [[{target}]]")

    # Report results
    print(f"\nChecked {len(files_to_check)} pages")
    
    if all_violations:
        print(f"\nFound {len(all_violations)} language consistency violations:")
        print("-" * 80)
        
        # Group violations by page
        violations_by_page = defaultdict(list)
        for page_name, target, line_num, violation_type in all_violations:
            violations_by_page[page_name].append((target, line_num, violation_type))
        
        for page_name, page_violations in violations_by_page.items():
            print(f"\nPage: {page_name}")
            print("-" * 40)
            for target, line_num, violation_type in page_violations:
                if violation_type == "language_link_not_at_bottom":
                    print(f"  Line {line_num}: Language link to '{target}' is not at the bottom of the page")
                elif violation_type == "cross_language_link":
                    print(f"  Line {line_num}: Cross-language link to '{target}' found in main content")
    else:
        print("\nNo language consistency violations found! All pages follow the language linking rules.")


if __name__ == "__main__":
    main()