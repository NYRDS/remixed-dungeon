--
-- User: Logodum
-- Date: 21.06.2021
-- Time: 13:27
-- This file is part of Remixed Pixel Dungeon.
--

local RPD = require "scripts/lib/commonClasses"
local mob = require "scripts/lib/mob"

return mob.init{
	act = function(me)
		local d = me:distance(me:getEnemy())

		if d < 2 then
			RPD.setAi(me, "Fleeing")
			return
		end

		if d > 4 then
			RPD.setAi(me, "Hunting")
			return
		end
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
