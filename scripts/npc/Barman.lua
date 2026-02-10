--
-- User: mike
-- Date: 02.01.2018
-- Time: 00:30
-- This file is part of Remixed Pixel Dungeon.
--

local RPD = require "scripts/lib/commonClasses"

local mob = require"scripts/lib/mob"

local dialog = function(index)
    if index == 0 then
        local hero = RPD.Dungeon.hero
        local pos = RPD.getXy(hero)
        RPD.Dungeon.hero:handle(RPD.Dungeon.level:cell(pos[1],pos[2]-3))
        return
    end

    if index == 1 then
        RPD.glog("okay...")

    end
end


return mob.init({
    interact = function(self, chr)
        RPD.chooseOption( dialog,
                "Test title",
                "Go back",
                "Yes",
                "No")
    end
})
