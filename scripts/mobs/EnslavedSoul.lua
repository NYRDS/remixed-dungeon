local RPD = require "scripts/lib/commonClasses"
local mob = require "scripts/lib/mob"

return mob.init{
    attackProc = function(self, enemy, dmg)
        -- 1/5 chance to curse a hero with a random debuff for 3 turns
        if math.random(5) == 1 and enemy:getEntityKind() == "Hero" then
            local debuffs = { "Blindness", "Charm", "Roots", "Slow", "Vertigo", "Weakness" }
            RPD.Buffs.Buff:affect(enemy, debuffs[math.random(#debuffs)], 3)
        end
        return dmg
    end
}
