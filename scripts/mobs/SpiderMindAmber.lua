local RPD = require "scripts/lib/commonClasses"
local mob = require "scripts/lib/mob"

-- java SpiderMindAmber (batch 10): zap debuffs the enemy (one of Blindness,
-- Slow, Weakness, 3 turns) on top of the base damage, and it retreats while
-- hunting (aiState Kite + kiteNeverApproach) keeping its ranged line.
return mob.init{
    stats = function(self)
        mob.restoreData(self).kiteNeverApproach = true
    end,

    zapProc = function(self, enemy, dmg)
        if enemy ~= nil then
            local debuffs = {"Blindness", "Slow", "Weakness"}
            RPD.Buffs.Buff:prolong(enemy, debuffs[math.random(#debuffs)], 3)
        end
        return dmg
    end,

}
