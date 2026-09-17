--[[
  MirrorImage (batch 17d-5, was actors/mobs/npcs/MirrorImage.java). A
  fragile copy of the hero: ScrollOfMirrorImage and the Multiplicity glyph
  clone into this kind, the script snapshots the hero's combat stats and
  pet ownership at clone time (java ctor parity), and the first attack
  destroys the image. The hero look/death effect ride the java
  CustomMob.heroLook fields, set by Hero.makeClone.
]]
local RPD = require "scripts/lib/commonClasses"
local mob = require "scripts/lib/mob"

return mob.init{
	-- direct java dispatch (Hero.makeClone): args are (script, the clone, the hero)
	onClone = function(self, img, hero)
		img:setBaseAttackSkill(hero:attackSkill(hero))
		img:setBaseDefenseSkill(hero:defenseSkill(hero))
		local roll = hero:damageRoll()
		img:setDmgMin(roll)
		img:setDmgMax(roll * 2)
		img:makePet(hero)
	end,

	attackProc = function(self, enemy, dmg)
		self:destroy()
		self:getSprite():die()
		return dmg
	end,
}
