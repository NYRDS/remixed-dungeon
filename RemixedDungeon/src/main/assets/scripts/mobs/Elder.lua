--
-- User: Logodum
-- Date: 21.06.2021
-- Time: 13:27
-- This file is part of Remixed Pixel Dungeon.
--

local RPD = require "scripts/lib/commonClasses"
local mob = require "scripts/lib/mob"
local buffState = false

return mob.init{
    attackProc = function(self, enemy, dmg)
        return dmg
    end,
	
    zapProc = function(self, enemy, dmg)
	if buffState == false then
		buffState = true
        RPD.permanentBuff(self, "ManaShield")
		return dmg
	else 
		return dmg
    end
end,

    defenceProc = function(self, buff, enemy, damage)
	if buffState == true then
		RPD.topEffect(self:getPos(),"mana_shield_effect")
		buffState = false
	else 
		return dmg
    end
end
}
