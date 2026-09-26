local RPD = require "scripts/lib/commonClasses"
local mob = require "scripts/lib/mob"

local healerAct = require "scripts/mobs/SpiderHealer"

-- java SpiderServant (batch 10): 1/4 Poison, 2 turns x duration factor
-- + sungrass medic behavior (see SpiderHealer.lua)
return mob.init{
    act = healerAct.act,
    attackProc = function(self, enemy, dmg)
        if enemy ~= nil and math.random(4) == 1 then
            RPD.affectBuff(enemy, "Poison", 2 * RPD.CharUtils:durationFactor(enemy))
        end
        return dmg
    end
}
