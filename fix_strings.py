import os
import re
import xml.etree.ElementTree as ET
from pathlib import Path

# The file name your project uses for strings
TARGET_FILE = "strings_all.xml"

def extract_reference_types(ref_path):
    """
    Parses the reference strings_all.xml and extracts the format types.
    Returns a dictionary: { "string_name": { 1: 'd', 2: 's' } }
    """
    ref_types = {}

    try:
        tree = ET.parse(ref_path)
        root = tree.getroot()
    except Exception as e:
        print(f"⚠️ Failed to parse reference file {ref_path}: {e}")
        return ref_types

    # Regex to extract Java format specifiers (e.g., %1$d, %s, %2$.2f)
    # Extracts the optional index (e.g., '1$') and the conversion type (e.g., 'd')
    format_pattern = re.compile(r'%(?P<index>[1-9]\d*\$)?[-#+ 0,]*(?P<width>\d+)?(?P<precision>\.\d+)?(?P<type>[a-zA-Z])')

    def parse_text(text, name):
        if not text: return
        implicit_idx = 1
        placeholders = {}

        for match in format_pattern.finditer(text):
            ctype = match.group('type')
            # Ignore newlines and time/date prefixes
            if ctype in ['n', 't', 'T']:
                continue

            idx_str = match.group('index')
            if idx_str:
                idx = int(idx_str[:-1]) # Remove the '$'
            else:
                idx = implicit_idx
                implicit_idx += 1

            placeholders[idx] = ctype

        if placeholders:
            if name not in ref_types:
                ref_types[name] = {}
            ref_types[name].update(placeholders)

    # Search for all strings, string-arrays, and plurals
    for child in root:
        name = child.get('name')
        if not name:
            continue

        if child.tag.endswith('string'):
            parse_text("".join(child.itertext()), name)
        elif child.tag.endswith('plurals') or child.tag.endswith('string-array'):
            for item in child.findall('item'):
                parse_text("".join(item.itertext()), name)

    return ref_types


def fix_all_strings_files():
    root_dir = Path('.')

    # 1. Find all reference files (default language)
    ref_files = list(root_dir.rglob(f"values/{TARGET_FILE}"))

    if not ref_files:
        print(f"❌ No 'values/{TARGET_FILE}' found. Are you in an Android project root?")
        return

    # Regex to find typos like %1 or %2s missing the '$'
    bad_pattern = re.compile(r'(?<!%)%([1-9]\d*)(?![\$])([a-zA-Z])?')

    # Regex to extract the name attribute above the text
    name_pattern = re.compile(r'name="([^"]+)"')

    total_fixed_files = 0
    total_replacements = 0

    print("--- Smart Android Strings Auto-Fixer ---")

    # Process each Android resource directory found (supports multi-module projects)
    for ref_file in ref_files:
        res_dir = ref_file.parent.parent
        print(f"\n📌 Loading reference types from: {ref_file.relative_to(root_dir)}")

        # 2. Get argument types from reference language
        ref_types = extract_reference_types(ref_file)

        locale_files = list(res_dir.rglob(TARGET_FILE))

        for xml_file in locale_files:
            # Skip the reference language itself
            if xml_file.parent.name == 'values':
                continue

            try:
                with open(xml_file, 'r', encoding='utf-8') as f:
                    content = f.read()

                # Find all string names and all bad placeholders in the file
                names = list(name_pattern.finditer(content))
                matches = list(bad_pattern.finditer(content))

                if not matches:
                    continue

                changes_made = []

                # 3. Use types from reference to fix
                def replacement(match):
                    bad_idx = int(match.group(1))
                    user_type = match.group(2)

                    # Find the nearest string 'name' preceding this bad placeholder
                    current_name = None
                    for nm in reversed(names):
                        if nm.start() < match.start():
                            current_name = nm.group(1)
                            break

                    # Resolve correct type from reference map
                    expected_type = 's' # safe fallback
                    if current_name and current_name in ref_types and bad_idx in ref_types[current_name]:
                        expected_type = ref_types[current_name][bad_idx]
                    elif user_type:
                        expected_type = user_type

                    old_str = match.group(0)
                    new_str = f"%{bad_idx}${expected_type}"

                    changes_made.append((current_name or "Unknown", old_str, new_str))
                    return new_str

                # Apply the replacements
                new_content = bad_pattern.sub(replacement, content)

                # Save the fixed file
                with open(xml_file, 'w', encoding='utf-8') as f:
                    f.write(new_content)

                rel_path = xml_file.relative_to(root_dir)
                print(f"  ✅ Fixed {len(changes_made)} issue(s) in: {rel_path}")
                for change in changes_made:
                    print(f"      [{change[0]}] changed {change[1]} -> {change[2]}")

                total_fixed_files += 1
                total_replacements += len(changes_made)

            except Exception as e:
                print(f"  ⚠️ Error processing {xml_file}: {e}")

    print("\n" + "="*45)
    print("🎯 SMART SUMMARY")
    print("="*45)
    print(f"Files modified:       {total_fixed_files}")
    print(f"Placeholders fixed:   {total_replacements}")
    print("="*45)

if __name__ == "__main__":
    fix_all_strings_files()