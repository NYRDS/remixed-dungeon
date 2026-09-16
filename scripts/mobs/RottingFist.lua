--[[
  RottingFist - Yog's rotting fist (batch 17d-2, was mobs/guts/RottingFist.java).
  Melee bruiser: 1-in-3 hits coat the victim in Ooze; while standing in
  water and wounded it regenerates 10 hp per turn.
]]
local RPD = require "scripts/lib/commonClasses"
local mob = require "scripts/lib/mob"

return mob.init{
    attackProc = function(self, enemy, damage)
        if enemy ~= nil and math.random(3) == 1 then
            RPD.Buffs.Buff:affect(enemy, "Ooze")
            enemy:getSprite():burst(0x000000, 5)
        end
        return damage
    end,

    act = function(self)
        local level = RPD.Dungeon.level
        if level.water[self:getPos() + 1] and self:hp() < self:ht() then
            self:getSprite():emitter():burst(RPD.Sfx.ShadowParticle.UP, 2)
            self:heal(10, self, true)
        end
    end,

    damage = function(self, dmg, src)
        -- being hurt draws every mob in the halls to the fist
        local mobs = RPD.Dungeon.level:getMobs()
        for i = 1, #mobs do
            mobs[i]:beckon(self:getPos())
        end
    end,
}
