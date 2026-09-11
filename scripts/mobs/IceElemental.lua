local RPD = require "scripts/lib/commonClasses"
local mob = require "scripts/lib/mob"

return mob.init{
    attackProc = function(self, enemy, dmg)
        -- 1/3 chance to chill a hero
        if math.random(3) == 1 and enemy:getEntityKind() == "Hero" then
            RPD.affectBuff(enemy, "Slow", 3)
        end
        return dmg
    end
}
