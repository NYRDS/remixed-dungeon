--
-- Shared sungrass-medic behavior for the nest spiders (beta.10 feedback
-- round): while the spider stands in Sungrass foliage it heals allies
-- instead of hunting the hero. Loaded by scripts/mobs/SpiderServant.lua,
-- SpiderExploding.lua and SpiderGuard.lua.
--

local RPD = require "scripts/lib/commonClasses"

local mob = require "scripts/lib/mob"

local healer = {}

-- dispatch is dot-style: mob.lua calls self.act(mob) - single arg, no self
function healer.act(me)
	local level = me:level()
	if level:blobAmountAt(RPD.Blobs.Foliage, me:getPos()) > 0 then
		if me:getState():getTag() ~= "Healer" then
			RPD.setAi(me, "Healer")
		end
	elseif me:getState():getTag() == "Healer" then
		-- left the grass: back to regular hunting
		RPD.setAi(me, "HUNTING")
	end
	return false
end

return healer
