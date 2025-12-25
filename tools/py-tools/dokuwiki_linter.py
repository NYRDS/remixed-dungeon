#!/usr/bin/env python3
"""
DokuWiki Linter for Remixed Dungeon Wiki

This script validates DokuWiki pages against the standards defined in 
WIKI_DOCUMENTATION.md for the Remixed Dungeon project.
"""

import argparse
import os
import re
import sys
from pathlib import Path
from typing import List, Tuple


class DokuWikiLinter:
    def __init__(self):
        self.errors = []
        self.warnings = []
        self.current_file = ""
        
        # Regex patterns for validation
        self.header_pattern = re.compile(r'^(={2,6})\s.*\s\1$')
        self.link_pattern = re.compile(r'\[\[(?:[a-z0-9_]+:)?[a-z0-9_]+(?:\|[^\]]+)?\]\]')
        self.image_pattern = re.compile(r'\{\{\s*[a-z0-9_/:\.-]+\|(?:[^\}]+\s*)?\}\}')
        self.paragraph_pattern = re.compile(r'\n[^\n]')  # Single newline followed by non-newline
        
        # Valid entity suffixes according to the documentation
        self.valid_suffixes = [
            '_mob', '_class', '_subclass', '_item', '_spell', 
            '_npc', '_level', '_mechanic', '_skill', '_talent', 
            '_buff', '_trap', '_script', '_level_object', '_config', 
            '_quest'
        ]

    def lint_file(self, file_path: str) -> Tuple[List[str], List[str]]:
        """Lint a single DokuWiki file and return errors and warnings."""
        self.current_file = file_path
        self.errors = []
        self.warnings = []
        
        # Check filename
        self._check_filename(file_path)
        
        # Read file content
        try:
            with open(file_path, 'r', encoding='utf-8') as f:
                content = f.read()
        except UnicodeDecodeError:
            self.errors.append(f"File encoding error: {file_path} is not UTF-8 encoded")
            return self.errors, self.warnings
            
        # Check content
        self._check_headers(content)
        self._check_links(content)
        self._check_images(content)
        self._check_paragraphs(content)
        self._check_lists(content)  # Check list formatting
        self._check_entity_suffixes(content)
        self._check_tags(content)
        
        return self.errors, self.warnings

    def _check_filename(self, file_path: str):
        """Check if the filename follows the lowercase with underscores convention."""
        filename = os.path.basename(file_path)
        name_part = filename.replace('.txt', '')
        
        # Check if filename is all lowercase
        if name_part != name_part.lower():
            self.errors.append(f"Filename not in lowercase: {filename}")
        
        # Check if filename uses underscores instead of hyphens or spaces
        if ' ' in name_part or '-' in name_part:
            self.errors.append(f"Filename should use underscores: {filename}")
        
        # Check if filename has valid suffix
        has_valid_suffix = any(name_part.endswith(suffix) for suffix in self.valid_suffixes)
        if not has_valid_suffix and not name_part.startswith('mr:'):
            self.warnings.append(f"Filename might be missing standard suffix: {filename}")

    def _check_headers(self, content: str):
        """Check if headers follow the correct format."""
        lines = content.split('\n')
        for i, line in enumerate(lines, 1):
            line = line.strip()
            if line.startswith('=') and '=' in line[1:]:
                if not self.header_pattern.match(line):
                    self.errors.append(f"Invalid header format at line {i}: {line}")

    def _check_links(self, content: str):
        """Check if internal links follow the correct format."""
        # Find all potential links
        potential_links = re.findall(r'\[\[([^\]]+)\]\]', content)

        for link in potential_links:
            # Skip external links (starting with http:// or https://)
            if link.startswith(('http://', 'https://')):
                continue

            # Check if link contains uppercase letters in the page name part (before | or end)
            page_part = link.split('|')[0] if '|' in link else link
            # Remove namespace if present
            if ':' in page_part:
                namespace, page_name = page_part.split(':', 1)
                if namespace != 'rpd' and not namespace.startswith('mr'):
                    self.warnings.append(f"Non-standard namespace in link: {link}")
                page_part = page_name
            else:
                # If no namespace, the whole thing should be lowercase
                if page_part != page_part.lower() and not page_part.startswith(('http://', 'https://')):
                    self.errors.append(f"Link page name should be lowercase: {link}")

            # Check if page part follows naming convention (only for internal links)
            if not link.startswith(('http://', 'https://')) and (' ' in page_part or page_part != page_part.lower()):
                self.errors.append(f"Link page name doesn't follow naming convention: {link}")

    def _check_images(self, content: str):
        """Check if images follow the correct format and exist in wiki-data/media."""
        # Find all potential image references (excluding tags)
        potential_images = re.findall(r'\{\{[^\}]+\}\}', content)

        for img in potential_images:
            # Skip if it's a tag
            if img.startswith('{{tag>'):
                continue

            if not self.image_pattern.match(img.strip()):
                self.warnings.append(f"Image format may not follow wiki standards: {img}")

            # Check if image name follows naming convention
            # Extract the image name part
            img_match = re.search(r'\{\{\s*([^\}|]+)', img)
            if img_match:
                img_name = img_match.group(1).strip()
                # Check if image name is lowercase
                if img_name != img_name.lower():
                    self.warnings.append(f"Image name should be lowercase: {img_name}")

                # Check if the image file exists in wiki-data/media
                # Extract just the filename part from the path
                # Handle various image reference formats like: rpd:images:imagename.png or rpd/images/imagename.png
                if ':' in img_name or '/' in img_name:
                    # Normalize the path separator to os.sep
                    normalized_path = img_name.replace(':', os.sep).replace('/', os.sep)
                    expected_path = os.path.join('wiki-data', 'media', normalized_path)

                    # Also try the rpd/images structure which is common
                    if not os.path.exists(expected_path):
                        # If it's in the format like rpd:images:imagename.png, extract the filename
                        img_filename = os.path.basename(img_name.replace(':', '/'))
                        alt_path = os.path.join('wiki-data', 'media', 'rpd', 'images', img_filename)

                        if not os.path.exists(alt_path):
                            self.errors.append(f"Image file does not exist: {img_name} (expected at {expected_path} or {alt_path})")

    def _check_paragraphs(self, content: str):
        """Check if paragraphs use proper double newlines."""
        # DokuWiki treats single newlines as spaces, so for paragraph breaks,
        # we need double newlines (which means two consecutive \n characters)
        # Find places where there's a single newline between content lines
        lines = content.split('\n')
        i = 0
        while i < len(lines) - 2:  # Need at least 2 more lines to check
            current_line = lines[i].strip()
            next_line = lines[i+1].strip()
            after_next = lines[i+2].strip() if i+2 < len(lines) else ""

            # If we have content on current and next lines (but not after that, which means it's already a paragraph break)
            if (current_line and next_line and
                not current_line.startswith(('=', '*', '-', '  *', '  -', '^', '|', '{{', '----')) and
                not next_line.startswith(('=', '*', '-', '  *', '  -', '^', '|', '{{', '----'))):

                # This is a single newline between content lines that should probably be a paragraph break
                self.warnings.append(f"Possible missing paragraph break at line {i+1}-{i+2}, consider adding an extra newline")
            i += 1

    def _check_lists(self, content: str):
        """Check if lists are properly formatted with indentation."""
        lines = content.split('\n')
        for i, line in enumerate(lines, 1):
            # Check for list items that are not properly indented
            # In DokuWiki, list items need to be indented to be recognized as lists
            if line.lstrip().startswith('* ') and not line.startswith('  *'):
                # Single * without proper indentation (should be at least 2 spaces)
                self.errors.append(f"List item not properly indented at line {i}: '{line.strip()}'. List items should start with at least 2 spaces before the '*'.")
            elif line.lstrip().startswith('- ') and not line.startswith('  -'):
                # Single - without proper indentation (should be at least 2 spaces)
                self.errors.append(f"List item not properly indented at line {i}: '{line.strip()}'. List items should start with at least 2 spaces before the '-'.")

    def fix_paragraphs(self, content: str) -> str:
        """Fix single paragraph breaks by adding an extra newline between paragraphs."""
        import re

        # Split content into lines
        lines = content.split('\n')
        new_lines = []
        i = 0

        while i < len(lines):
            current_line = lines[i]
            new_lines.append(current_line)

            # Check if we have a next line and both current and next are content lines
            if (i + 1 < len(lines) and
                current_line.strip() and
                lines[i+1].strip() and
                not current_line.strip().startswith(('=', '*', '-', '  *', '  -', '^', '|', '{{', '----')) and
                not lines[i+1].strip().startswith(('=', '*', '-', '  *', '  -', '^', '|', '{{', '----'))):

                # Add an extra newline to create a proper paragraph break
                new_lines.append('')
            i += 1

        return '\n'.join(new_lines)

    def _check_entity_suffixes(self, content: str):
        """Check if content mentions entities that should have proper suffixes."""
        # This is a basic check - in practice, this would need more context
        # to determine if an entity name should have a suffix
        pass

    def _check_tags(self, content: str):
        """Check if tags follow the correct format."""
        tag_pattern = re.compile(r'\{\{tag>\s*[a-z0-9_\s]+\s*\}\}')
        tags = re.findall(r'\{\{tag>[^}]+\}\}', content)

        for tag in tags:
            if not tag_pattern.match(tag):
                # Check if it's a more complex tag with spaces between tags
                tag_content = tag[7:-2]  # Remove {{tag> and }}
                individual_tags = [t.strip() for t in tag_content.split()]
                valid = all(re.match(r'^[a-z0-9_]+$', t) for t in individual_tags if t)
                if not valid:
                    self.warnings.append(f"Tag format may not follow standards: {tag}")

    def lint_directory(self, directory: str) -> Tuple[List[str], List[str]]:
        """Lint all .txt files in a directory."""
        all_errors = []
        all_warnings = []
        
        for root, dirs, files in os.walk(directory):
            for file in files:
                if file.endswith('.txt'):
                    file_path = os.path.join(root, file)
                    errors, warnings = self.lint_file(file_path)
                    all_errors.extend([f"{file_path}: {e}" for e in errors])
                    all_warnings.extend([f"{file_path}: {w}" for w in warnings])
        
        return all_errors, all_warnings


def main():
    parser = argparse.ArgumentParser(description='DokuWiki Linter for Remixed Dungeon Wiki')
    parser.add_argument('path', help='Path to DokuWiki file or directory to lint')
    parser.add_argument('--format', choices=['text', 'json'], default='text',
                       help='Output format (default: text)')
    parser.add_argument('--fix', action='store_true',
                       help='Attempt to fix trivial issues (like paragraph breaks)')

    args = parser.parse_args()

    linter = DokuWikiLinter()

    path = Path(args.path)

    if not path.exists():
        print(f"Error: Path {args.path} does not exist", file=sys.stderr)
        sys.exit(1)

    if path.is_file():
        if args.fix:
            # Read the file content
            try:
                with open(str(path), 'r', encoding='utf-8') as f:
                    original_content = f.read()
            except UnicodeDecodeError:
                print(f"Error: File {str(path)} is not UTF-8 encoded", file=sys.stderr)
                sys.exit(1)

            # Fix paragraphs
            fixed_content = linter.fix_paragraphs(original_content)

            # Write the fixed content back to the file
            with open(str(path), 'w', encoding='utf-8') as f:
                f.write(fixed_content)

            print(f"Fixed paragraph breaks in {str(path)}")
            # Now lint the fixed content
            errors, warnings = linter.lint_file(str(path))
        else:
            errors, warnings = linter.lint_file(str(path))
    elif path.is_dir():
        if args.fix:
            print("Fix option is not supported for directories", file=sys.stderr)
            sys.exit(1)
        errors, warnings = linter.lint_directory(str(path))
    else:
        print(f"Error: Path {args.path} is neither a file nor a directory", file=sys.stderr)
        sys.exit(1)

    # Print results
    if args.format == 'text':
        if errors:
            print("Errors found:")
            for error in errors:
                print(f"  - {error}")
            print()

        if warnings:
            print("Warnings found:")
            for warning in warnings:
                print(f"  - {warning}")
            print()

        if not errors and not warnings:
            print("No issues found! All DokuWiki pages are compliant.")
        else:
            print(f"Found {len(errors)} error(s) and {len(warnings)} warning(s).")
            sys.exit(1 if errors else 0)
    else:
        # JSON format would be implemented here if needed
        import json
        result = {
            "errors": errors,
            "warnings": warnings,
            "summary": {
                "error_count": len(errors),
                "warning_count": len(warnings),
                "status": "fail" if errors else "pass"
            }
        }
        print(json.dumps(result, indent=2))


if __name__ == "__main__":
    main()