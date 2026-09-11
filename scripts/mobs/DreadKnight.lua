local RPD = require "scripts/lib/commonClasses"
local mob = require "scripts/lib/mob"

return mob.init{
    attackProc = function(self, enemy, dmg)
        -- 1/4 chance: death stroke + double damage
        if enemy ~= nil and math.random(4) == 1 then
            RPD.Sfx.DeathStroke.hit(enemy)
            return dmg * 2
        end
        -- 1/10 chance to stun
        if enemy ~= nil and math.random(10) == 1 then
            RPD.Buffs.Buff:affect(enemy, "Stun")
        end
        return dmg
    end
}
