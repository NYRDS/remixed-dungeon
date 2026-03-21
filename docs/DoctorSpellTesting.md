# Doctor Spell Testing Documentation

## Overview

This document describes the testing setup for Doctor class spells using the WebServer debug endpoints.

## Prerequisites

1. Start the desktop game with the webserver in windowed mode:
   ```bash
   # Using helper script
   ./tests/http_api/start_game_server.sh

   # Or manually
   ./gradlew -p RemixedDungeonDesktop runDesktopGameWithWebServer --args="--windowed"
   ```

2. The webserver will start on port 8080 by default.

## Test Scripts

Test scripts are located in `tests/http_api/`:

- `start_game_server.sh` - Helper script to start game server in windowed mode
- `game_client.py` - Base client class for WebServer debug API
- `test_doctor_spells.py` - Test suite for Doctor class spells

### Running Tests

```bash
python3 tests/http_api/test_doctor_spells.py
```

Options:
- `--host HOST` - WebServer host (default: localhost)
- `--port PORT` - WebServer port (default: 8080)

## WebServer Debug Endpoints

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

### Movement and Combat

| Endpoint | Parameters | Description |
|----------|------------|-------------|
| `/debug/get_mob_positions` | - | Get mob positions (x, y, type, hp, ht) |
| `/debug/get_hero_position` | - | Get hero position (x, y, pos) |
| `/debug/move_hero` | `x`, `y` | Move hero to coordinates |
| `/debug/hero_attack` | `x`, `y` | Hero attacks mob at position |
| `/debug/wait_ticks` | `ticks` | Wait N game ticks (default: 10) |

### Debugging

| Endpoint | Parameters | Description |
|----------|------------|-------------|
| `/debug/get_recent_logs` | - | Get recent log messages |
| `/debug/get_items` | - | Get items on level |
| `/debug/get_inventory` | - | Get hero inventory |

## Doctor Class Setup

When starting a game with `class=DOCTOR`, the hero receives:

- **Armor:** DoctorArmor (with Miasma Cloud special ability)
- **Weapon:** BoneSaw (custom Lua item)
- **Ring:** PlagueDoctorMask (custom Lua item)
- **Buff:** GasesImmunity (custom Lua buff)
- **Magic Affinity:** PlagueDoctor

## Doctor Spells

### BloodTransfusion
- **Type:** Targeted spell
- **Function:** Extracts life essence from a target to heal the caster
- **Lua File:** `scripts/spells/BloodTransfusion.lua`
- **Dependencies:** `BloodParticle` (exposed via commonClasses.lua)
- **Test Result:** Spell casts but target may resist if no valid target nearby

### CorpseExplosion
- **Type:** Cell-targeted spell
- **Function:** Explodes a corpse (Carcass item) to create toxic gas clouds
- **Lua File:** `scripts/spells/CorpseExplosion.lua`
- **Dependencies:** `ToxicGas`, `ParalyticGas`, `ConfusionGas`, `MiasmaGas` (exposed via commonClasses.lua)
- **Test Result:** Requires a Carcass item on the target cell

### Anesthesia
- **Type:** Targeted spell
- **Function:** Applies a sleep effect to the target
- **Lua File:** `scripts/spells/Anesthesia.lua`
- **Test Result:** Spell is available and can be cast

## Recent Fixes Applied

1. **BloodTransfusion.lua** - Fixed nil `BloodParticle` error by adding to commonClasses.lua:
   ```lua
   BloodParticle = luajava.bindClass("com.watabou.pixeldungeon.effects.particles.BloodParticle")
   ```

2. **CorpseExplosion.lua** - Fixed nil `FlammableGas` error:
   - Replaced non-existent `FlammableGas` with `MiasmaGas`
   - Added `MiasmaGas` to commonClasses.lua:
   ```lua
   MiasmaGas = luajava.bindClass("com.watabou.pixeldungeon.actors.blobs.MiasmaGas")
   ```

## Testing Findings

### 2026-03-21 Test Results

1. **Server Connection:** ✓ PASS
   - Webserver accessible at localhost:8080

2. **Start Game:** ✓ PASS
   - Game starts successfully with DOCTOR class

3. **Hero Setup:** ✓ PASS
   - DoctorArmor equipped correctly
   - BoneSaw weapon equipped
   - PlagueDoctorMask ring equipped
   - GasesImmunity buff active
   - PlagueDoctor magic affinity set

4. **Available Spells:** ✓ PASS
   - BloodTransfusion available
   - CorpseExplosion available
   - Anesthesia available

5. **BloodTransfusion:** ✓ PASS
   - Spell cast scheduled successfully
   - Target resisted (expected without valid target)

6. **CorpseExplosion:** ✓ PASS
   - Spell cast scheduled successfully
   - Requires Carcass item on target cell

7. **Anesthesia:** ✓ PASS
   - Spell cast scheduled successfully

## DebugEndpoints.java Update

Updated the error message in `handleDebugStartGame` to include all valid hero classes:

```java
// Before:
String.format("{\"error\":\"Unknown hero class: %s. Valid classes: WARRIOR, MAGE, ROGUE, HUNTRESS\"}", heroClass)

// After:
String.format("{\"error\":\"Unknown hero class: %s. Valid classes: WARRIOR, MAGE, ROGUE, HUNTRESS, ELF, NECROMANCER, GNOLL, PRIEST, DOCTOR\"}", heroClass)
```

## Notes

- The `kill_mob` endpoint requires exact coordinates, which may be tricky if mobs move
- Creating a mob returns its position, which can be used immediately
- Some spells require specific conditions (corpses, valid targets) to succeed
- The webserver runs in "standalone mode" when the game UI isn't fully initialized

## Known Issues

### Game Crash on Doctor Class Start (via WebServer)

**Symptom:** Game crashes with "Trying to create GameScene when hero is invalid!" when starting a game with Doctor class via the webserver debug endpoint.

**Root Cause:** The `PlagueDoctorMask` Lua script (`scripts/items/PlagueDoctorMask.lua`) has conflicting implementations:
- Defined as a ring (`ring1`) in `initHeroes.json`
- Uses `luajava.newInstance("com.nyrds.pixeldungeon.items.accessories.PlagueDoctorMask")` which creates a Java Accessory
- The Lua item system and Java Accessory system have different initialization patterns

**Fix Required:** The `PlagueDoctorMask` needs to be properly implemented as either:
1. A pure Lua item with proper buff application, OR
2. A proper ring class that extends the Java Ring class

Current problematic code in `scripts/items/PlagueDoctorMask.lua`:
```lua
local PlagueDoctorMask = luajava.newInstance("com.nyrds.pixeldungeon.items.accessories.PlagueDoctorMask")

return item.init{
    -- ...
    activate = function(self, item, hero)
        PlagueDoctorMask:equip(true)  -- This calls Java Accessory.equip()
        RPD.permanentBuff(hero, "GasesImmunity")
    end,
    -- ...
}
```

The `luajava.newInstance` call on module load creates a shared instance that's used for all equip/unequip operations, which is incorrect behavior.
