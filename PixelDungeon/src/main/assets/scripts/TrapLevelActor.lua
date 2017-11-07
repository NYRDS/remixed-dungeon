--
-- User: mike
-- Date: 05.11.2017
-- Time: 14:46
-- This file is part of Remixed Pixel Dungeon.
--
require "scripts/commonClasses"

local trap = require"scripts/TrapCommon"

return trap.init(
    function (cell, char, data)
        RPD.Dungeon.level:addScriptedActor(luajava.newInstance("com.nyrds.pixeldungeon.mechanics.actors.ScriptedActor","scripts/LevelActorSandbox"))
    end
)
