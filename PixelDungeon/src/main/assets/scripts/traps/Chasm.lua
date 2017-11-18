--
-- User: mike
-- Date: 17.11.2017
-- Time: 23:36
-- This file is part of Remixed Pixel Dungeon.
--
local RPD = require "scripts/lib/commonClasses"

local trap = require"scripts/lib/trap"

return trap.init(
    function (cell, char, data)
        RPD.Dungeon.level:set(cell, RPD.Terrain.CHASM)
        RPD.GameScene:updateMap(cell)
        RPD.Dungeon:observe()
        RPD.Dungeon.level:press(cell, char)
    end
)

