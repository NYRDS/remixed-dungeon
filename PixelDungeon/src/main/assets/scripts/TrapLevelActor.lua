--
-- User: mike
-- Date: 05.11.2017
-- Time: 14:46
-- This file is part of Remixed Pixel Dungeon.
--
require "scripts/commonClasses"

local data
function setData(_data)
    data = _data
end

function trap(cell, char)
    Dungeon.level:addScriptedActor(luajava.newInstance("com.nyrds.pixeldungeon.mechanics.actors.ScriptedActor","scripts/LevelActorSandbox"))
end
