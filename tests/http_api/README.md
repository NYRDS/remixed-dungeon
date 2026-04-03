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

### Game Control
| Endpoint | Parameters | Description |
|----------|------------|-------------|
| `/debug/start_game` | `class`, `difficulty` | Start a new game |
| `/debug/get_game_state` | - | Get current game state |
| `/debug/get_hero_info` | - | Get detailed hero info |
| `/debug/get_level_info` | - | Get current level info |

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
