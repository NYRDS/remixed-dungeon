local RPD = require "scripts/lib/commonClasses"
local mob = require "scripts/lib/mob"

-- java Nightmare (batch 12): 1/10 Roots + 1/10 Stun on hit, forces Hunting
-- every tick. Durations real per the as-intended pass (Worm Roots / Stun:duration).
return mob.init{
    attackProc = function(self, enemy, dmg)
        if enemy ~= nil then
            if math.random(10) == 1 then
                RPD.affectBuff(enemy, "Roots", 3)
            end
            if math.random(10) == 1 then
                RPD.affectBuff(enemy, "Stun", RPD.Buffs.Stun:duration(enemy))
            end
        end
        return dmg
    end,
    act = function(self)
        RPD.setAi(self, "Hunting")
    end
}
