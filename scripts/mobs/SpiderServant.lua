local RPD = require "scripts/lib/commonClasses"
local mob = require "scripts/lib/mob"

-- java SpiderServant (batch 10): 1/4 Poison, 2 turns x duration factor
return mob.init{
    attackProc = function(self, enemy, dmg)
        if enemy ~= nil and math.random(4) == 1 then
            RPD.affectBuff(enemy, "Poison", 2 * RPD.Buffs.Poison:durationFactor(enemy))
        end
        return dmg
    end
}
