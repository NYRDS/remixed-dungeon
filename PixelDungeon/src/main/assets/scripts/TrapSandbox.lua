--
-- User: mike
-- Date: 04.11.2017
-- Time: 22:26
-- This file is part of Remixed Pixel Dungeon.
--
require "scripts/commonClasses"

local data
function setData(_data)
    data = _data
end

function trap(cell, char)
    local x = Dungeon.level:cellX(cell)
    local y = Dungeon.level:cellY(cell)
    for i = x - 1, x + 1 do
        for j = y - 1, y + 1 do
            if i~=x or j~=y then
                GameScene:add( Blob:seed( Dungeon.level:cell(i,j),cell, Fire ) );
            end
        end
    end
end

