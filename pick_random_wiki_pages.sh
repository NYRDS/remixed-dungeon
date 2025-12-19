#!/bin/bash

# Remixed Dungeon Wiki Random Page Picker
# Selects a few random wiki pages from the entire wiki for review or editing

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

# Function to pick random wiki pages
pick_random_wiki_pages() {
    local wiki_dir="./wiki-data/pages"
    local num_pages=${1:-5}  # Default to 5 pages if not specified

    print_info "Picking $num_pages random wiki pages from the entire wiki ($wiki_dir)..."

    # Find all .txt files in the wiki directory, excluding images directory
    local all_pages=()
    while IFS= read -r -d '' file; do
        all_pages+=("$file")
    done < <(find "$wiki_dir" -name "*.txt" -not -path "*/images/*" -print0)
    
    local total_pages=${#all_pages[@]}
    
    if [ $total_pages -eq 0 ]; then
        print_error "No wiki pages found in $wiki_dir"
        return 1
    fi
    
    print_info "Found $total_pages total wiki pages"
    
    # Check that we don't request more pages than exist
    if [ $num_pages -gt $total_pages ]; then
        print_warning "Requested $num_pages pages but only $total_pages exist. Selecting all pages."
        num_pages=$total_pages
    fi
    
    # Select random pages
    print_info "Randomly selected wiki pages:"
    local selected_count=0
    local selected_full_paths=()

    # Create array of indices
    local indices=()
    for ((i=0; i<total_pages; i++)); do
        indices[$i]=$i
    done

    # Shuffle the indices array using Fisher-Yates algorithm
    for ((i=total_pages-1; i>0; i--)); do
        # Generate random index
        local j=$((RANDOM % (i+1)))

        # Swap elements at i and j
        local temp=${indices[$i]}
        indices[$i]=${indices[$j]}
        indices[$j]=$temp
    done

    # Select the first num_pages indices
    for ((i=0; i<num_pages; i++)); do
        local random_index=${indices[$i]}
        local page_path=${all_pages[$random_index]}
        local page_name=$(basename "$page_path")
        echo "  $page_name"
        selected_full_paths+=("$page_path")
    done

    print_success "$num_pages random wiki pages selected successfully!"

    # Option to show full paths
    print_info "Full paths to selected pages:"
    for path in "${selected_full_paths[@]}"; do
        echo "  $path"
    done
}

# Main execution
if [[ "${BASH_SOURCE[0]}" == "${0}" ]]; then
    # Accept number of pages as an argument
    num_pages=${1:-5}

    if [[ -z "$1" ]]; then
        print_info "Usage: $0 [number_of_pages]"
        print_info "Defaulting to 5 random pages"
    fi

    pick_random_wiki_pages "$num_pages"
fi