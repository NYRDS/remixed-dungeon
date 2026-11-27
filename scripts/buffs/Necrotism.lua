---
--- Necrotism: spreading necrosis DoT (was mechanics/buffs/Necrotism.java).
--- Iteration rides the buff level (packed); the remaining acts ride the
--- script data (serpent), initialized by the spreaDer/caller through
--- loadData - exactly what the java set(duration, i) field was.
---
local RPD     = require "scripts/lib/commonClasses"
local buff    = require "scripts/lib/buff"
local serpent = require "scripts/lib/serpent"

local DURATION = 3

return buff.init{
    desc  = function ()
        return {
            icon          = 39, -- BuffIndicator.NECROTISM
            name          = "NecrotismBuff_Name",
            info          = "NecrotismBuff_Info",
        }
    end,

    act = function(self, buff)
        local target = buff.target
        if not target:isAlive() then
            buff:detach()
            return
        end

        target:getSprite():burst(0x6935a5, 3)

        local iteration = buff:level()
        local damage
        if target:isBoss() then
            damage = math.floor(target:hp() / 200)
        else
            damage = math.floor(target:hp() / math.max(3, 21 - iteration))
        end

        target:damage(math.max(1, damage * iteration), buff)
        buff:spend(1)

        -- spread: Level.NEIGHBOURS16 order kept verbatim
        local cell = target:getPos()
        local lvl = RPD.Dungeon.level
        local W = lvl:getWidth()
        local dirs = { 1, 2, -1, -2,
            W, -W, -2 * W, 2 * W,
            1 + W, 1 - W, 2 + W, 2 - W,
            1 + 2 * W, 1 - 2 * W, 2 + 2 * W, 2 - 2 * W,
            -1 + W, -1 - W, -2 + W, -2 - W,
            -1 + 2 * W, -1 - 2 * W, -2 + 2 * W, -2 - 2 * W }
        for i = 1, #dirs do
            local p = dirs[i] + cell
            local ch = RPD.Actor:findChar(p)
            -- java: Random.Int(1) == 0 is always true, kept verbatim
            if lvl:cellValid(p) and ch ~= nil and ch:getEntityKind() ~= "Hero" and not ch:isNpc()
                    and RPD.Random:Int(1) == 0 and not ch:hasBuff("Necrotism") then
                -- set(duration, iteration + 1): level = iteration, data.left = duration
                local spread = RPD.Buffs.Buff:affect(ch, "Necrotism")
                spread:level(iteration + 1)
                spread:loadScriptData(serpent.dump({ left = DURATION + iteration + 1 }))
            end
        end

        local data = self.data or {}
        data.left = (data.left or 0) - 1
        self.data = data
        if data.left <= 0 then
            buff:detach()
        end
    end,

    onHeroDeath = function(self, buff)
        RPD.Badges:validateDeathFromNecrotism()
        RPD.Dungeon:fail(RPD.JavaUtils:format(
            RPD.ResultDescriptions:getDescription(RPD.ResultReason.NECROTISM), { RPD.Dungeon.depth }))
        RPD.glogn(RPD.textById("Necrotism_Death"))
    end,
}
