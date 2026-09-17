--[[
  Crystal (batch 17d-5, was mobs/common/Crystal.java). Immobile zapper that
  fires its held wand, free of charge, at anything in line of sight. The
  Shadow Lord spawns the kind-2 pedestal variant (25% shadowbolt wand);
  plain pedestal rooms spawn kind 0 with a random simple wand. Kind < 2
  auto-hits (attackSkill 1000, java kept). Dying on a pedestal clears the
  Darkness around it and grows Foliage. Being stolen from marks the thief
  with chaos and kills the crystal. Stats scale with depth.
]]
local RPD = require "scripts/lib/commonClasses"
local mob = require "scripts/lib/mob"

local function ensureWand(self)
	local wand = self:getBelongings():getItemPartialMatch("WandOf")
	if wand:valid() then
		return wand
	end

	local depth = RPD.Dungeon.depth
	if self:getKind() == 2 and math.random() < 0.25 then
		wand = RPD.ItemFactory:itemByName("WandOfShadowbolt")
		wand:upgrade(math.floor(depth / 2))
	else
		wand = RPD.SimpleWand:createRandomSimpleWand()
		wand:upgrade(math.floor(depth / 3))
	end

	wand:collect(self) -- no treasury check: the wand stays in the crystal
	return wand
end

return mob.init{
	spawn = function(self, level)
		ensureWand(self)
	end,

	stats = function(self)
		local depth = RPD.Dungeon.depth
		self:ht(depth * 4 + 1)
		self:setBaseDefenseSkill(depth * 2 + 1)
		self:setExpForKill(depth + 1)
		self:setMaxLvl(depth + 2)
	end,

	dr = function(self)
		return math.floor((RPD.Dungeon.depth + 1) / 3)
	end,

	attackSkill = function(self, target)
		if self:getKind() < 2 then
			return 1000
		end
		return nil
	end,

	damageRoll = function(self)
		return RPD.Random:NormalIntRange(math.floor(self:hp() / 2), math.floor(self:ht() / 2))
	end,

	canAttack = function(self, enemy)
		return RPD.Ballistica:cast(self:getPos(), enemy:getPos(), false, true) == enemy:getPos()
	end,

	doAttack = function(self, enemy)
		-- java attack() always zapped, even point blank
		if RPD.Dungeon.level:distance(self:getPos(), enemy:getPos()) <= 1 then
			self:zap(enemy)
			self:spend(self:attackDelay())
			return true
		end
		return nil
	end,

	zapProc = function(self, enemy, damage)
		ensureWand(self):mobWandUse(self, enemy:getPos())
		return 0
	end,

	die = function(self, cause)
		local level = RPD.Dungeon.level
		local pos = self:getPos()
		local obj = level:getTopLevelObject(pos)

		if obj ~= nil and obj:getEntityKind() == "pedestal" then
			level:remove(obj)
			level:set(pos, RPD.Terrain.EMBERS)
			local x, y = level:cellX(pos), level:cellY(pos)
			level:clearAreaFrom(RPD.Blobs.Darkness, x - 2, y - 2, 5, 5)
			level:fillAreaWith(RPD.Blobs.Foliage, x - 2, y - 2, 5, 5, 1)
			RPD.GameScene:updateMap()
		end
	end,

	-- direct java dispatch (CustomMob.onActionTarget): (script, the crystal, action, actor)
	onActionTarget = function(self, me, action, actor)
		if action == RPD.Actions.ch_steal then
			RPD.ChaosCommon:doChaosMark(me:getPos(), RPD.Dungeon.depth * 3)
			me:die(actor)
		end
	end,
}
