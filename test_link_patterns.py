#!/usr/bin/env python3
"""
Test script to validate regex patterns for wiki link replacement
"""

import re

def test_link_patterns():
    # Test the regex patterns with sample DokuWiki links
    test_content = """
    Some text with a link [[rpd:old_page_name|Display Text]] in it.
    Another link with different format [[old_page_name|Text]].
    And another: [[rpd:old_page_name]] without display text.
    And: [[old_page_name]] without display text too.
    A page with underscore: [[rpd:boss_lich_mob|Lich Mob]].
    And: [[boss_lich_mob|Lich]].
    Some other page [[rpd:another_page|Another]] that shouldn't be affected.
    """
    
    old_name = "old_page_name"
    new_name = "new_page_name"
    
    print("Original content:")
    print(test_content)
    print("\n" + "="*50)
    
    # Update rpd: links (e.g. [[rpd:old_name|Text]] -> [[rpd:new_name|Text]])
    pattern1 = r'\[\[rpd:' + re.escape(old_name) + r'(\|[^\]]+)?\]\]'
    replacement1 = f'[[rpd:{new_name}\\1]]'
    test_content = re.sub(pattern1, replacement1, test_content, flags=re.IGNORECASE)
    print(f"\nAfter updating rpd: links:")
    print(test_content)
    
    # Update regular links (e.g. [[old_name|Text]] -> [[new_name|Text]])
    # This is trickier since we need to avoid partial matches
    # We'll create a more specific pattern
    pattern2 = r'(?<!\w)' + re.escape(old_name) + r'(?!\w)'
    # This needs to work within the context of the [[link]] format
    # Let's rebuild with a better approach:
    
    # Reset content for the second approach
    test_content = """
    Some text with a link [[rpd:old_page_name|Display Text]] in it.
    Another link with different format [[old_page_name|Text]].
    And another: [[rpd:old_page_name]] without display text.
    And: [[old_page_name]] without display text too.
    A page with underscore: [[rpd:boss_lich_mob|Lich Mob]].
    And: [[boss_lich_mob|Lich]].
    Some other page [[rpd:another_page|Another]] that shouldn't be affected.
    """
    
    # More specific patterns that only match inside [[...]] 
    # First, handle rpd: links
    test_content = re.sub(r'\[\[rpd:' + re.escape(old_name) + r'(\|[^\]]+)?\]\]', 
                 f'[[rpd:{new_name}\\1]]', test_content, flags=re.IGNORECASE)
    print(f"\nAfter updating rpd: links (second attempt):")
    print(test_content)
    
    # Then, handle regular links that are whole words
    # Use word boundaries to make sure we match the full page name
    test_content = re.sub(r'\[\[(' + re.escape(old_name) + r')(\|[^\]]+)?\]\]', 
                 f'[[{new_name}\\2]]', test_content, flags=re.IGNORECASE)
    print(f"\nAfter updating regular links (second attempt):")
    print(test_content)


if __name__ == "__main__":
    test_link_patterns()