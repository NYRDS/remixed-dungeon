--
-- Lua painter for laboratory rooms
-- This script handles LABORATORY room type
--

local RPD = require "scripts/lib/commonClasses"

local LabRoomPainter = {}

function LabRoomPainter.paint(level, room, roomType)
    -- Fill the room with empty terrain first
    RPD.fillRoom(level, room, RPD.Terrain.EMPTY)
    
    -- Add walls around the perimeter
    RPD.drawWalls(level, room)
    
    -- Add lab-specific features
    local width = room.right - room.left
    local height = room.bottom - room.top
    
    -- Add alchemy pot in the center
    local centerX = room.left + math.floor(width / 2)
    local centerY = room.top + math.floor(height / 2)
    RPD.setCell(level, centerX, centerY, RPD.Terrain.ALCHEMY)
    
    -- Add some experimental tables around
    local positions = {
        {centerX - 1, centerY - 1},
        {centerX + 1, centerY - 1},
        {centerX - 1, centerY + 1},
        {centerX + 1, centerY + 1}
    }
    
    for _, pos in ipairs(positions) do
        local x, y = pos[1], pos[2]
        if x > room.left and x < room.right and y > room.top and y < room.bottom then
            RPD.setCell(level, x, y, RPD.Terrain.EMPTY_SP)
        end
    end
    
    -- Add some water features
    if width >= 6 and height >= 6 then
        -- Add water in corners
        RPD.setCell(level, room.left + 1, room.top + 1, RPD.Terrain.WATER)
        RPD.setCell(level, room.right - 1, room.top + 1, RPD.Terrain.WATER)
        RPD.setCell(level, room.left + 1, room.bottom - 1, RPD.Terrain.WATER)
        RPD.setCell(level, room.right - 1, room.bottom - 1, RPD.Terrain.WATER)
    end
    
    -- Add doors to connect to adjacent rooms
    for _, door in ipairs(room.doors or {}) do
        RPD.setCell(level, door.x, door.y, RPD.Terrain.DOOR)
    end
end

return LabRoomPainter