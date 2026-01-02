#!/usr/bin/env python3
"""
Script to analyze wiki data: find red links, missing images, build wiki map, generate backlinks, 
and find pages with names similar to missing links.

This script can:
1. Find and report red links (links to non-existent pages)
2. Find and report missing images (images referenced but not found in media)
3. Build a complete wiki map showing all page relationships
4. Generate backlinks showing which pages link to each page
5. Generate DOT graph files for visualization with Graphviz
6. Find pages with names similar to missing links

Usage Examples:
    # Find only red links (original functionality)
    python find_red_links_with_similar.py --output red-links

    # Find missing images
    python find_red_links_with_similar.py --output missing-images

    # Build complete wiki map
    python find_red_links_with_similar.py --output wiki-map

    # Generate backlinks showing which pages link to each page
    python find_red_links_with_similar.py --output backlinks

    # Generate backlinks for a specific page only
    python find_red_links_with_similar.py --output backlinks --page "rpd:warrior"

    # Generate DOT graph file
    python find_red_links_with_similar.py --output dot

    # Generate DOT graph with only red links
    python find_red_links_with_similar.py --output dot --red-only

    # Show everything
    python find_red_links_with_similar.py --output all

    # Find pages with names similar to missing links
    python find_red_links_with_similar.py --output similar

Wiki link format: [[target|display_text]] or [[target]]
Wiki image format: {{namespace:images:image_name.png|alt_text}} or {{namespace:images:image_name.png}}
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
from difflib import SequenceMatcher


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


def extract_wiki_images(content: str) -> List[Tuple[str, str]]:
    """
    Extract wiki image references from content.

    Returns a list of tuples (image_path, alt_text) where image_path is the
    path to the image in the format namespace:images:image_name.png format.
    """
    # Pattern to match {{namespace:images:image_name.png|alt_text}} or {{namespace:images:image_name.png}}
    # Handles both with and without alt text
    pattern = r'\{\{([^\}]+)\}\}'

    images = []
    for match in re.finditer(pattern, content):
        image_content = match.group(1)
        if '|' in image_content:
            path, alt_text = image_content.split('|', 1)
            path = path.strip()
            alt_text = alt_text.strip()
        else:
            path = image_content.strip()
            alt_text = path.split('/')[-1]  # Use filename as alt text if not provided

        # Only consider it an image if it has the namespace:images: pattern
        if ':' in path and 'images:' in path:
            # Format: namespace:images:image_name.png -> namespace:image_name.png
            # For comparison with media files
            parts = path.split(':')
            if len(parts) >= 2 and parts[1].startswith('images/'):
                # Convert to path relative to media directory
                namespace = parts[0]
                image_file = parts[1].split('/', 1)[1]  # Get the actual image filename
                full_path = f"{namespace}/{image_file}"
                images.append((full_path, alt_text))

    return images


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


def get_existing_images(media_dir: Path) -> Set[str]:
    """
    Get a set of all existing images by scanning the media directory.
    """
    existing_images = set()

    for root, dirs, files in os.walk(media_dir):
        for file in files:
            if file.lower().endswith(('.png', '.jpg', '.jpeg', '.gif', '.svg', '.bmp', '.webp')):
                file_path = Path(root) / file
                # Convert file path to image reference format
                # e.g., media/rpd/images/amulet_sprite.png -> rpd/amulet_sprite.png
                # e.g., media/images/start.png -> /start.png (root namespace)

                # Get relative path from media directory
                rel_path = file_path.relative_to(media_dir)

                # Convert to image reference name
                parts = rel_path.parts
                if len(parts) >= 2:
                    # Namespaced image like rpd/images/amulet_sprite.png -> rpd/amulet_sprite.png
                    namespace = parts[0]
                    image_file = parts[-1]  # Last part is the image filename
                    image_name = f"{namespace}/{image_file}"
                elif len(parts) == 1:
                    # Root-level image like images/start.png -> /start.png (no namespace)
                    image_name = f"/{parts[0]}"

                existing_images.add(image_name)

    return existing_images


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


def build_wiki_map(wiki_data_dir: Path) -> Tuple[Dict[str, List[Tuple[str, str]]], List[Tuple[str, str, str]], Set[str], List[Tuple[str, str, str]], Set[str]]:
    """
    Build a complete wiki map showing all page relationships and image references.

    Returns:
    - Dictionary mapping page names to lists of (target, display_text) tuples
    - List of red links (page_path, link_target, display_text)
    - Set of all existing pages
    - List of missing images (page_path, image_path, alt_text)
    - Set of all existing images
    """
    pages_dir = wiki_data_dir / 'pages'
    media_dir = wiki_data_dir / 'media'

    if not pages_dir.exists():
        print(f"Error: Pages directory {pages_dir} does not exist")
        return {}, [], set(), [], set()

    if not media_dir.exists():
        print(f"Error: Media directory {media_dir} does not exist")
        return {}, [], set(), [], set()

    existing_pages = get_existing_pages(pages_dir)
    existing_images = get_existing_images(media_dir)
    page_links = defaultdict(list)  # Maps page_path to list of (target, display_text) tuples
    red_links = []
    missing_images = []

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

                # Extract images from the content
                images = extract_wiki_images(content)

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

                # Process images from the content
                for image_path, alt_text in images:
                    # Check if the image exists in the media directory
                    if image_path not in existing_images:
                        # This is a missing image
                        missing_images.append((str(file_path), image_path, alt_text))

    return dict(page_links), red_links, existing_pages, missing_images, existing_images


def generate_backlinks(page_links: Dict[str, List[Tuple[str, str]]], existing_pages: Set[str], target_page: str = None):
    """
    Generate backlinks - show which pages link to each page.

    Args:
        page_links: Dictionary mapping page names to lists of (target, display_text) tuples
        existing_pages: Set of all existing pages
        target_page: Optional specific page to show backlinks for (if None, shows all pages)
    """
    # Create a mapping from target pages to pages that link to them
    backlinks = {}

    # Initialize backlinks for all existing pages
    for page in existing_pages:
        backlinks[page] = set()

    # Go through each source page and its links
    for source_page, links in page_links.items():
        for target, display_text in links:
            # Add the source page to the backlinks of the target page
            if target in backlinks:
                backlinks[target].add(source_page)
            else:
                # If the target page doesn't exist, ensure it's in the backlinks map anyway
                if target not in backlinks:
                    backlinks[target] = set()
                backlinks[target].add(source_page)

    if target_page:
        # Show backlinks for only the specific target page
        print(f"\nBacklinks for page '{target_page}':")
        print("=" * 80)

        if target_page in backlinks:
            linking_pages = sorted(backlinks[target_page])
            if linking_pages:
                print(f"\n'{target_page}' is linked from:")
                for linker in linking_pages:
                    print(f"  - {linker}")
            else:
                print(f"\n'{target_page}' has no backlinks")
        else:
            print(f"\n'{target_page}' does not exist in the wiki")
    else:
        # Show backlinks for all pages (original behavior)
        print(f"\nBacklinks for {len(backlinks)} pages:")
        print("=" * 80)

        for page in sorted(backlinks.keys()):
            linking_pages = sorted(backlinks[page])
            if linking_pages:
                print(f"\n'{page}' is linked from:")
                for linker in linking_pages:
                    print(f"  - {linker}")
            else:
                print(f"\n'{page}' has no backlinks")


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
    page_links, red_links, _, _, _ = build_wiki_map(wiki_data_dir)
    return red_links


def find_missing_images(wiki_data_dir: Path) -> List[Tuple[str, str, str]]:
    """
    Find all missing images in the wiki data.

    Returns a list of tuples (page_path, image_path, alt_text) for missing images.
    """
    _, _, _, missing_images, _ = build_wiki_map(wiki_data_dir)
    return missing_images


def find_similar_pages(red_links: List[Tuple[str, str, str]], existing_pages: Set[str], similarity_threshold: float = 0.6) -> Dict[str, List[str]]:
    """
    Find pages with names similar to missing links.

    Args:
        red_links: List of red links (page_path, link_target, display_text)
        existing_pages: Set of all existing pages
        similarity_threshold: Minimum similarity ratio (0-1) to consider a match

    Returns:
        Dictionary mapping missing page names to lists of similar existing page names
    """
    similar_pages = {}

    # Get unique missing page names from red links
    missing_page_names = set()
    for _, link_target, _ in red_links:
        missing_page_names.add(link_target)

    # For each missing page, find similar existing pages
    for missing_page in missing_page_names:
        similar = []
        for existing_page in existing_pages:
            # Calculate similarity ratio between missing page and existing page
            similarity = SequenceMatcher(None, missing_page.lower(), existing_page.lower()).ratio()
            if similarity >= similarity_threshold:
                similar.append((existing_page, similarity))

        # Sort by similarity (highest first) and store in the dictionary
        if similar:
            similar.sort(key=lambda x: x[1], reverse=True)
            similar_pages[missing_page] = [page for page, _ in similar]

    return similar_pages


def main():
    parser = argparse.ArgumentParser(description="Analyze wiki data to find red links, missing images and build wiki map")
    parser.add_argument("--dir", default="wiki-data", help="Wiki data directory (default: wiki-data)")
    parser.add_argument("--output", choices=["red-links", "missing-images", "wiki-map", "dot", "backlinks", "all", "similar"],
                       default="red-links", help="Output format: red-links (default), missing-images, wiki-map, dot (graphviz), backlinks, similar, or all")
    parser.add_argument("--graph-file", default="wiki_map.dot", help="Output file for DOT graph (default: wiki_map.dot)")
    parser.add_argument("--red-only", action="store_true", help="In DOT output, show only red links")
    parser.add_argument("--page", help="Specific page to analyze (for backlinks, shows only backlinks to this page)")
    parser.add_argument("--similarity-threshold", type=float, default=0.6, help="Minimum similarity ratio (0-1) to consider a match (default: 0.6)")

    args = parser.parse_args()

    wiki_data_dir = Path(args.dir)

    if not wiki_data_dir.exists():
        print(f"Error: Wiki data directory {wiki_data_dir} does not exist")
        return

    print(f"Analyzing wiki data in {wiki_data_dir}...")
    page_links, red_links, existing_pages, missing_images, existing_images = build_wiki_map(wiki_data_dir)

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

    if args.output in ["missing-images", "all"]:
        print(f"\nFound {len(missing_images)} missing images:")
        print("-" * 80)

        # Group by source page for better readability
        from collections import defaultdict
        grouped = defaultdict(list)
        for page_path, image_path, alt_text in missing_images:
            grouped[page_path].append((image_path, alt_text))

        for page_path, images in grouped.items():
            print(f"\nFrom {page_path}:")
            for image_path, alt_text in images:
                print(f"  -> {{rpd:images:{image_path}|{alt_text}}} -> Image '{image_path}' does not exist")

        if not missing_images:
            print("\nNo missing images found! All wiki image references point to existing images.")

    if args.output in ["wiki-map", "all"]:
        print_wiki_map(page_links, existing_pages)

    if args.output in ["backlinks", "all"]:
        generate_backlinks(page_links, existing_pages, args.page)

    if args.output in ["dot", "all"]:
        generate_dot_graph(page_links, args.graph_file, show_red_links_only=args.red_only)

    if args.output in ["similar", "all"]:
        print(f"\nFinding pages similar to missing links (threshold: {args.similarity_threshold}):")
        print("-" * 80)

        # Count occurrences of each missing page in red links
        missing_page_counts = {}
        for _, link_target, _ in red_links:
            missing_page_counts[link_target] = missing_page_counts.get(link_target, 0) + 1

        similar_pages = find_similar_pages(red_links, existing_pages, args.similarity_threshold)

        if similar_pages:
            print(f"\nFound similar pages for {len(similar_pages)} missing links:")

            # Sort missing pages by count in descending order
            sorted_missing_pages = sorted(similar_pages.keys(),
                                        key=lambda x: missing_page_counts.get(x, 0),
                                        reverse=True)

            for missing_page in sorted_missing_pages:
                print(f"\nMissing page: '{missing_page}' (referenced {missing_page_counts.get(missing_page, 0)} times)")
                print("  Similar existing pages:")
                for similar_page in similar_pages[missing_page][:5]:  # Show top 5 matches
                    print(f"    - {similar_page}")
        else:
            print("\nNo similar pages found for missing links.")

    print(f"\nChecked against {len(existing_pages)} existing pages and {len(existing_images)} existing images.")
    print(f"Wiki map contains {len(page_links)} pages with {sum(len(links) for links in page_links.values())} total links.")
    print(f"Found {len(missing_images)} missing images from {len(set(p[0] for p in missing_images))} different pages.")


if __name__ == "__main__":
    main()