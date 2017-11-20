--
-- User: mike
-- Date: 21.11.2017
-- Time: 0:21
-- This file is part of Remixed Pixel Dungeon.
--

local RPD = require "scripts/lib/commonClasses"

local trap = require"scripts/lib/trap"

return trap.init(
    function (cell, char, data)
        local hero = RPD.Dungeon.hero
        hero:STR(hero:STR()-1)
    end
)

