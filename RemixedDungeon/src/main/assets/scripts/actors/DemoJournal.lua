--
-- User: mike
-- Date: 14.11.2017
-- Time: 21:58
-- This file is part of Remixed Pixel Dungeon.
--

local RPD = require "scripts/lib/commonClasses"
local actor = require "scripts/lib/actor"

return actor.init({
    activate = function()
        RPD.Journal:add("Wow! Custom Entry!")
    end
})
