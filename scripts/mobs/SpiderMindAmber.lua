local RPD = require "scripts/lib/commonClasses"
local mob = require "scripts/lib/mob"

-- java SpiderMindAmber (batch 10): zap debuffs the enemy (one of Blindness,
-- Slow, Weakness, 3 turns) on top of the base damage, and it retreats while
-- hunting (getCloser = flee) keeping its ranged line.
return mob.init{
    zapProc = function(self, enemy, dmg)
        if enemy ~= nil then
            local debuffs = {"Blindness", "Slow", "Weakness"}
            RPD.Buffs.Buff:prolong(enemy, debuffs[math.random(#debuffs)], 3)
        end
        return dmg
    end,

    getCloser = function(self, target, ignorePets)
        -- java override: while hunting, flee instead of approaching
        if self:getState():getTag() == "HUNTING" then
            if self.enemySeen then
                return not not self:getFurther(target)
            end
            return false
        end
        return false
    end
}
