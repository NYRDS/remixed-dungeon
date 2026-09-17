--[[
  Sheep (batch 17d-5, was WandOfFlock.Sheep). Immortal flock block: stands
  where the wand put it until its lifespan runs out, then fades. The
  lifespan rides the script (setLifespan, called by java WandOfFlock and
  the chaos shield) and deliberately does NOT persist - java never saved
  it, so a reloaded sheep fades almost at once. Buff-immune like the old
  NPC base class.
]]
local RPD = require "scripts/lib/commonClasses"
local mob = require "scripts/lib/mob"

-- per-instance state, intentionally not persisted (java lifespan quirk)
local state = setmetatable({}, { __mode = "k" })

local sheepMob = mob.init{
	addBuff = function(self, buff)
		return true
	end,

	act = function(self)
		local s = state[self]
		if not s then
			s = { start = self.time, limit = math.random() * 2 } -- no lifespan set: fades fast
			state[self] = s
		end

		-- the actor's own accumulated time is the turn clock (the act hook
		-- itself can run several times per turn, a raw tick counter can't)
		if self.time - s.start >= s.limit then
			self:destroy()
			self:getSprite():die()
			return true -- skip base act: nothing left to spend on
		end
	end,

	interact = function(self, hero)
		local variants = RPD.StringsManager:getVars("WandOfFlock_SheepBaa")
		self:say(RPD.StringsManager:maybeId("WandOfFlock_SheepBaa", RPD.Random:Int(0, variants.length)))
		return false
	end,
}

-- java dispatch passes (script, the sheep, lifespan); lua call sites use
-- setLifespan(sheep, lifespan) - accept both shapes
sheepMob.setLifespan = function(self, a, b)
	local mob, lifespan
	if b == nil then
		mob, lifespan = self, a
	else
		mob, lifespan = a, b
	end
	state[mob] = { start = mob.time, limit = lifespan + math.random() * 2 }
end

return sheepMob
