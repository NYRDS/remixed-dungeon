--
-- User: mike
-- Date: 13.02.2018
-- Time: 22:40
-- This file is part of Remixed Pixel Dungeon.
--

local RPD = require "scripts/lib/commonClasses"

local trap = require"scripts/lib/trap"

return trap.init(
    function (cell, char, data)
        local mobs = RPD.Dungeon.level.mobs

        local iterator = mobs:iterator()

        while iterator:hasNext() do
            local mob = iterator:next()
            if mob:getMobClassName() == data then
                mob:beckon(cell)
            end
        end
    end
)
