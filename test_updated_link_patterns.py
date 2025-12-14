#!/usr/bin/env python3
"""
Test script to validate the updated link replacement patterns
"""

import re

def test_link_replacement():
    old_name = "old_page_name"
    new_name = "new_page_name"
    
    # Test content with various types of links
    test_content = """
    Some text with a link [[rpd:old_page_name|Display Text]] in it.
    Another link with different format [[old_page_name|Text]].
    And another: [[rpd:old_page_name]] without display text.
    And: [[old_page_name]] without display text too.
    A page with underscore: [[rpd:boss_lich_mob|Lich Mob]].
    And: [[boss_lich_mob|Lich]].
    Some other page [[rpd:another_page|Another]] that shouldn't be affected.
    
    Edge cases with our target name:
    Link at start: [[old_page_name|Start link]]
    Link at end with text: this is text [[old_page_name|End link]]
    Link at end without text: this is text [[old_page_name]]
    Partial match that should NOT change: [[boss_old_page_name|Boss link]]
    Another partial: [[old_page_name_boss|Another boss link]]
    """
    
    print("Original content:")
    print(test_content)
    print("\n" + "="*70)
    
    # Apply the same transformations as in the main function
    # Update rpd: links with display text
    pattern1 = rf'\[\[rpd:{re.escape(old_name)}(\|[^\]]+?)\]\]'
    replacement1 = f'[[rpd:{new_name}\\1]]'
    result_content = re.sub(pattern1, replacement1, test_content, flags=re.IGNORECASE)
    print("After updating rpd: links with text:")
    print(result_content)
    print("\n" + "="*70)

    # Update rpd: links without display text
    pattern2 = rf'\[\[rpd:{re.escape(old_name)}\]\]'
    replacement2 = f'[[rpd:{new_name}]]'
    result_content = re.sub(pattern2, replacement2, result_content, flags=re.IGNORECASE)
    print("After updating rpd: links without text:")
    print(result_content)
    print("\n" + "="*70)

    # Update regular links with display text
    pattern3 = rf'\[\[{re.escape(old_name)}(\|[^\]]+?)\]\]'
    replacement3 = f'[[{new_name}\\1]]'
    result_content = re.sub(pattern3, replacement3, result_content, flags=re.IGNORECASE)
    print("After updating regular links with text:")
    print(result_content)
    print("\n" + "="*70)

    # Update regular links without display text (using word boundaries)
    pattern4 = rf'(?<!\w)\[\[{re.escape(old_name)}\]\](?!\w)'
    replacement4 = f'[[{new_name}]]'
    result_content = re.sub(pattern4, replacement4, result_content, flags=re.IGNORECASE)
    print("After updating regular links without text:")
    print(result_content)
    print("\n" + "="*70)
    
    # Count the changes as the function would
    old_rpd_with_text_pattern = rf'\[\[rpd:{re.escape(old_name)}\|[^\]]+\]\]'
    old_rpd_without_text_pattern = rf'\[\[rpd:{re.escape(old_name)}\]\]'
    old_regular_with_text_pattern = rf'\[\[{re.escape(old_name)}\|[^\]]+\]\]'
    old_regular_without_text_pattern = rf'(?<!\w)\[\[{re.escape(old_name)}\]\](?!\w)'
    
    count_rpd_with_text = len(re.findall(old_rpd_with_text_pattern, test_content, re.IGNORECASE))
    count_rpd_without_text = len(re.findall(old_rpd_without_text_pattern, test_content, re.IGNORECASE))
    count_regular_with_text = len(re.findall(old_regular_with_text_pattern, test_content, re.IGNORECASE))
    count_regular_without_text = len(re.findall(old_regular_without_text_pattern, test_content, re.IGNORECASE))
    
    total_changes = (count_rpd_with_text + count_rpd_without_text + 
                     count_regular_with_text + count_regular_without_text)
    
    print(f"\nTotal changes detected: {total_changes}")
    print("Expected: 6 (2 rpd: with text, 2 rpd: without text, 2 regular with text, 0 regular without text)")

if __name__ == "__main__":
    test_link_replacement()