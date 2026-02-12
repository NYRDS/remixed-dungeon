# Map Generation in Remixed Dungeon

## Overview

Remixed Dungeon uses a sophisticated procedural map generation system that creates diverse and interesting dungeon layouts. The system is built around a modular architecture that allows for different types of levels and room configurations.

## Core Architecture

### Level Hierarchy

The map generation system is organized around a hierarchy of classes:

- `com.watabou.pixeldungeon.levels.Level` - The base class for all levels, defining core functionality
- `com.watabou.pixeldungeon.levels.RegularLevel` - Implements the standard dungeon generation algorithm
- Specific level types (e.g., `SewerLevel`, `CavesLevel`, `PrisonLevel`, `BossLevel`)

### Room-Based Generation

The system uses a room-based approach where:

1. The level area is recursively subdivided into rectangular rooms
2. Rooms are connected with doors and passages
3. Each room is painted with specific content based on its type
4. Special room types are assigned based on available space and game requirements

## Generation Process

### 1. Room Layout Generation

The process begins with recursive subdivision of the level area:

```java
protected void split(Rect rect) {
    int w = rect.width();
    int h = rect.height();

    if (w > maxRoomSize && h < minRoomSize) {
        // Split vertically
    } else if (h > maxRoomSize && w < minRoomSize) {
        // Split horizontally
    } else if ((Math.random() <= (minRoomSize * minRoomSize / rect.square()) &&
               w <= maxRoomSize && h <= maxRoomSize) || w < minRoomSize || h < minRoomSize) {
        // Create a room from this rectangle
        rooms.add((Room) new Room().set(rect));
    } else {
        // Continue subdividing
    }
}
```

This method is implemented in `com.watabou.pixeldungeon.levels.RegularLevel`.

### 2. Room Connection

After rooms are created, they are connected to form a traversable graph:

- An entrance room and exit room are selected
- A path is built between them using graph algorithms
- Additional rooms are connected to ensure all areas are accessible
- Special rooms (shops, special chambers) are integrated into the network

The connection logic is handled in `com.watabou.pixeldungeon.levels.RegularLevel` and uses graph utilities from `com.watabou.utils.Graph`.

### 3. Room Painting

Each room is painted with specific content based on its type:

- `STANDARD` - Basic room with walls and floor
- `ENTRANCE` - The starting room of the level
- `EXIT` - The exit room leading to the next level
- `SHOP` - Merchant room with items for sale
- `LABORATORY` - Contains alchemy pot and special items
- `LIBRARY` - Contains books and scrolls
- `VAULT` - Contains valuable treasure
- `GARDEN` - Contains wells and plants
- `CRYPT` - Contains tombstones and special items
- And many more special room types

The painting is performed by painter classes located in `com.watabou.pixeldungeon.levels.painters`.

## Room Types and Painters

Different room types are painted using specialized painter classes:

- `com.watabou.pixeldungeon.levels.painters.StandardPainter` - Paints basic rectangular rooms
- `com.watabou.pixeldungeon.levels.painters.ShopPainter` - Creates merchant shops
- `com.watabou.pixeldungeon.levels.painters.LaboratoryPainter` - Creates alchemy laboratories
- `com.watabou.pixeldungeon.levels.painters.LibraryPainter` - Creates libraries with bookshelves
- `com.watabou.pixeldungeon.levels.painters.VaultPainter` - Creates treasure vaults
- `com.watabou.pixeldungeon.levels.painters.GardenPainter` - Creates garden rooms with wells
- `com.watabou.pixeldungeon.levels.painters.CryptPainter` - Creates crypts with tombstones
- `com.watabou.pixeldungeon.levels.painters.PoolPainter` - Creates pools of various types
- `com.watabou.pixeldungeon.levels.painters.EntrancePainter` - Creates entrance rooms
- `com.watabou.pixeldungeon.levels.painters.ExitPainter` - Creates exit rooms
- `com.watabou.pixeldungeon.levels.painters.BlacksmithPainter` - Creates blacksmith workshops
- `com.watabou.pixeldungeon.levels.painters.ArmoryPainter` - Creates armory rooms
- `com.watabou.pixeldungeon.levels.painters.TreasuryPainter` - Creates treasure rooms
- `com.watabou.pixeldungeon.levels.painters.StatuePainter` - Creates statue rooms
- `com.watabou.pixeldungeon.levels.painters.MagicWellPainter` - Creates magic well rooms
- `com.watabou.pixeldungeon.levels.painters.TrapsPainter` - Creates trap-filled rooms
- `com.watabou.pixeldungeon.levels.painters.StoragePainter` - Creates storage rooms
- `com.watabou.pixeldungeon.levels.painters.WarehousePainter` - Creates warehouse rooms
- `com.watabou.pixeldungeon.levels.painters.WeakFloorPainter` - Creates rooms with weak floors
- `com.watabou.pixeldungeon.levels.painters.PitPainter` - Creates pit rooms
- `com.watabou.pixeldungeon.levels.painters.TunnelPainter` - Creates tunnels between rooms
- `com.watabou.pixeldungeon.levels.painters.PassagePainter` - Creates passage connections
- `com.watabou.pixeldungeon.levels.painters.RatKingPainter` - Creates rat king rooms

Each painter follows a consistent pattern of filling the room with walls, then adding specific features and decorations. The painters use the `com.watabou.pixeldungeon.levels.painters.Painter` utility class which provides helper methods for:

- `fill()` - Fill rectangular areas with specific terrain
- `set()` - Set individual cells to specific terrain
- `drawInside()` - Draw features extending from room edges
- Coordinate conversion utilities between points and positions

Painters can implement different room layouts and themes, from simple rectangular rooms to complex structures with multiple features, furniture, and special items.

## SpiderLevel Generation

The `com.nyrds.pixeldungeon.spiders.levels.SpiderLevel` represents a unique approach to level generation that differs significantly from the standard room-based system. Instead of using the traditional room subdivision approach, SpiderLevel uses a chamber-based system:

### Chamber System

- Chambers are circular or shaped areas centered at specific coordinates
- Each chamber has a radius and a shape type (square, diamond, circle, etc.)
- Chambers are connected by tunnels formed by connecting their centers
- The chamber shapes are determined by mathematical formulas:
  - Square: `abs(i - j) < r`, `abs(j - i) < r`, `abs(i + j) < r`
  - Diamond: `abs(i) + abs(j) < r`
  - Circle: `i*i + j*j < r*r`
  - Hyperbolic: `abs(i * j) < r`

The chamber system is implemented in `com.nyrds.pixeldungeon.spiders.levels.Chamber`.

### Generation Process

1. **Chamber Creation**: Multiple chambers are randomly placed throughout the level
2. **Chamber Digging**: Each chamber is excavated according to its shape
3. **Connection**: Chambers are connected by tunnels that snake between them
4. **Special Features**: Spider eggs, nests, and queen are placed in chambers

### Unique Characteristics

- Unlike regular levels, SpiderLevel doesn't use the standard room system
- Chambers can have different interiors (simple caves, gardens, water areas)
- The connection algorithm creates organic-looking tunnels between chambers
- Spider-specific mobs are spawned throughout the chambers
- The level is designed to feel like a spider nest with interconnected chambers

## Terrain System

The map uses a tile-based system with various terrain types:

- `Terrain.EMPTY` - Walkable floor
- `Terrain.WALL` - Impassable wall
- `Terrain.DOOR` - Door that can be opened
- `Terrain.WATER` - Water that blocks movement but can be seen through
- `Terrain.GRASS` - Grass-covered ground
- `Terrain.HIGH_GRASS` - Tall grass that blocks line of sight
- `Terrain.CHASM` - Dangerous chasm that causes falling damage
- Various trap types (fire, poison, paralytic, etc.)

These terrain types are defined in `com.watabou.pixeldungeon.levels.Terrain` and their properties are managed in `com.watabou.pixeldungeon.levels.TerrainFlags`.

## Special Features

### Patch Generation

Some terrain types like water and grass are generated using cellular automata:

```java
public static boolean[] generate(Level level, float seed, int nGen) {
    // Initialize with random values based on seed probability
    for (int i=0; i < len; i++) {
        off[i] = Random.Float() < seed;
    }

    // Apply cellular automata rules for nGen iterations
    for (int i=0; i < nGen; i++) {
        // Count neighbors and apply rules
    }

    return off;
}
```

This implementation is found in `com.watabou.pixeldungeon.levels.Patch`.

### Feeling System

Levels can have different "feelings" that affect generation:

- `NONE` - Normal dungeon
- `CHASM` - More chasms and dangerous areas
- `WATER` - More water features
- `GRASS` - More grass and vegetation

The feeling system is implemented in `com.watabou.pixeldungeon.levels.Level.Feeling`.

### Trap and Decoration Placement

Traps and decorations are placed during the decoration phase:

- Secret doors are randomly placed on walls
- Traps are distributed throughout the level
- Decorative elements like embers and grass are added
- Special features like signs and statues are placed

## Level-Specific Variations

Different level types modify the base generation algorithm:

- `com.watabou.pixeldungeon.levels.SewerLevel` - Features water and sewer-themed decorations
- `com.watabou.pixeldungeon.levels.CavesLevel` - Has cave-like appearance with different textures
- `com.watabou.pixeldungeon.levels.PrisonLevel` - Prison-themed with bars and cells
- `com.watabou.pixeldungeon.levels.BossLevel` - Special arena-style levels for boss fights
- `com.watabou.pixeldungeon.levels.DeadEndLevel` - Small dead-end rooms with minimal content

## Dungeon Generator

The overall dungeon structure is controlled by `com.nyrds.pixeldungeon.utils.DungeonGenerator`, which:

- Reads level definitions from JSON configuration files
- Manages the connection between levels
- Handles transitions between different level types
- Controls special properties like music, tilesets, and view distances

## Room Class

The `com.watabou.pixeldungeon.levels.Room` class defines the room structure and contains:

- Information about room dimensions and position
- Connections to neighboring rooms
- Room types and special properties
- Methods for room intersection and neighbor detection

## Lua Integration and Custom Painters

The Remixed Dungeon project includes extensive Lua integration that allows for customizing many aspects of the game, including level generation. The system provides:

- A LuaEngine that enables running Lua scripts alongside Java code
- Binding of Java classes to Lua for seamless interaction
- Support for defining custom levels via Lua scripts
- Libraries that expose game functionality to Lua

### Lua Painter Implementation

The system now supports creating custom painters in Lua through the `com.nyrds.pixeldungeon.levels.painters.LuaPainter` class. This class acts as a bridge between the Java-based room painting system and Lua scripts.

#### Creating Lua Painters

To create a custom painter in Lua:

1. Create a Lua script in the `scripts/painters/` directory
2. Implement a `paint` function that accepts `level`, `room`, and `roomType` parameters
3. Use helper functions provided by the RPD library to manipulate the level
4. Configure your level to use the Lua painter in its JSON description

Example Lua painter:
```lua
local RPD = require "scripts/lib/commonClasses"

local LuaPainter = {}

function LuaPainter.paint(level, room, roomType)
    -- Fill the room with empty terrain first
    RPD.fillRoom(level, room, RPD.Terrain.EMPTY)

    -- Add walls around the perimeter
    RPD.drawWalls(level, room)

    -- Check the room type and customize accordingly
    if roomType == "SHOP" then
        -- Add shop-specific features
        -- ...
    elseif roomType == "LABORATORY" then
        -- Add lab-specific features
        -- ...
    else
        -- Add standard features
        local width = room.right - room.left
        local height = room.bottom - room.top

        if width >= 5 and height >= 5 then
            -- Add a central feature
            local centerX = room.left + math.floor(width / 2)
            local centerY = room.top + math.floor(height / 2)
            RPD.setCell(level, centerX, centerY, RPD.Terrain.PEDESTAL)
        end
    end
end

return LuaPainter
```

#### Multiple Lua Painters Support

The system supports different painters for different room types:

1. Create specific painter scripts for each room type (e.g., `LuaStandardPainter.lua`, `LuaShopPainter.lua`, `LuaLabPainter.lua`)
2. Configure your level to use specific painters for specific room types in its JSON configuration

Example configuration for specific room type painters:
```json
{
  "kind": "PredesignedLevel",
  "luaPainter.standard": "scripts/painters/LuaStandardPainter",
  "luaPainter.shop": "scripts/painters/LuaShopPainter",
  "luaPainter.laboratory": "scripts/painters/LuaLabPainter",
  "width": 32,
  "height": 32,
  "...": "..."
}
```

If a specific painter is not defined for a room type, the system will fall back to a general painter if defined, or to the default Java painters.

#### Available Helper Functions

The RPD library provides several helper functions for Lua painters:

- `RPD.setCell(level, x, y, terrain)` - Sets the terrain of a specific cell
- `RPD.getTerrain(level, x, y)` - Gets the terrain of a specific cell
- `RPD.fillRoom(level, room, terrain)` - Fills a room with a specific terrain
- `RPD.drawWalls(level, room)` - Draws walls around a room

#### Configuring Levels to Use Lua Painters

To use a Lua painter in a custom level, specify the painter in the level's JSON configuration:

```json
{
  "kind": "PredesignedLevel",
  "luaPainter": "scripts/painters/LuaCustomPainter",
  "width": 32,
  "height": 32,
  "...": "..."
}
```

## Modding Support

The system is designed to support modding:

- Level properties can be overridden via JSON configuration
- Custom painters and room types can be added
- New level types can be registered with the system
- Tilesets and other assets can be customized per level
- Lua painters allow for dynamic room generation logic

## Conclusion

The Remixed Dungeon map generation system provides a flexible and extensible framework for creating varied and interesting dungeon layouts. The combination of room-based generation, specialized painters, Lua integration, and configurable properties allows for diverse gameplay experiences while maintaining consistent underlying mechanics. The addition of Lua painters enables modders to create complex, dynamic room layouts without needing to compile Java code.