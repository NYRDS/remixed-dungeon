--
-- Lua painter for shop rooms
-- This script handles SHOP room type
--

local RPD = require "scripts/lib/commonClasses"

local ShopRoomPainter = {}

function ShopRoomPainter.paint(level, room, roomType)
    -- Fill the room with empty terrain first
    RPD.fillRoom(level, room, RPD.Terrain.EMPTY)
    
    -- Add walls around the perimeter
    RPD.drawWalls(level, room)
    
    -- Add shop-specific features
    local width = room.right - room.left
    local height = room.bottom - room.top
    
    -- Add counters along the walls
    -- Top counter
    for x = room.left + 1, room.right - 1 do
        RPD.setCell(level, x, room.top + 1, RPD.Terrain.BOOKSHELF)
    end
    
    -- Bottom counter
    for x = room.left + 1, room.right - 1 do
        RPD.setCell(level, x, room.bottom - 1, RPD.Terrain.BOOKSHELF)
    end
    
    -- Side counters
    for y = room.top + 2, room.bottom - 2 do
        RPD.setCell(level, room.left + 1, y, RPD.Terrain.BOOKSHELF)
        RPD.setCell(level, room.right - 1, y, RPD.Terrain.BOOKSHELF)
    end
    
    -- Add a few empty spaces in the middle for customers
    local centerX = room.left + math.floor(width / 2)
    local centerY = room.top + math.floor(height / 2)
    RPD.setCell(level, centerX, centerY, RPD.Terrain.EMPTY)
    
    -- Add doors to connect to adjacent rooms
    for _, door in ipairs(room.doors or {}) do
        RPD.setCell(level, door.x, door.y, RPD.Terrain.DOOR)
    end
end

return ShopRoomPainter