--
-- User: mike
-- Date: 06.11.2017
-- Time: 23:57
-- This file is part of Remixed Pixel Dungeon.
--

local RPD = require "scripts/lib/commonClasses"
local actor = require "scripts/lib/actor"

return actor.init({
    act = function()

        local objects = RPD.Dungeon.level:getLevelObjects()

        for k,v in pairs(objects) do

        end
        return true
    end,
    actionTime = function()
        return 1
    end,
    activate = function()
    end
})