--
-- User: mike
-- Date: 17.11.2017
-- Time: 21:51
-- This file is part of Remixed Pixel Dungeon.
--

local RPD = require "scripts/lib/commonClasses"

local trap = require"scripts/lib/trap"

return trap.init(
    function (cell, char, data)
        RPD.Chasm:charFall(cell,char)
    end
)
