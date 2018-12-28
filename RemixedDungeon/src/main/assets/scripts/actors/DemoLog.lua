--
-- User: mike
-- Date: 22.01.2018
-- Time: 23:40
-- This file is part of Remixed Pixel Dungeon.
--

local RPD = require "scripts/lib/commonClasses"
local actor = require "scripts/lib/actor"

return actor.init({
    activate = function()
        RPD.glog("Welcome to DemoLevel")
        RPD.glog("++ Welcome to DemoLevel")
        RPD.glog("@@ Welcome to %s", "DemoLevel")
        RPD.glog("-- Welcome to DemoLevel")
        RPD.glog("** Welcome to %s", "DemoLevel")
    end
})
