--
-- Sungrass medic AI: stand in the foliage and heal wounded allies instead of
-- hunting (beta.10 feedback round). Any damage switches the mob back to
-- HUNTING; the mob script puts it back on this state while the grass lasts.
--

local RPD = require "scripts/lib/commonClasses"

local ai = require "scripts/lib/ai"

local healPerTurn = 2

local function healWoundedAlly(me)
	local level = RPD.Dungeon.level
	local x = level:cellX(me:getPos())
	local y = level:cellY(me:getPos())

	for dx = -1, 1 do
		for dy = -1, 1 do
			if dx ~= 0 or dy ~= 0 then
				local cell = level:cell(x + dx, y + dy)
				if level:cellValid(cell) then
					local chr = RPD.Actor:findChar(cell)
					if chr and chr:valid() and me:friendly(chr) and chr:hp() < chr:ht() then
						chr:heal(healPerTurn, me)
						return true
					end
				end
			end
		end
	end
	return false
end

return ai.init{
	-- dispatch prepends the module table AND the CustomMobAi state object:
	-- (self, ai, me) like BlackCat
	act = function(self, ai, me)
		-- one healed ally per turn; the engine spends the tick itself
		healWoundedAlly(me)
		return true
	end,

	gotDamage = function(self, ai, me, src, dmg)
		-- attacked while medicing: fight back, the mob script returns us
		-- to healing while the foliage lasts
		RPD.setAi(me, "HUNTING")
	end,

	status = function(self, ai, me)
		return "healing the nest"
	end
}
