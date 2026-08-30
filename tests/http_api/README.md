# HTTP API Tests for Remixed Dungeon

This folder contains test scripts for testing the game through the WebServer debug API.

## Prerequisites

1. Start the game with webserver in windowed mode:
   ```bash
   # Using the helper script
   ./tests/http_api/start_game_server.sh

   # Or manually
   ./gradlew -p RemixedDungeonDesktop runDesktopGameWithWebServer --args="--windowed"
   ```

2. The webserver will start on port 8080 by default.

3. Run Tests
```bash
python3 tests/http_api/test_doctor_spells.py
```

Options:
- `--host HOST` - WebServer host (default: localhost)
- `--port PORT` - WebServer port (default: 8080)

## Available Debug Endpoints

Routing is **exact-match** on the URI path (`/debug/foo` matches only that path).
`/ready` answers before the game is initialized; every other endpoint returns
503 until a game is running (call `/debug/start_game` and poll
`/debug/get_game_state` until a `hero` key appears). Responses to POST requests
close the connection (NanoHTTPD 2.3.1 keep-alive desyncs on reused POST
connections).

### LLM / Agent Control Surface

The endpoints an autonomous driver (LLM or script) needs per loop tick. The
full working loop is exercised by `test_llm_control.py` (41 checks).

| Endpoint | Parameters | Description |
|----------|------------|-------------|
| `/debug/observe` | - | **Atomic world frame**: hero vitals (class, level, hp/ht, str, gold, hunger, starving, buffs, pos, current action), visible mobs (id, type, pos, hp, state, distance, owned), visible item heaps, stair cells, levelId/depth/dimensions. One consistent read replaces 5-8 non-atomic calls |
| `/debug/get_map` | `mask` (optional) | Level as grid: `terrain` rows (int codes, same as `get_tile_info`), `passable`/`visible`/`mapped` masks as 0/1 strings per row, `entrance` + `exits` cells. `mask=1` returns terrain `-1` for unexplored-and-unseen cells |
| `/debug/hero_status` | - | Cheap poll: `alive`, `hp`, `ht`, `pos`, `x`, `y`, `action` (current action class or `"idle"`), `levelId`, `depth`. `action=="idle"` is the signal to issue the next command |
| `/debug/move_to` | `x`, `y` (or `cell`) | **Tap on a cell** — resolved by the game's own tap logic (`Hero.handle` → `CharUtils.actionForCell`): empty passable cell → pathfinding walk, hostile mob → Attack, heap → PickUp/OpenChest, exit stairs → Descend, locked door → Unlock, object → InteractObject. Response carries the `resolved` action. Returns 409 + "busy" if the hero is mid-action |

Agent loop pattern (see `test_llm_control.py`):

1. `observe` → decide
2. `move_to` → poll `hero_status` until `action=="idle"`
3. re-`move_to` on idle if the walk was interrupted (visible hostiles
   interrupt tap-walking, same as for a human player)
4. pathing note: the game walks only cells that are `passable && (visited || mapped)`
   — route over `get_map` masks, and `reveal_map` first when targeting
   undiscovered stairs

### Game Control
| Endpoint | Parameters | Description |
|----------|------------|-------------|
| `/debug/start_game` | `class`, `difficulty` | Start a new game (async — poll `get_game_state` until `hero` appears) |
| `/debug/get_game_state` | - | Get current game state |
| `/debug/get_hero_info` | - | Get detailed hero info (raw save bundle — noisy; prefer `observe`/`hero_status`) |
| `/debug/get_level_info` | - | Get current level info |
| `/debug/reload_game` | - | Full save/load cycle (exercises bundle restore path) |

### Spell Testing
| Endpoint | Parameters | Description |
|----------|------------|-------------|
| `/debug/get_available_spells` | - | List all available spells |
| `/debug/cast_spell` | `type` | Cast a spell by name |
| `/debug/cast_spell_on_target` | `type`, `x`, `y` | Cast spell at position |

### Mob Management
| Endpoint | Parameters | Description |
|----------|------------|-------------|
| `/debug/get_mobs` | - | List mobs on level |
| `/debug/create_mob` | `type` | Create a mob |
| `/debug/kill_mob` | `x`, `y` | Kill mob at position |

### Item Management
| Endpoint | Parameters | Description |
|----------|------------|-------------|
| `/debug/get_items` | - | Get items on level |
| `/debug/get_inventory` | - | Get hero inventory |
| `/debug/create_item` | `type`, `x`, `y` | Create an item |
| `/debug/give_item` | `type` | Give item to hero |

### Debugging
| Endpoint | Parameters | Description |
|----------|------------|-------------|
| `/debug/get_recent_logs` | - | Get recent log messages |
| `/debug/set_hero_stat` | `stat`, `value` | Set hero stat |

### Level Control
| Endpoint | Parameters | Description |
|----------|------------|-------------|
| `/debug/change_level` | `depth` | Change dungeon depth |

### Movement and Combat
| Endpoint | Parameters | Description |
|----------|------------|-------------|
| `/debug/get_mob_positions` | - | Get mob positions (x, y, type, hp, ht) |
| `/debug/get_hero_position` | - | Get hero position (x, y, pos) |
| `/debug/move_hero` | `x`, `y` | Move hero to coordinates |
| `/debug/hero_attack` | `x`, `y` | Hero attacks mob at position |
| `/debug/wait_ticks` | `ticks` | Wait N game ticks (default: 10) |

### Level Navigation
| Endpoint | Parameters | Description |
|----------|------------|-------------|
| `/debug/go_to_level` | `id`, `entrance` | Switch to any level by ID |
| `/debug/list_levels` | - | List all available levels |
| `/debug/get_exits` | - | Get exits from current level |
| `/debug/get_entrances` | - | Get entrances to current level |
| `/debug/descend_to` | `id` | Descend to connected level |
| `/debug/ascend` | - | Ascend to previous level |
| `/debug/reveal_map` | - | Reveal whole level (sets visible/visited/mapped) |

### Tile Inspection
| Endpoint | Parameters | Description |
|----------|------------|-------------|
| `/debug/get_tile_info` | `x`, `y` | Terrain, passable, visibility flags, item heap and character at a cell |
| `/debug/handle_cell` | `x`, `y` | Legacy primitive: move-or-attack toward a cell (prefer `move_to`) |
| `/debug/remove_item` | `x`, `y` | Remove item heap at position |
| `/debug/spawn_at` | `entity`, `value`, `x`, `y` | Spawn mob or item at exact coordinates |

### Alchemy System
| Endpoint | Parameters | Description |
|----------|------------|-------------|
| `/debug/alchemy/list_recipes` | - | List all available alchemy recipes |
| `/debug/alchemy/get_recipe` | `ingredient` (multiple) | Get recipe matching ingredients |
| `/debug/alchemy/craft` | `ingredient` (multiple), `times` | Execute recipe N times |
| `/debug/alchemy/get_inventory` | - | Get hero's inventory (for alchemy) |
| `/debug/alchemy/give_item` | `type`, `count` | Give item to hero (for test setup) |

## Hero Classes

Valid values for `/debug/start_game?class=`:
- `WARRIOR`
- `MAGE`
- `ROGUE`
- `HUNTRESS`
- `ELF`
- `NECROMANCER`
- `GNOLL`
- `PRIEST`
- `DOCTOR`
