local RPD = require "scripts/lib/commonClasses"
local mob = require "scripts/lib/mob"

-- java SpiderGuard (batch 10): 1/10 Stun for 3 turns
return mob.init{
    attackProc = function(self, enemy, dmg)
        if enemy ~= nil and math.random(10) == 1 then
            RPD.Buffs.Buff:prolong(enemy, "Stun", 3)
        end
        return dmg
    end
}
