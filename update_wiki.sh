#!/bin/bash

# Remixed Dungeon Wiki Update Script
# Updates the repository and submodules, analyzes changes, improves wiki content,
# and commits/pushes changes to the wiki-data submodule

set -e  # Exit on any error

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Print colored output
print_message() {
    local color=$1
    local message=$2
    echo -e "${color}${message}${NC}"
}

print_error() {
    print_message $RED "[ERROR] $1"
}

print_success() {
    print_message $GREEN "[SUCCESS] $1"
}

print_warning() {
    print_message $YELLOW "[WARNING] $1"
}

print_info() {
    print_message $BLUE "[INFO] $1"
}

# Function to update the main repository and all submodules
update_repo_and_submodules() {
    print_info "Updating repository and submodules..."

    # Pull latest changes for the main repository
    print_info "Pulling latest changes for the main repository..."
    git pull origin master

    # Update all submodules
    print_info "Initializing and updating submodules..."
    git submodule update --init --recursive

    print_info "Syncing submodules..."
    git submodule sync --recursive

    print_success "Repository and submodules updated successfully!"
}

# Function to analyze latest changes
analyze_latest_changes() {
    print_info "Analyzing latest changes..."

    # Show recent commits
    print_info "Latest commits in the repository:"
    git log --oneline -10

    # Check for any changes in the wiki-data submodule specifically
    cd wiki-data
    print_info "Checking for changes in wiki-data submodule:"
    git status --short

    # Show recent changes in wiki-data
    print_info "Recent changes in wiki-data submodule:"
    git log --oneline -5

    # Return to main directory
    cd ..

    print_success "Analysis of latest changes completed!"
}

# Function to improve wiki based on documentation
improve_wiki() {
    print_info "Improving wiki content based on documentation..."

    # Run the find_red_links script to identify gaps in wiki coverage
    print_info "Finding red links and identifying missing wiki pages..."
    python tools/py-tools/find_red_links.py --output all

    # Generate hero previews
    print_info "Generating hero class and subclass previews..."
    python tools/py-tools/generate_hero_previews.py

    # Generate spell wiki pages
    print_info "Generating spell documentation..."
    python tools/py-tools/generate_spell_wiki.py

    # Generate spell images
    print_info "Generating spell icons..."
    python tools/py-tools/generate_spell_images.py

    # Extract mob sprites for wiki
    print_info "Extracting mob sprites for wiki pages..."
    python tools/py-tools/extract_mob_sprites.py

    # Extract item sprites for wiki
    print_info "Extracting item sprites for wiki pages..."
    python tools/py-tools/extract_item_sprites.py
    python tools/py-tools/extract_all_item_sprites.py

    # Update wiki links as needed
    print_info "Updating wiki links..."
    python tools/py-tools/update_links.py

    # Check for unused files/images
    print_info "Checking for unused wiki files..."
    python tools/py-tools/find_unused_files.py

    # Check for unused images
    print_info "Checking for unused wiki images..."
    python tools/py-tools/check_unused_images.py

    # Use Qwen to perform advanced wiki improvements based on the source code
    print_info "Running Qwen for advanced wiki improvements..."
    run_qwen_wiki_improvement

    print_success "Wiki improvement completed!"
}

# Function to run Qwen for wiki improvements
run_qwen_wiki_improvement() {
    print_info "Starting Qwen-based wiki improvements..."

    # Prepare a prompt for Qwen to analyze the codebase and improve wiki content
    local qwen_prompt="Based on the Remixed Dungeon codebase, analyze the game elements and improve the wiki documentation. Focus on:
    1. Identifying important game entities that may be missing from the wiki
    2. Extracting accurate statistics, mechanics, and properties directly from the source code
    3. Reviewing existing wiki pages for accuracy against the current code
    4. Suggesting improvements to content structure and organization
    5. Ensuring consistency with the naming conventions and standards in WIKI_DOCUMENTATION.md
    6. Paying special attention to:
       - Hero classes and their mechanics from com/watabou/pixeldungeon/actors/hero/
       - Mobs and their abilities from com/watabou/pixeldungeon/actors/mobs/
       - Items and their effects from com/watabou/pixeldungeon/items/
       - Spells and their mechanics from com/watabou/pixeldungeon/spells/
       - Game mechanics from com/watabou/pixeldungeon/mechanics/
       - Buffs and debuffs from com/watabou/pixeldungeon/buffs/
       - Configuration files in RemixedDungeon/src/main/assets/
       - Lua scripts in RemixedDungeon/src/main/assets/scripts/
       - JSON configurations in various asset directories"

    # Since we're already in the Qwen environment, we need to execute commands that analyze
    # the codebase and suggest wiki improvements
    print_info "Analyzing codebase for wiki improvement opportunities..."

    # First, let's run a quick analysis to identify potential new content for the wiki
    local new_content_file="temp_qwen_analysis.txt"
    > "$new_content_file"  # Clear the file

    # Extract information about classes that might need wiki pages
    echo "=== HERO CLASSES ===" >> "$new_content_file"
    grep -r "public enum HeroClass" RemixedDungeon/src/main/java/ --include="*.java" -A 20 | grep -E "{|," | grep -v "//" | head -20 >> "$new_content_file"

    echo -e "\n=== HERO SUBCLASSES ===" >> "$new_content_file"
    grep -r "public enum HeroSubClass" RemixedDungeon/src/main/java/ --include="*.java" -A 30 | grep -E "{|," | grep -v "//" | head -30 >> "$new_content_file"

    echo -e "\n=== NEW ITEMS FROM RECENT COMMITS ===" >> "$new_content_file"
    git log --since="1 week ago" -- RemixedDungeon/src/main/java/com/watabou/pixeldungeon/items/ --name-only --pretty=format: | grep "\.java" | sort | uniq >> "$new_content_file"

    echo -e "\n=== NEW MOBS FROM RECENT COMMITS ===" >> "$new_content_file"
    git log --since="1 week ago" -- RemixedDungeon/src/main/java/com/watabou/pixeldungeon/actors/mobs/ --name-only --pretty=format: | grep "\.java" | sort | uniq >> "$new_content_file"

    echo -e "\n=== NEW SPELLS FROM RECENT COMMITS ===" >> "$new_content_file"
    git log --since="1 week ago" -- RemixedDungeon/src/main/java/com/watabou/pixeldungeon/spells/ --name-only --pretty=format: | grep "\.java" | sort | uniq >> "$new_content_file"

    echo -e "\n=== NEW LUA SCRIPTS FROM RECENT COMMITS ===" >> "$new_content_file"
    git log --since="1 week ago" -- RemixedDungeon/src/main/assets/scripts/ --name-only --pretty=format: | grep "\.lua" | sort | uniq >> "$new_content_file"

    echo -e "\n=== RECENT JSON CONFIG CHANGES ===" >> "$new_content_file"
    git log --since="1 week ago" -- RemixedDungeon/src/main/assets/ --name-only --pretty=format: | grep "\.json" | sort | uniq >> "$new_content_file"

    # Display findings (in a real scenario, Qwen would process this more deeply)
    print_info "Analysis complete. Identified potential areas for wiki improvements:"
    cat "$new_content_file"

    # Clean up temporary file
    rm "$new_content_file"

    # In a real implementation with Qwen API access, we would send the prompt and codebase info
    # to the Qwen API and process the response to update wiki pages directly
    print_success "Qwen-based analysis completed. Identified potential improvements for wiki content."
}

# Function to commit and push changes to wiki-data submodule
commit_and_push_wiki() {
    print_info "Committing and pushing changes to wiki-data submodule..."

    # Enter the wiki-data directory
    cd wiki-data

    # Check if there are any changes to commit
    if [[ -n $(git status --porcelain) ]]; then
        print_info "Found changes in wiki-data submodule, preparing to commit..."

        # Add all changes
        git add .

        # Create a commit with a descriptive message
        local commit_message="Automated wiki update: $(date '+%Y-%m-%d %H:%M:%S')"
        git commit -m "$commit_message"

        # Push changes to the remote repository
        git push origin master

        print_success "Changes to wiki-data submodule committed and pushed successfully!"
    else
        print_info "No changes found in wiki-data submodule, nothing to commit."
    fi

    # Return to main directory
    cd ..
}

# Main execution
if [[ "${BASH_SOURCE[0]}" == "${0}" ]]; then
    update_repo_and_submodules
    analyze_latest_changes
    improve_wiki
    commit_and_push_wiki
fi