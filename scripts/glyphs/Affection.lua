---
--- Affection: charms both sides of the exchange (was armor/glyphs/Affection.java).
---
local RPD = require "scripts/lib/commonClasses"

return {
    desc = function()
        return {
            chance  = 1,
            glowing = { color = 0xFF4488 },
        }
    end,

    defenceProc = function(self, glyph, armor, attacker, defender, damage)
        -- was GameMath.gate(0, armor.level(), 6)
        local level = math.max(0, math.min(armor:level(), 6))
        if attacker:adjacent(defender) and RPD.Random:Int(math.floor(level / 2) + 5) >= 4 then
            local duration = RPD.Random:IntRange(2, 5)
            RPD.Buffs.Buff:affect(attacker, "Charm", RPD.CharUtils:charmDurationFactor(attacker) * duration)
            attacker:getSprite():centerEmitter():start(RPD.Sfx.Speck:factory(RPD.Sfx.Speck.HEART), 0.2, 5)
            RPD.Buffs.Buff:affect(defender, "Charm",
                RPD.Random:Float(RPD.CharUtils:charmDurationFactor(defender) * duration / 2, duration))
            defender:getSprite():centerEmitter():start(RPD.Sfx.Speck:factory(RPD.Sfx.Speck.HEART), 0.2, 5)
        end
        return damage
    end,
}
