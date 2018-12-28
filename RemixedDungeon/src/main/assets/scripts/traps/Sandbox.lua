--
-- User: mike
-- Date: 04.11.2017
-- Time: 22:26
-- This file is part of Remixed Pixel Dungeon.
--
local RPD = require "scripts/lib/commonClasses"

local trap = require"scripts/lib/trap"

return trap.init(
    function (cell, char, data)
        local x = RPD.Dungeon.level:cellX(cell)
        local y = RPD.Dungeon.level:cellY(cell)
        for i = x - 1, x + 1 do
            for j = y - 1, y + 1 do
                if i~=x or j~=y then
                    RPD.GameScene:add( RPD.Blobs.Blob:seed( RPD.Dungeon.level:cell(i,j), 10 , RPD.Blobs.Fire ) );
                end
            end
        end
    end
)
