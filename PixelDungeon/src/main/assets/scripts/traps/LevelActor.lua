--
-- User: mike
-- Date: 05.11.2017
-- Time: 14:46
-- This file is part of Remixed Pixel Dungeon.
--
local RPD = require "scripts/lib/commonClasses"

local trap = require"scripts/lib/trap"

return trap.init(
    function (cell, char, data)
        RPD.Dungeon.level:addScriptedActor(RPD.new(RPD.Objects.Actors.ScriptedActor,"scripts/Burn"))
    end
)
