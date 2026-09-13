local RPD = require "scripts/lib/commonClasses"
local mob = require "scripts/lib/mob"

return mob.init{
    attackProc = function(self, enemy, dmg)
        -- 1/4 chance: death stroke + double damage
        if enemy ~= nil and math.random(4) == 1 then
            RPD.Sfx.DeathStroke:hit(enemy)
            return dmg * 2
        end
        -- 1/10 chance to stun (java's 2-arg affect carried no duration and
        -- never stunned anyone; Stun.duration = the standard convention)
        if enemy ~= nil and math.random(10) == 1 then
            RPD.affectBuff(enemy, "Stun", RPD.Buffs.Stun:duration(enemy))
        end
        return dmg
    end
}
