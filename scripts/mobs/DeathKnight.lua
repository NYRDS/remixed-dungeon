local RPD = require "scripts/lib/commonClasses"
local mob = require "scripts/lib/mob"

return mob.init{
    attackProc = function(self, enemy, dmg)
        -- 1/7 chance: death stroke + double damage
        if enemy ~= nil and math.random(7) == 1 then
            RPD.Sfx.DeathStroke.hit(enemy)
            return dmg * 2
        end
        return dmg
    end
}
