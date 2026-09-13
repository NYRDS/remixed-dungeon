local RPD = require "scripts/lib/commonClasses"
local mob = require "scripts/lib/mob"

-- java EarthElemental: half speed in liquid; half of its hits seed a
-- Regrowth blob on open ground. Stats scale with the spawn depth (json
-- holds the depth-1 defaults).
return mob.init{
    stats = function(self)
        local d = RPD.Dungeon.depth
        local ht = d * 10 + 1
        self:ht(ht)
        self:hp(ht)
        self:STR(15)
        self:setBaseDefenseSkill(math.floor(d / 2) + 1)
        self:setBaseAttackSkill(math.floor((math.floor(d / 2) + 1) / 2) + 1)
        self:setExpForKill(d + 1)
        self:setMaxLvl(d + 2)
        self:setDr(d + 1)
        self:setDmgMin(math.floor(ht / 5))
        self:setDmgMax(math.floor(ht / 5))
    end,

    speed = function(self, base)
        if RPD.TerrainFlags:is(RPD.Dungeon.level.map[self:getPos() + 1], RPD.TerrainFlags.LIQUID) then
            return base * 0.5
        end
        return base
    end,

    attackProc = function(self, enemy, dmg)
        if enemy ~= nil and math.random(2) == 1 then
            local cell = enemy:getPos()
            local c = RPD.Dungeon.level.map[cell + 1]
            if c == RPD.Terrain.EMPTY or c == RPD.Terrain.EMBERS
                    or c == RPD.Terrain.EMPTY_DECO or c == RPD.Terrain.GRASS
                    or c == RPD.Terrain.HIGH_GRASS then
                RPD.placeBlob(RPD.Blobs.Regrowth, cell, math.max(self:getExpForKill(), 10) * 15)
            end
        end
        return dmg
    end,
}
