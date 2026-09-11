local RPD = require "scripts/lib/commonClasses"
local mob = require "scripts/lib/mob"

return mob.init{
    attackProc = function(self, enemy, dmg)
        -- 1/4 chance to chill the enemy (java did Freezing.affect: Frost
        -- buff plus cell-side fire out / heap freeze; Frost is the
        -- Char-facing part)
        if math.random(4) == 1 then
            RPD.Buffs.Buff:affect(enemy, "Frost")
        end
        return dmg
    end
}
