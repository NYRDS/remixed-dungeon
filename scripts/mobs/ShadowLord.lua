--[[
  ShadowLord (batch 17d-5, was mobs/common/ShadowLord.java). Depth 25 boss,
  closes the boss series. Ranged-only attacker (3 cells + LOS; the zap
  visual rides the sprite zapEffect). Any damage makes it blink away in
  Fleeing state and twist the arena into a walled maze whose pedestals
  sprout kind-2 Crystals wrapped in Darkness - it heals standing in
  Darkness, chars in Foliage, and summons wraiths (or a Shadow) while
  fleeing. Death resets the arena. Carries no SkeletonKey, by the
  long-standing ShadowLord exclusion.
]]
local RPD = require "scripts/lib/commonClasses"
local mob = require "scripts/lib/mob"
local function carveAround(level, cell)
	if not level:cellValid(cell) then
		return
	end

	local x, y = level:cellX(cell), level:cellY(cell)
	for i = x - 1, x + 1 do
		for j = y - 1, y + 1 do
			if level:cellValid(i, j) and level:get(i, j) ~= RPD.Terrain.EMPTY then
				level:set(i, j, RPD.Terrain.EMPTY)
				RPD.GameScene:updateMap(level:cell(i, j))
			end
		end
	end
end

local function twist(self)
	if not self:isAlive() then
		return
	end

	local level = RPD.Dungeon.level
	local data = mob.restoreData(self)

	if not data.levelCreated then
		RPD.LevelTools:makeEmptyLevel(level, false)
		RPD.LevelTools:buildShadowLordMaze(level, 6)
		-- caveman: maze buries whoever stands on a grid line - hero sealed
		-- in solid terrain gets no line of fire, boss stalls forever (#23)
		carveAround(level, RPD.Dungeon.hero:getPos())
		carveAround(level, self:getPos())
		data.levelCreated = true
	end

	local cell = level:getRandomLevelObjectPosition("pedestal")
	if level:cellValid(cell) then
		if RPD.Actor:findChar(cell) == nil then
			local crystal = RPD.spawnMob("Crystal", cell, { var = 2 })
			local sprite = crystal:getSprite()
			sprite:alpha(0)
			-- java built the fade-in tweener directly; no lua ctor idiom yet
			RPD.GameScene:addToMobLayer(
				luajava.newInstance("com.watabou.noosa.tweeners.AlphaTweener", sprite, 1, 0.4))
			sprite:emitter():start(RPD.Sfx.Speck:factory(RPD.Sfx.Speck.LIGHT), 0.2, 3)
			RPD.playSound("snd_teleport")
			level:fillAreaWith(RPD.Blobs.Darkness, level:cellX(cell) - 2, level:cellY(cell) - 2, 5, 5, 1)
		else
			self:damage(math.floor(self:ht() / 9), self)
		end
	end
end

return mob.init{
	spawn = function(self, level)
		local bag = self:getBelongings()
		if not bag:getItem("ScrollOfWeaponUpgrade") then
			-- raw factory: java collected it without the treasury check
			self:collect(RPD.ItemFactory:itemByName("ScrollOfWeaponUpgrade"))
		end
	end,

	damage = function(self, dmg, src)
		local data = mob.restoreData(self)
		if src ~= self and dmg > 0 and (data.cooldownUntil or 0) <= self.time and dmg < self:hp() then
			data.cooldownUntil = self.time + 10

			local jumpFrom = self
			if RPD.CharUtils:isChar(src) then
				jumpFrom = src
			end
			RPD.CharUtils:blinkAwayFrom(self, jumpFrom, 3)

			twist(self)
		end
		-- no numeric return: the regular damage flow proceeds; the revenge AI
		-- re-asserts HUNTING over any state set here, so Fleeing rides act
	end,

	act = function(self)
		local data = mob.restoreData(self)
		local fleeing = self:getState():getTag() == "Fleeing"

		if data.cooldownUntil == nil then
			-- no active flee window
		elseif self.time < data.cooldownUntil then
			if not fleeing then
				RPD.setAi(self, "Fleeing")
			end
		else
			data.cooldownUntil = nil
			RPD.setAi(self, "Wandering")
			if math.random() < 0.7 then
				for _ = 1, 4 do
					local cell = RPD.Dungeon.level:getEmptyCellNextTo(self:getPos())
					if RPD.Dungeon.level:cellValid(cell) then
						RPD.CharUtils:spawnWraithAt(RPD.Dungeon.level, cell)
					end
				end
			else
				local cell = RPD.Dungeon.level:getSolidCellNextTo(self:getPos())
				if RPD.Dungeon.level:cellValid(cell) then
					local shadow = RPD.MobFactory:mobByName("Shadow")
					RPD.setAi(shadow, "Wandering")
					RPD.WandOfBlink:appear(shadow, cell)
				end
			end
			self:yell(RPD.textById("ShadowLord_Intro"))
		end

		local level = RPD.Dungeon.level
		if level:blobAmountAt(RPD.Blobs.Darkness, self:getPos()) > 0 and self:hp() < self:ht() then
			self:heal(math.floor((self:ht() - self:hp()) / 4), level:getBlobByName("Darkness"))
		end

		if level:blobAmountAt(RPD.Blobs.Foliage, self:getPos()) > 0 then
			self:getSprite():emitter():burst(RPD.Sfx.Speck:factory(RPD.Sfx.Speck.BONE), 1)
			self:damage(1, self)
		end
	end,

	die = function(self, cause)
		self:yell(RPD.textById("ShadowLord_Death"))
		RPD.LevelTools:makeEmptyLevel(RPD.Dungeon.level, false)
		RPD.CharUtils:validateBossSlain("SHADOW_LORD_SLAIN")
	end,
}
