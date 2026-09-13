local RPD = require "scripts/lib/commonClasses"
local mob = require "scripts/lib/mob"

-- java Thief (batch 7): steals a random backpack item on hit and runs
-- (ThiefFleeing), dropping gold when struck mid-flight.
return mob.init{
    attackProc = function(self, enemy, dmg)
        if enemy ~= nil and RPD.CharUtils:steal(self, enemy) then
            RPD.setAi(self, "ThiefFleeing")
        end
        return dmg
    end,

    defenceProc = function(self, enemy, dmg)
        if self:getState():getTag() == "THIEFFLEEING" then
            RPD.item("Gold", 1):doDrop(self)
        end
        return dmg
    end,
}
