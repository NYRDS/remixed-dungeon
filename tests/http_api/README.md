# HTTP API Tests for Remixed Dungeon

This folder contains test scripts for testing the game through the WebServer debug API.

## Prerequisites

Start the game with webserver in windowed mode:

```bash
# Using the helper script
./tests/http_api/start_game_server.sh

# Or manually
./gradlew -p RemixedDungeonDesktop runDesktopGameWithWebServer --args="--windowed"
```

## Files

- `start_game_server.sh` - Helper script to start the game server in windowed mode
- `game_client.py` - Base client class for the WebServer debug API
- `test_doctor_spells.py` - Test suite for Doctor class spells

## Running Tests

```bash
# Run Doctor spell tests
python3 tests/http_api/test_doctor_spells.py

# With custom host/port
python3 tests/http_api/test_doctor_spells.py --host 192.168.1.100 --port 8080
```

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
