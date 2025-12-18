#!/usr/bin/env python3
"""
Script to check if files mentioned in mr namespace pages actually exist in the codebase.
This helps ensure that the mr: pages contain accurate and up-to-date references to source code files.

Usage:
    python3 check_mr_links.py

The script will:
1. Scan all .txt files in the wiki-data/pages/mr directory
2. Extract all GitHub links in DokuWiki format [[url|display text]]
3. Verify that each linked file actually exists in the repository
4. Report any broken links found
5. Return exit code 0 if all links are valid, 1 if there are broken links
"""

import os
import sys
import re
import requests
from pathlib import Path

def check_local_file_exists(file_path):
    """Check if a local file exists in the codebase"""
    # Standard locations for Remixed Dungeon source code
    base_paths = [
        "RemixedDungeon/src/main/java/",
        "RemixedDungeon/src/main/assets/",
        "RemixedDungeon/src/main/res/",
        "annotation/src/main/java/",
        "processor/src/main/java/",
        "json_clone/src/main/java/",
        "GameServices/src/main/java/"
    ]

    for base_path in base_paths:
        full_path = os.path.join(base_path, file_path)
        if os.path.exists(full_path):
            return True
    return False

def check_github_file_exists(url):
    """Check if a GitHub file exists by attempting to access it.
    GitHub raw content URLs often return 404s, so we'll test with the raw.githubusercontent.com URL"""

    # Convert GitHub blob URL to raw content URL for checking
    raw_url = url.replace("github.com", "raw.githubusercontent.com").replace("/blob/", "/")

    try:
        # First, try the raw URL which should return 200 for existing files
        response = requests.head(raw_url, timeout=10)

        # If we get a 404, it might still be a valid GitHub page, so try GET on original URL
        if response.status_code == 404:
            response = requests.get(url, timeout=10)

        return response.status_code == 200
    except:
        # If both requests fail, try one more time with GET on the original URL
        try:
            response = requests.get(url, timeout=10)
            return response.status_code == 200
        except:
            return False

def extract_links_from_mr_page(file_path):
    """Extract all file links from an mr namespace page"""
    links = []

    with open(file_path, 'r', encoding='utf-8') as f:
        content = f.read()

    # Extract GitHub URLs using regex - looking for DokuWiki format: [[url|text]]
    github_url_pattern = r'\[\[(https://github\.com/[^\]|]+)\|([^\]]+)\]\]'
    github_matches = re.findall(github_url_pattern, content)

    for url, text in github_matches:
        links.append({'type': 'remote', 'url': url, 'original': f'[[{url}|{text}]]'})

    return links

def check_mr_page_links(mr_page_path, verbose=False):
    """Check all links in a single mr namespace page"""
    print(f"Checking links in: {mr_page_path}")

    links = extract_links_from_mr_page(mr_page_path)
    results = []

    for link in links:
        if link['type'] == 'remote':
            # Check if GitHub URL exists
            exists = check_github_file_exists(link['url'])
            status = "✓" if exists else "✗"
            results.append((link['original'], exists, status))
            if verbose:
                print(f"  {status} {link['url']}")

    return results

def check_all_mr_pages(mr_pages_dir, verbose=False):
    """Check all mr namespace pages in a directory"""
    mr_pages = Path(mr_pages_dir).glob("*.txt")
    all_results = []

    pages_processed = 0
    for page in mr_pages:
        pages_processed += 1
        page_results = check_mr_page_links(page, verbose)
        all_results.extend(page_results)

        # Count broken links for this page
        broken_count = sum(1 for _, exists, _ in page_results if not exists)
        print(f"  {len(page_results) - broken_count}/{len(page_results)} links valid")

    if pages_processed == 0:
        print(f"No .txt files found in {mr_pages_dir}")

    return all_results

def main():
    """Main function to run the link checker"""
    # Default location for mr namespace pages
    mr_pages_dir = "wiki-data/pages/mr"

    # Check if directory exists
    if not os.path.exists(mr_pages_dir):
        print(f"Error: Mr namespace directory '{mr_pages_dir}' does not exist")
        print("Please run this script from the root of the Remixed Dungeon project")
        sys.exit(1)

    print(f"Checking links in mr namespace pages in '{mr_pages_dir}'...")
    print("=" * 60)

    all_results = check_all_mr_pages(mr_pages_dir, verbose=True)

    print("=" * 60)

    # Summarize results
    total_links = len(all_results)

    if total_links == 0:
        print("No links found in mr namespace pages.")
        return 0

    valid_links = sum(1 for _, exists, _ in all_results if exists)
    broken_links = total_links - valid_links

    print(f"Summary: {valid_links}/{total_links} links are valid ({broken_links} broken)")

    if broken_links > 0:
        print("\nBroken links:")
        for original, exists, status in all_results:
            if not exists:
                print(f"  {original}")

        return 1  # Return error code

    print("\nAll links are valid!")
    return 0

if __name__ == "__main__":
    sys.exit(main())