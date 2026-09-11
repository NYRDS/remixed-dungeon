local RPD = require "scripts/lib/commonClasses"
local mob = require "scripts/lib/mob"

return mob.init{
    attackProc = function(self, enemy, dmg)
        -- 1/2 chance to inflict bleeding as strong as the hit
        if math.random(2) == 1 then
            local bleeding = RPD.Buffs.Buff:affect(enemy, "Bleeding")
            bleeding:level(dmg)
        end
        return dmg
    end
}
