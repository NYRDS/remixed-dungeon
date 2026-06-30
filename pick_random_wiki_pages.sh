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

    # Find all .txt files in the wiki directory, excluding images, fan content, and known documentation pages
    # Documentation pages are identified by specific filenames that are known to be overview/guide/reference pages
    local all_pages=()
    while IFS= read -r -d '' file; do
        all_pages+=("$file")
    done < <(find "$wiki_dir" -name "*.txt" \
        -not -path "*/images/*" \
        -not -path "*/fan/*" \
        -not -name "fan_stories.txt" \
        -not -name "fanart.txt" \
        -not -name "start.txt" \
        -not -name "sidebar.txt" \
        -not -name "welcome.txt" \
        -not -name "changelog.txt" \
        -not -name "faq.txt" \
        -not -name "documentation.txt" \
        -not -name "verification_report.txt" \
        -not -name "rpd_mission.txt" \
        -not -name "rpd_stats.txt" \
        -not -name "ai_wiki.txt" \
        -not -name "ai_wiki_tools_suggestions.txt" \
        -not -name "privacy_policy.txt" \
        -not -name "donate.txt" \
        -not -name "test_gitbacked.txt" \
        -not -name "bugs_and_ideas.txt" \
        -not -name "thoughts.txt" \
        -not -name "buffs.txt" \
        -not -name "debuffs.txt" \
        -not -name "artifacts.txt" \
        -not -name "weapons.txt" \
        -not -name "armor.txt" \
        -not -name "potions.txt" \
        -not -name "potions_overview.txt" \
        -not -name "scrolls.txt" \
        -not -name "rings.txt" \
        -not -name "wands.txt" \
        -not -name "spells_overview.txt" \
        -not -name "mobs.txt" \
        -not -name "enemies_overview.txt" \
        -not -name "items.txt" \
        -not -name "bosses.txt" \
        -not -name "challenges.txt" \
        -not -name "achievements.txt" \
        -not -name "glyphs.txt" \
        -not -name "enchantments.txt" \
        -not -name "curses.txt" \
        -not -name "status_effects.txt" \
        -not -name "combat.txt" \
        -not -name "crafting.txt" \
        -not -name "cooking.txt" \
        -not -name "stealth.txt" \
        -not -name "dual_wielding.txt" \
        -not -name "leveling.txt" \
        -not -name "experience_system.txt" \
        -not -name "energy_system.txt" \
        -not -name "inventory.txt" \
        -not -name "equipment.txt" \
        -not -name "identification.txt" \
        -not -name "damage_over_time.txt" \
        -not -name "control_effects.txt" \
        -not -name "immunities.txt" \
        -not -name "resistances.txt" \
        -not -name "mechanics.txt" \
        -not -name "melee_combat.txt" \
        -not -name "ranged_combat.txt" \
        -not -name "spellcasting.txt" \
        -not -name "buff_mechanics.txt" \
        -not -name "consumables.txt" \
        -not -name "souls_mechanic.txt" \
        -not -name "modding_getting_started_guide.txt" \
        -not -name "modding_custom_items.txt" \
        -not -name "modding_custom_mobs.txt" \
        -not -name "modding_custom_levels.txt" \
        -not -name "modding_quick_reference.txt" \
        -not -name "modding_non_java_techniques.txt" \
        -not -name "modding_platform_abstraction.txt" \
        -not -name "modding_ui_elements.txt" \
        -not -name "items_modding_guide.txt" \
        -not -name "customitem.txt" \
        -not -name "custommob.txt" \
        -not -name "customizations_system.txt" \
        -not -name "json_configs.txt" \
        -not -name "lua_scripts.txt" \
        -not -name "mods.txt" \
        -not -name "mods_list.txt" \
        -not -name "mods_limitations.txt" \
        -not -name "mods_possibilities.txt" \
        -not -name "mods_scripts.txt" \
        -not -name "mods_mobs.txt" \
        -not -name "mods_objects.txt" \
        -not -name "interface.txt" \
        -not -name "hotkeys.txt" \
        -not -name "hotbars.txt" \
        -not -name "ui.txt" \
        -not -name "quickslots.txt" \
        -not -name "development.txt" \
        -not -name "differences.txt" \
        -not -name "localization.txt" \
        -not -name "arts_by_artemij_nesterov.txt" \
        -not -name "arts_by_sergey_andreev.txt" \
        -not -name "arts_by_yuila_kozlova.txt" \
        -not -name "unsorted_arts.txt" \
        -not -name "pixel_ponies.txt" \
        -not -name "shattered_pixel_dungeon.txt" \
        -not -name "sprouted_pixel_dungeon.txt" \
        -not -name "lovecraft_pixel_dungeon.txt" \
        -not -name "pixel_dungeon_ml.txt" \
        -not -name "pixel_dungeon_plus.txt" \
        -not -name "easy_pixel_dungeon.txt" \
        -not -name "skillful_pixel_dungeon.txt" \
        -not -name "ripped_pixel_dungeon.txt" \
        -not -name "unleashed_pixel_dungeon.txt" \
        -not -name "yet_another_pixel_dungeon.txt" \
        -not -name "your_pixel_dungeon.txt" \
        -not -name "pd_mini.txt" \
        -not -name "phoenix_pixel_dungeon.txt" \
        -not -name "caged_kobold_message1.txt" \
        -not -name "caged_kobold_message2.txt" \
        -not -name "caged_kobold_message3.txt" \
        -print0)
    
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