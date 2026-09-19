local RPD = require "scripts/lib/commonClasses"
local mob = require "scripts/lib/mob"

local healerAct = require "scripts/mobs/SpiderHealer"

-- java SpiderGuard (batch 10): 1/10 Stun for 3 turns
-- + sungrass medic behavior (see SpiderHealer.lua)
return mob.init{
    act = healerAct.act,
    attackProc = function(self, enemy, dmg)
        if enemy ~= nil and math.random(10) == 1 then
            RPD.Buffs.Buff:prolong(enemy, "Stun", 3)
        end
        return dmg
    end
}
