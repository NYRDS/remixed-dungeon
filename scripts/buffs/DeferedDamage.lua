---
--- DeferedDamage: Viscosity glyph's deferred damage accumulator (was
--- Viscosity.DeferedDamage). The pending amount rides the buff level
--- (persisted); the glyph script creates the buff and postpones its first
--- tick to the next turn.
---
local RPD  = require "scripts/lib/commonClasses"
local buff = require "scripts/lib/buff"

return buff.init{
    desc  = function ()
        return {
            icon          = 28, -- BuffIndicator.DEFERRED
            name          = "DeferedDamageBuff_Name",
            info          = "DeferedDamageBuff_Info",
        }
    end,

    -- dynamic name/info: the pending damage is formatted in
    name = function(self, buff)
        return RPD.JavaUtils:format(RPD.textById("DeferedDamageBuff_Name"), { buff:level() })
    end,

    info = function(self, buff)
        return RPD.JavaUtils:format(RPD.textById("DeferedDamageBuff_Info"), { buff:level() })
    end,

    act = function(self, buff)
        local target = buff.target
        if not target:isAlive() then
            buff:detach()
            return
        end

        target:damage(1, buff)

        buff:spend(1) -- Actor.TICK

        local left = buff:level() - 1
        buff:level(left)
        if left <= 0 then
            buff:detach()
        end
    end,

    onHeroDeath = function(self, buff)
        -- was the java act() inline hero-death check: death from the deferred
        -- drain is reported as a glyph death (the java buff had no Doom path)
        local glyphName = RPD.JavaUtils:format(RPD.textById("Viscosity_Txt"), { RPD.textById("Armor_Glyph") })
        RPD.Badges:validateDeathFromGlyph()
        RPD.Dungeon:fail(RPD.JavaUtils:format(
            RPD.ResultDescriptions:getDescription(RPD.ResultReason.GLYPH), { glyphName, RPD.Dungeon.depth }))
        RPD.glogn(RPD.textById("DeferedDamage_Killed_Txt"), glyphName)
    end,
}
