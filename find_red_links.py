#!/usr/bin/env python3
"""
Script to analyze wiki data: find red links, build wiki map, and generate DOT graphs.

This script can:
1. Find and report red links (links to non-existent pages)
2. Build a complete wiki map showing all page relationships
3. Generate DOT graph files for visualization with Graphviz

Usage Examples:
    # Find only red links (original functionality)
    python find_red_links.py --output red-links

    # Build complete wiki map
    python find_red_links.py --output wiki-map

    # Generate DOT graph file
    python find_red_links.py --output dot

    # Generate DOT graph with only red links
    python find_red_links.py --output dot --red-only

    # Show everything
    python find_red_links.py --output all

Wiki link format: [[target|display_text]] or [[target]]
Special namespaces like 'wiki:' and 'doku>' are ignored as they are not actual pages.
Relative links starting with ':' like ':start' and ':sidebar' are also ignored.
Special namespaces like 'playground:' and 'some:' are also ignored.
"""

import os
import re
import argparse
from pathlib import Path
from typing import Set, List, Tuple, Dict, Any
from collections import defaultdict


def extract_wiki_links(content: str) -> List[Tuple[str, str]]:
    """
    Extract wiki links from content.

    Returns a list of tuples (target, display_text) where target is the
    page being linked to and display_text is the text shown for the link.
    """
    # Pattern to match [[target|display_text]] or [[target]]
    # Handles both internal links (e.g., rpd:elementals) and internal page links
    pattern = r'\[\[([^\]]+)\]\]'

    links = []
    for match in re.finditer(pattern, content):
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
            links.append((target, display_text))

    return links


def get_existing_pages(pages_dir: Path) -> Set[str]:
    """
    Get a set of all existing wiki pages by scanning the pages directory.
    """
    existing_pages = set()

    for root, dirs, files in os.walk(pages_dir):
        for file in files:
            if file.endswith('.txt'):
                file_path = Path(root) / file
                # Convert file path to wiki page name
                # e.g., pages/rpd/elementals.txt -> rpd:elementals
                # e.g., pages/start.txt -> start
                # e.g., pages/ru/rpd/elementals.txt -> ru:rpd:elementals

                # Get relative path from pages directory
                rel_path = file_path.relative_to(pages_dir)

                # Convert to wiki page name
                parts = rel_path.parts
                if len(parts) == 1:
                    # Simple page like start.txt -> start
                    page_name = parts[0][:-4]  # Remove .txt
                elif len(parts) == 2:
                    # Namespaced page like rpd/elementals.txt -> rpd:elementals
                    namespace = parts[0]
                    page = parts[1][:-4]  # Remove .txt
                    page_name = f"{namespace}:{page}"
                elif len(parts) >= 3:
                    # Multi-namespaced page like ru/rpd/elementals.txt -> ru:rpd:elementals
                    namespace_path = ':'.join(parts[:-1])
                    page = parts[-1][:-4]  # Remove .txt
                    page_name = f"{namespace_path}:{page}"

                existing_pages.add(page_name)

    return existing_pages


def resolve_wiki_link(link_target: str, current_namespace: str = None) -> str:
    """
    Resolve a wiki link target to an actual page name.

    Handles namespace shortcuts like 'start' in rpd namespace becoming 'rpd:start'.
    """
    # Check if it's a special namespace that should be ignored
    if link_target.startswith(('wiki:', 'doku>')):
        return link_target  # Return as is since we'll filter it out later

    # Check if it's a special relative link that should be ignored (like :start, :sidebar)
    if link_target.startswith(':'):
        return link_target  # Return as is since we'll filter it out later

    # Check if it's a special namespace that should be ignored (like playground:, some:)
    if link_target.startswith(('playground:', 'some:')):
        return link_target  # Return as is since we'll filter it out later

    # If it contains a colon (but not at the start), it's a full namespace:page reference
    if ':' in link_target and not link_target.startswith(':'):
        return link_target

    # If it doesn't contain a colon, it's a local page reference
    # If we know the current namespace, use it as a prefix
    if current_namespace:
        # Handle multi-level namespaces like 'ru:rpd'
        return f"{current_namespace}:{link_target}"

    # Otherwise, it's a root page
    return link_target


def build_wiki_map(wiki_data_dir: Path) -> Tuple[Dict[str, List[Tuple[str, str]]], List[Tuple[str, str, str]], Set[str]]:
    """
    Build a complete wiki map showing all page relationships.

    Returns:
    - Dictionary mapping page names to lists of (target, display_text) tuples
    - List of red links (page_path, link_target, display_text)
    - Set of all existing pages
    """
    pages_dir = wiki_data_dir / 'pages'
    if not pages_dir.exists():
        print(f"Error: Pages directory {pages_dir} does not exist")
        return {}, [], set()

    existing_pages = get_existing_pages(pages_dir)
    page_links = defaultdict(list)  # Maps page_path to list of (target, display_text) tuples
    red_links = []

    # Walk through all .txt files in the pages directory
    for root, dirs, files in os.walk(pages_dir):
        for file in files:
            if file.endswith('.txt'):
                file_path = Path(root) / file

                # Determine the namespace for this file
                rel_path = file_path.relative_to(pages_dir)
                parts = rel_path.parts
                if len(parts) > 1:
                    # Multi-level namespace, e.g., 'ru/rpd/enemies.txt' -> 'ru:rpd'
                    current_namespace = ':'.join(parts[:-1])
                else:
                    current_namespace = None

                # Convert file path to page name for the map
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

                # Read the file content
                try:
                    with open(file_path, 'r', encoding='utf-8') as f:
                        content = f.read()
                except UnicodeDecodeError:
                    print(f"Warning: Could not read {file_path} due to encoding issues")
                    continue

                # Extract links from the content
                links = extract_wiki_links(content)

                for target, display_text in links:
                    # Skip special namespaces like wiki: and doku>
                    if target.startswith(('wiki:', 'doku>')):
                        continue

                    # Skip special relative links like :start and :sidebar
                    if target.startswith(':'):
                        continue

                    # Skip special namespaces like playground: and some:
                    if target.startswith(('playground:', 'some:')):
                        continue

                    # Resolve the target to an actual page name
                    resolved_target = resolve_wiki_link(target, current_namespace)

                    # Skip special namespaces after resolution
                    if resolved_target.startswith(('wiki:', 'doku>')):
                        continue

                    # Skip special relative links after resolution
                    if resolved_target.startswith(':'):
                        continue

                    # Skip special namespaces after resolution
                    if resolved_target.startswith(('playground:', 'some:')):
                        continue

                    # Add to page links map
                    page_links[page_name].append((resolved_target, display_text))

                    # Check if the resolved target exists
                    if resolved_target not in existing_pages:
                        # This is a red link
                        red_links.append((str(file_path), resolved_target, display_text))

    return dict(page_links), red_links, existing_pages


def generate_dot_graph(page_links: Dict[str, List[Tuple[str, str]]], output_file: str, show_red_links_only: bool = False):
    """
    Generate a DOT graph file from the wiki map.

    Args:
        page_links: Dictionary mapping page names to lists of (target, display_text) tuples
        output_file: Path to output DOT file
        show_red_links_only: If True, only show red links in the graph
    """
    # Get all existing pages to determine red links
    all_targets = set()
    for targets in page_links.values():
        for target, _ in targets:
            all_targets.add(target)

    # existing_pages are the pages that have files in the wiki
    # red_links_set are the pages that are linked to but don't exist
    existing_pages = set(page_links.keys())
    all_pages = existing_pages | all_targets
    red_links_set = all_targets - existing_pages

    # Helper function to safely escape DOT strings
    def escape_dot_string(s):
        # Escape quotes and backslashes, which are special in DOT
        return s.replace('\\', '\\\\').replace('"', '\\"')

    with open(output_file, 'w') as f:
        f.write("digraph WikiMap {\n")
        f.write("  rankdir=LR;\n")  # Left to right layout
        f.write("  node [shape=box];\n\n")

        # Write all nodes
        for page in sorted(all_pages):
            escaped_page = escape_dot_string(page)
            if page in red_links_set:
                f.write(f'  "{escaped_page}" [color=red, style=filled, fillcolor=lightpink];\n')
            else:
                f.write(f'  "{escaped_page}" [color=black];\n')

        f.write("\n")

        # Write all edges
        for source_page, targets in page_links.items():
            for target, display_text in targets:
                if show_red_links_only and target not in red_links_set:
                    continue  # Skip non-red links if showing red links only

                escaped_source = escape_dot_string(source_page)
                escaped_target = escape_dot_string(target)
                escaped_label = escape_dot_string(display_text)

                if target in red_links_set:
                    f.write(f'  "{escaped_source}" -> "{escaped_target}" [color=red, label="{escaped_label}"];\n')
                else:
                    f.write(f'  "{escaped_source}" -> "{escaped_target}" [label="{escaped_label}"];\n')

        f.write("}\n")

    print(f"DOT graph written to {output_file}")


def print_wiki_map(page_links: Dict[str, List[Tuple[str, str]]], existing_pages: Set[str]):
    """
    Print a text-based representation of the wiki map.
    """
    print(f"\nWiki Map (showing {len(page_links)} pages with their links):")
    print("=" * 80)

    for page in sorted(page_links.keys()):
        links = page_links[page]
        if links:
            print(f"\nPage: {page}")
            print("-" * 40)
            for target, display_text in links:
                status = "EXISTS" if target in existing_pages else "RED LINK"
                status_marker = "✓" if target in existing_pages else "✗"
                print(f"  {status_marker} [[{target}|{display_text}]] -> {status}")
        else:
            print(f"\nPage: {page}")
            print("-" * 40)
            print("  (no links)")


def find_red_links(wiki_data_dir: Path) -> List[Tuple[str, str, str]]:
    """
    Find all red links in the wiki data.

    Returns a list of tuples (page_path, link_target, display_text) for red links.
    """
    page_links, red_links, _ = build_wiki_map(wiki_data_dir)
    return red_links


def main():
    parser = argparse.ArgumentParser(description="Analyze wiki data to find red links and build wiki map")
    parser.add_argument("--dir", default="wiki-data", help="Wiki data directory (default: wiki-data)")
    parser.add_argument("--output", choices=["red-links", "wiki-map", "dot", "all"], default="red-links",
                       help="Output format: red-links (default), wiki-map, dot (graphviz), or all")
    parser.add_argument("--graph-file", default="wiki_map.dot", help="Output file for DOT graph (default: wiki_map.dot)")
    parser.add_argument("--red-only", action="store_true", help="In DOT output, show only red links")

    args = parser.parse_args()

    wiki_data_dir = Path(args.dir)

    if not wiki_data_dir.exists():
        print(f"Error: Wiki data directory {wiki_data_dir} does not exist")
        return

    print(f"Analyzing wiki data in {wiki_data_dir}...")
    page_links, red_links, existing_pages = build_wiki_map(wiki_data_dir)

    if args.output in ["red-links", "all"]:
        print(f"\nFound {len(red_links)} red links:")
        print("-" * 80)

        # Group by source page for better readability
        from collections import defaultdict
        grouped = defaultdict(list)
        for page_path, target, display_text in red_links:
            grouped[page_path].append((target, display_text))

        for page_path, links in grouped.items():
            print(f"\nFrom {page_path}:")
            for target, display_text in links:
                print(f"  -> [[{target}|{display_text}]] -> Page '{target}' does not exist")

        if not red_links:
            print("\nNo red links found! All wiki links point to existing pages.")

    if args.output in ["wiki-map", "all"]:
        print_wiki_map(page_links, existing_pages)

    if args.output in ["dot", "all"]:
        generate_dot_graph(page_links, args.graph_file, show_red_links_only=args.red_only)

    print(f"\nChecked against {len(existing_pages)} existing pages.")
    print(f"Wiki map contains {len(page_links)} pages with {sum(len(links) for links in page_links.values())} total links.")


if __name__ == "__main__":
    main()