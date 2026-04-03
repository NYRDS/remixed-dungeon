# Alchemy System Test Suite

Comprehensive testing infrastructure for testing the alchemy system in Remixed Dungeon.

## Files

- `test_alchemy.py` - Main Python test script with 42 tests
- `run_alchemy_test.sh` - Bash wrapper script with auto-start functionality
- `game_client.py` - Base client library (shared with other tests)

## Quick Start

### Option 1: Using the wrapper script (recommended)

```bash
# Test all alchemy functionality (42 tests)
./run_alchemy_test.sh

# Test specific category
./run_alchemy_test.sh --category items

# Test specific category
./run_alchemy_test.sh --category mobs

# Use different port
./run_alchemy_test.sh --port 8082
```

The wrapper script will automatically detect if the server is running and offer to start it.

### Option 2: Direct Python script

```bash
# Start the game with webserver first
./gradlew -p RemixedDungeonDesktop runDesktopGameWithWebServer

# Then run tests
python3 test_alchemy.py
```

## Usage

### Python Script Options

```
python3 test_alchemy.py [--port PORT] [--host HOST] [--category CATEGORY] [--verbose] [--json]
```

| Option | Description | Default |
|--------|-------------|---------|
| `--host` | WebServer host | localhost |
| `--port` | WebServer port | 8080 |
| `--category` | Test specific category only | All categories |
| `--verbose`, `-v` | Verbose output | False |
| `--json` | Output results as JSON | False |

### Examples

```bash
# Test all alchemy functionality (42 tests)
python3 test_alchemy.py

# Test only item recipes
python3 test_alchemy.py --category items

# Test only mob recipes
python3 test_alchemy.py --category mobs

# Test with verbose output
python3 test_alchemy.py -v

# Output results as JSON for CI/CD integration
python3 test_alchemy.py --json > results.json

# Use custom port
python3 test_alchemy.py --port 8082
```

## Test Categories

The test suite is organized into 6 categories with a total of 42 tests:

### Category A: Recipe Loading & Validation (8 tests)

Tests that verify the alchemy recipe system is properly initialized and validated.

| Test | Description |
|------|-------------|
| `test_json_recipes_loaded` | Verify 16 recipes from JSON are loaded |
| `test_lua_recipes_loaded` | Verify Lua recipes are registered |
| `test_auto_mob_recipes_exist` | Verify auto-generated mob resurrection recipes exist |
| `test_recipe_count_matches_expected` | Total recipe count is correct |
| `test_recipe_inputs_exist` | All ingredients are valid entities |
| `test_recipe_outputs_exist` | All outputs are valid entities |
| `test_invalid_recipes_rejected` | Malformed recipes are rejected at load time |
| `test_duplicate_recipes_handled` | No duplicate recipes exist |

### Category B: Recipe Matching (6 tests)

Tests that verify recipe lookup and ingredient matching logic.

| Test | Description |
|------|-------------|
| `test_exact_ingredient_match` | Exact ingredient match succeeds |
| `test_ingredient_count_validation` | Wrong ingredient count fails |
| `test_order_independence` | Ingredient order doesn't matter |
| `test_case_sensitivity` | Case handling is correct |
| `test_partial_ingredients_fail` | Missing ingredients cause failure |
| `test_carcass_normalization` | "Carcass of X" is normalized to "X" |

### Category C: Recipe Execution - Items (10 tests)

Tests that verify item crafting recipes work correctly.

| Test | Description |
|------|-------------|
| `test_rat_armor_recipe` | 1×VileEssence + 1×RatCarcass → RatArmor |
| `test_vile_essence_creation` | 1×BoneShard + 1×RottenOrgan + 1×ToxicGland → VileEssence |
| `test_goo_armor_recipe` | 4×GooCarcass + 1×VileEssence → GooArmor |
| `test_high_count_ingredients` | 100×VileEssence in recipe (PseudoRat) |
| `test_ingredients_consumed` | Verify ingredients are removed from inventory |
| `test_output_item_created` | Verify output item appears in inventory |
| `test_inventory_updated` | Inventory correctly reflects changes |
| `test_sequential_crafting` | Craft same recipe multiple times |
| `test_multiple_outputs` | Handle recipes with multiple outputs |
| `test_complex_item_recipes` | Test complex recipes (e.g., SacrificialSword) |

### Category D: Recipe Execution - Mobs (6 tests)

Tests that verify mob resurrection recipes work correctly.

| Test | Description |
|------|-------------|
| `test_rat_resurrection` | 5×RatCarcass + 1×VileEssence → Rat |
| `test_tiered_mob_resurrection` | Different VileEssence requirements for different mob tiers |
| `test_zombie_recipe` | RottenMeat + BoneShard → Zombies |
| `test_brute_recipe` | GnollCarcass + Shield + VileEssence → Brute |
| `test_mob_becomes_pet` | Resurrected mobs become pets |
| `test_mob_spawn_position` | Mobs spawn at valid positions on the level |

### Category E: Bulk Operations (4 tests)

Tests that verify bulk crafting functionality (following WndItemAlchemy pattern).

| Test | Description |
|------|-------------|
| `test_craft_x5` | Craft recipe 5 times in one call |
| `test_craft_x10` | Craft recipe 10 times in one call |
| `test_craft_x50` | Craft recipe 50 times in one call |
| `test_bulk_inventory_sufficient` | Verify inventory checks for bulk operations |

### Category F: Edge Cases (8 tests)

Tests that verify error handling and edge cases.

| Test | Description |
|------|-------------|
| `test_insufficient_ingredients` | Missing ingredients → error |
| `test_invalid_ingredient_name` | Invalid ingredient name → error |
| `test_nonexistent_item_in_recipe` | Invalid items in recipes → rejected at load time |
| `test_empty_recipe_inputs` | No ingredients → error |
| `test_craft_without_hero` | Crafting without hero → error |
| `test_craft_without_level` | Mob spawning without level → error |
| `test_malformed_recipe_data` | Malformed recipe data → error |
| `test_wrong_ingredient_types` | Incompatible ingredient types → error |

## Test Output

### Console Output

The script provides detailed console output:

```
================================================================================
Running All Alchemy Tests (42 tests)
================================================================================

--- Category A: Recipe Loading & Validation (8 tests) ---
✓ Validation::JSON recipes loaded
  Loaded 48 recipes (expected >= 16)
✓ Validation::Lua recipes loaded
  Total recipes: 48
✓ Validation::Auto mob recipes exist
  Found 32 mob resurrection recipes
...

--- Category B: Recipe Matching (6 tests) ---
✓ Matching::Exact ingredient match
  Recipe matched successfully
...

================================================================================
Test Summary:
  Total: 42
  Passed: 40
  Failed: 2

Failed tests:
  ✗ Execution-Items::High count ingredients: Insufficient VileEssence
  ✗ Edge Cases::Craft without hero: Hero not initialized

Duration: 15.32s
================================================================================
```

### JSON Output

With `--json` flag, results are output in JSON format:

```json
[
  {
    "category": "Validation",
    "test_name": "JSON recipes loaded",
    "success": true,
    "message": "Loaded 48 recipes (expected >= 16)",
    "duration": 0.123
  },
  {
    "category": "Execution-Items",
    "test_name": "RatArmor recipe",
    "success": false,
    "message": "Insufficient VileEssence",
    "duration": 0.456
  }
]
```

## Alchemy System Overview

### Recipe Structure

Alchemy recipes are defined in `scripts/alchemy_recipes.json` and can also be registered via Lua scripts (`scripts/alchemy_recipes.lua`).

**Recipe Format:**
```json
{
  "input": [
    {"name": "VileEssence", "count": 1},
    {"name": "Carcass of Rat", "count": 1}
  ],
  "outputs": [
    {"name": "RatArmor", "count": 1}
  ]
}
```

### Entity Types

The alchemy system supports three entity types:
- **ITEM**: Regular items (weapons, armor, potions, etc.)
- **MOB**: Mobs/creatures that can be resurrected as pets
- **CARCASS**: Carcass items dropped by mobs, used as ingredients

### Key Ingredients

| Ingredient | Purpose |
|------------|---------|
| `VileEssence` | Key ingredient for mob resurrection and high-tier items |
| `Carcass of [Mob]` | Dropped by mobs, used for resurrection and crafting |
| `BoneShard` | Common crafting ingredient |
| `RottenOrgan` | Common crafting ingredient |
| `ToxicGland` | Common crafting ingredient |
| `RottenMeat` | Used for zombie creation |

### Recipe Types

1. **Item Crafting**: Create items from ingredients
   - Simple: 2-3 ingredients → 1 item
   - Complex: Many ingredients → powerful item
   - Examples: VileEssence, RatArmor, SacrificialSword

2. **Mob Resurrection**: Resurrect mobs as pets
   - 5× carcass + X× VileEssence → mob
   - X varies based on mob power/level
   - Auto-generated for all valid mobs

3. **Bulk Operations**: Craft multiple times at once
   - Follows WndItemAlchemy pattern: x5, x10, x50
   - Efficient for mass crafting

## API Endpoints

### List All Recipes

```bash
GET /debug/alchemy/list_recipes
```

**Response:**
```json
{
  "count": 48,
  "recipes": [
    {
      "inputs": [
        {"name": "VileEssence", "count": 1},
        {"name": "Carcass of Rat", "count": 1}
      ],
      "outputs": [
        {"name": "RatArmor", "count": 1}
      ]
    }
  ]
}
```

### Get Recipe by Ingredients

```bash
GET /debug/alchemy/get_recipe?ingredient=VileEssence&ingredient=Carcass%20of%20Rat
```

**Response:**
```json
{
  "success": true,
  "inputs": [
    {"name": "VileEssence", "count": 1},
    {"name": "Carcass of Rat", "count": 1}
  ],
  "outputs": [
    {"name": "RatArmor", "count": 1}
  ]
}
```

### Craft Items/Mobs

```bash
GET /debug/alchemy/craft?ingredient=VileEssence&ingredient=Carcass%20of%20Rat&times=1
```

**Response:**
```json
{
  "success": true,
  "message": "Crafting 1x recipe",
  "times": 1,
  "outputs": [
    {"name": "RatArmor", "count": 1}
  ]
}
```

### Get Inventory

```bash
GET /debug/alchemy/get_inventory
```

**Response:**
```json
{
  "count": 5,
  "inventory": [
    {"name": "VileEssence", "quantity": 10},
    {"name": "BoneShard", "quantity": 5}
  ]
}
```

### Give Item (Test Setup)

```bash
GET /debug/alchemy/give_item?type=VileEssence&count=10
```

**Response:**
```json
{
  "success": true,
  "message": "Gave 10x VileEssence to hero",
  "type": "VileEssence",
  "count": 10
}
```

## Troubleshooting

### Server Not Running

If you see "Cannot connect to server", start the game with webserver:

```bash
./gradlew -p RemixedDungeonDesktop runDesktopGameWithWebServer
```

Or use the helper script:
```bash
./tests/http_api/start_game_server.sh
```

### Game Not Started

The test script automatically starts a game. If this fails, manually start a game via:

```bash
curl "http://localhost:8080/debug/start_game?class=WARRIOR&difficulty=0"
```

### Recipe Not Found

If you see "No recipe found for the given ingredients":
- Check ingredient names are exact (case-sensitive)
- Verify the recipe exists in `scripts/alchemy_recipes.json`
- Check that you have all required ingredients in the correct quantities

### Insufficient Ingredients

If you see "Insufficient ingredients":
- Use `/debug/alchemy/give_item` to add ingredients
- Check your inventory with `/debug/alchemy/get_inventory`
- Verify ingredient counts match recipe requirements

## CI/CD Integration

For automated testing in CI/CD pipelines:

```yaml
# Example GitHub Actions workflow
- name: Start Game Server
  run: ./gradlew -p RemixedDungeonDesktop runDesktopGameWithWebServer &
  background: true

- name: Wait for Server
  run: sleep 30

- name: Run Alchemy Tests
  run: python3 tests/http_api/test_alchemy.py --json > alchemy_results.json

- name: Upload Results
  if: always()
  uses: actions/upload-artifact@v3
  with:
    name: alchemy-test-results
    path: alchemy_results.json
```

## Expected Test Count

| Category | Tests |
|----------|-------|
| Validation | 8 |
| Matching | 6 |
| Execution-Items | 10 |
| Execution-Mobs | 6 |
| Bulk Operations | 4 |
| Edge Cases | 8 |
| **Total** | **42** |

## Contributing

When adding new alchemy recipes:
1. Update `scripts/alchemy_recipes.json`
2. Add corresponding tests to `test_alchemy.py`
3. Update this documentation
4. Run the full test suite to ensure no regressions

## See Also

- [Main README](README.md) - General HTTP API documentation
- [All Spells Test Documentation](README_ALL_SPELLS_TEST.md) - Spell system tests
- [Alchemy System Documentation](../../../wiki-data/pages/en/rpd/alchemy.txt) - Game mechanics
- [Alchemy Recipes](../../../scripts/alchemy_recipes.json) - Recipe definitions
