local RPD = require "scripts/lib/commonClasses"
local mob = require "scripts/lib/mob"

-- java Bandit (batch 7): a thief that also blinds the victim it robbed
-- from (its defenceProc gold drop is inherited from Thief).
return mob.init{
    attackProc = function(self, enemy, dmg)
        if enemy ~= nil and RPD.CharUtils:steal(self, enemy) then
            RPD.setAi(self, "ThiefFleeing")
            -- Random.Int(5,12) is [5,12)
            RPD.Buffs.Buff:prolong(enemy, "Blindness", math.random(5, 11))
            enemy:observe()
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
