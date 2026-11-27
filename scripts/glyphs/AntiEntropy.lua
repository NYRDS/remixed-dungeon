---
--- AntiEntropy: freezes the attacker, ignites the defender (was
--- armor/glyphs/AntiEntropy.java).
---
local RPD = require "scripts/lib/commonClasses"

return {
    desc = function()
        return {
            chance  = 1,
            glowing = { color = 0x0000FF },
        }
    end,

    defenceProc = function(self, glyph, armor, attacker, defender, damage)
        local level = math.max(0, armor:level())

        if attacker:adjacent(defender) and RPD.Random:Int(level + 6) >= 5 then
            RPD.Buffs.Buff:prolong(attacker, "Frost", RPD.CharUtils:durationFactor(attacker) * 5 * RPD.Random:Float(1, 1.5))
            RPD.Sfx.CellEmitter:get(attacker:getPos()):start(RPD.Sfx.SnowParticle.FACTORY, 0.2, 6)

            RPD.CharUtils:ignite(defender)
            defender:getSprite():emitter():burst(RPD.Sfx.FlameParticle.FACTORY, 5)
        end

        return damage
    end,
}
