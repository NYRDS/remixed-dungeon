local RPD = require "scripts/lib/commonClasses"
local mob = require "scripts/lib/mob"

-- java Skeleton (batch 8): carries the worst of 3 random weapons (20%),
-- detonates on death - bone-blast damage to all 4 neighbours.
return mob.init{
    stats = function(self)
        -- ctor-only roll, persisted (stats re-runs on every restore)
        local data = mob.restoreData(self)
        if data.rolled then
            return
        end
        if not self:getBelongings():isBackpackEmpty() then
            -- pre-migration saves already carry the weapon their java rolled
            data.rolled = true
            return
        end
        data.rolled = true

        -- java getLoot(): no treasury loot on the Lich's arena
        -- (ctor runs during level creation, Dungeon.level may be nil -
        -- java's `level() instanceof NecroBossLevel` was null-safe too)
        local lvl = RPD.Dungeon.level
        if lvl ~= nil and lvl:levelKind() == "NecroBossLevel" then
            return
        end

        -- java loot() hero-level gate
        local hero = RPD.Dungeon.hero
        if not self:isBoss() and hero:lvl() > self:getMaxLvl() + 2 + self:lvl() then
            return
        end

        if math.random(5) == 1 then
            self:collect(RPD.Treasury:getLevelTreasury():worstOf("WEAPON", 3))
        end
    end,

    die = function(self, cause)
        -- bone-blast: damage all 4 neighbours
        local level = RPD.Dungeon.level
        local pos = self:getPos()
        local w = level:getWidth()
        local heroKilled = false
        for _, off in ipairs({-w, 1, w, -1}) do
            local ch = RPD.Actor:findChar(pos + off)
            if ch ~= nil and ch:isAlive() then
                local dmg = math.max(0, self:damageRoll() - ch:defenceRoll(self) / 2)
                ch:damage(dmg, self)
                if ch:getEntityKind() == "Hero" and not ch:isAlive() then
                    heroKilled = true
                end
            end
        end

        if RPD.CharUtils:isVisible(self) then
            RPD.playSound("snd_bones")
        end

        if heroKilled then
            -- java Skeleton.die set the death report explicitly (on top of the
            -- generic Doom path, which composes the same MOB wording)
            RPD.Dungeon:fail(RPD.JavaUtils:format(
                    RPD.ResultDescriptions:getDescription(RPD.ResultReason.MOB),
                    { RPD.JavaUtils:indefinite(self:getName()), RPD.Dungeon.depth }))
            RPD.glogn(RPD.textById("Skeleton_Killed"))
        end
    end
}
