--
-- User: Logodum
-- Date: 21.06.2021
-- Time: 13:27
-- This file is part of Remixed Pixel Dungeon.
--

local RPD = require "scripts/lib/commonClasses"
local mob = require "scripts/lib/mob"

return mob.init{
	stats = function(me)
		-- aiState Kite + kiteMinDist 2: never lets the enemy close in
		mob.restoreData(me).kiteMinDist = 2
	end,

    zapProc = function(me, enemy, dmg)
		RPD.affectBuff(me, "ManaShield", me:skillLevel())
	end,

	zapMiss = function(me, enemy)
		if math.random() < 0.2 then
			me:yell("Shaman_ZapMiss")
		end
	end
}
