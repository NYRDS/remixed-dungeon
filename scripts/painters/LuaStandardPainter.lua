--
-- Lua painter for standard rooms
-- This script handles STANDARD room type
--

local RPD = require "scripts/lib/commonClasses"

local StandardRoomPainter = {}

function StandardRoomPainter.paint(level, room, roomType)
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

return StandardRoomPainter