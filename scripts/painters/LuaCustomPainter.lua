--
-- Example Lua painter script
-- This script demonstrates how to create a custom room painter in Lua
--

local RPD = require "scripts/lib/commonClasses"

local LuaPainter = {}

-- Main paint function - this is called by the Java side
function LuaPainter.paint(level, room)
    -- Fill the room with empty terrain first
    RPD.fillRoom(level, room, RPD.Terrain.EMPTY)
    
    -- Add walls around the perimeter
    RPD.drawWalls(level, room)
    
    -- Add some custom features based on room size
    local width = room.right - room.left
    local height = room.bottom - room.top
    
    -- If the room is large enough, add some features
    if width >= 5 and height >= 5 then
        -- Add a central feature
        local centerX = room.left + math.floor(width / 2)
        local centerY = room.top + math.floor(height / 2)
        
        -- Place a special terrain in the center
        RPD.setCell(level, centerX, centerY, RPD.Terrain.PEDESTAL)
        
        -- Add some grass around the center
        for dx = -1, 1 do
            for dy = -1, 1 do
                if not (dx == 0 and dy == 0) then
                    local x, y = centerX + dx, centerY + dy
                    if x > room.left and x < room.right and y > room.top and y < room.bottom then
                        -- Only place grass if the cell is currently empty
                        local currentTerrain = RPD.getTerrain(level, x, y)
                        if currentTerrain == RPD.Terrain.EMPTY then
                            RPD.setCell(level, x, y, RPD.Terrain.GRASS)
                        end
                    end
                end
            end
        end
    end
    
    -- Add doors to connect to adjacent rooms
    for _, door in ipairs(room.doors or {}) do
        RPD.setCell(level, door.x, door.y, RPD.Terrain.DOOR)
    end
end

-- Helper function to fill a room with a specific terrain
function RPD.fillRoom(level, room, terrain)
    for x = room.left + 1, room.right - 1 do
        for y = room.top + 1, room.bottom - 1 do
            RPD.setCell(level, x, y, terrain)
        end
    end
end

-- Helper function to draw walls around a room
function RPD.drawWalls(level, room)
    -- Top and bottom walls
    for x = room.left, room.right do
        RPD.setCell(level, x, room.top, RPD.Terrain.WALL)
        RPD.setCell(level, x, room.bottom, RPD.Terrain.WALL)
    end
    
    -- Left and right walls
    for y = room.top, room.bottom do
        RPD.setCell(level, room.left, y, RPD.Terrain.WALL)
        RPD.setCell(level, room.right, y, RPD.Terrain.WALL)
    end
end

-- Helper function to set a cell's terrain
function RPD.setCell(level, x, y, terrain)
    local cell = y * level.height + x
    level.map[cell + 1] = terrain  -- Lua arrays are 1-indexed
end

-- Helper function to get a cell's terrain
function RPD.getTerrain(level, x, y)
    local cell = y * level.height + x
    return level.map[cell + 1]  -- Lua arrays are 1-indexed
end

return LuaPainter