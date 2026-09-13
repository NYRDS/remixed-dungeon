local RPD = require "scripts/lib/commonClasses"
local mob = require "scripts/lib/mob"

-- java WaterElemental: fast in water, sluggish on land; heals while standing
-- in water; hits chill; Frost is absorbed (heal + no attach), Burning hurts
-- it yet still attaches. Stats scale with the spawn depth (json holds the
-- depth-1 defaults), the depth it lives on is the only depth it ever sees.
return mob.init{
    stats = function(self)
        local d = RPD.Dungeon.depth
        local ht = d * 5 + 1
        self:ht(ht)
        self:hp(ht)
        self:STR(14)
        self:setBaseDefenseSkill(d * 2 + 1)
        self:setBaseAttackSkill(math.floor((d * 2 + 1) / 2) + 1)
        self:setExpForKill(d + 1)
        self:setMaxLvl(d + 2)
        self:setDr(math.floor((d + 1) / 3))
        self:setDmgMin(math.floor(ht / 2))
        self:setDmgMax(math.floor(ht / 2))
    end,

    speed = function(self, base)
        if RPD.TerrainFlags:is(RPD.Dungeon.level.map[self:getPos() + 1], RPD.TerrainFlags.LIQUID) then
            return base * 2
        end
        return base * 0.5
    end,

    act = function(self)
        if RPD.Dungeon.level.water[self:getPos() + 1] then
            self:heal(self:getExpForKill(), self)
        end
    end,

    attackProc = function(self, enemy, dmg)
        -- java hit proc: Freezing.affect on the victim cell - Frost (with
        -- duration), fire put out, and the heap under the victim freezes
        -- (meat -> FrozenCarpaccio, potions shatter, mimics wake chilled).
        -- The Frost itself still shatters to the same hit's damage.
        if enemy ~= nil and math.random(2) == 1 then
            RPD.PseudoBlobs.Freezing:affect(enemy:getPos())
        end
        return dmg
    end,

    addBuff = function(self, buff)
        local kind = buff:getEntityKind()
        if kind == "Frost" then
            -- absorb: heal (src = self, else resist() zeroes a heal sourced
            -- by the Frost itself) and don't attach. The json no longer
            -- lists Frost as immunity so the buff actually reaches here.
            if self:hp() < self:ht() then
                self:heal(self:getExpForKill(), self)
            end
            return true
        end
        if kind == "Burning" then
            if not RPD.Dungeon:isLoading() then
                self:damage(RPD.Random:NormalIntRange(1, math.floor(self:ht() / 3)), buff)
            end
            return false
        end
        return false
    end,
}
