--
-- User: Logodum
-- Date: 21.06.2021
-- Time: 13:27
-- This file is part of Remixed Pixel Dungeon.
--

local RPD = require "scripts/lib/commonClasses"
local mob = require "scripts/lib/mob"

return mob.init{
    zapProc = function(self, enemy, dmg)
		RPD.affectBuff(self, "ManaShield", self:skillLevel())
	end
}
