local RPD = require "scripts/lib/commonClasses"
local mob = require "scripts/lib/mob"

return mob.init{
    attackProc = function(self, enemy, dmg)
        -- 1/3 chance to poison; java rolled Random.Int(2,3) which is [2,3)
        -- i.e. always 2 turns
        if enemy ~= nil and math.random(3) == 1 then
            local factor = RPD.Buffs.Poison:durationFactor(enemy)
            RPD.Buffs.Buff:affect(enemy, "Poison", 2 * factor)
        end
        return dmg
    end
}
