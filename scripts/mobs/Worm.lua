local RPD = require "scripts/lib/commonClasses"
local mob = require "scripts/lib/mob"

return mob.init{
    attackProc = function(self, enemy, dmg)
        -- 1/7 chance to root, 1/5 chance to poison for 7-8 turns
        if math.random(7) == 1 then
            -- java 2-arg affect had no duration: roots that never rooted
            RPD.affectBuff(enemy, "Roots", 3)
        end
        if math.random(5) == 1 then
            local factor = RPD.CharUtils:durationFactor(enemy)
            RPD.Buffs.Buff:affect(enemy, "Poison", math.random(7, 8) * factor)
        end
        return dmg
    end
}
