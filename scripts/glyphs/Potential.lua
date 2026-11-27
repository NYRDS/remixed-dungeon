---
--- Potential: discharges electricity through both sides of the exchange (was
--- armor/glyphs/Potential.java).
---
local RPD = require "scripts/lib/commonClasses"

return {
    desc = function()
        return {
            chance  = 1,
            glowing = { color = 0x66CCEE },
        }
    end,

    defenceProc = function(self, glyph, armor, attacker, defender, damage)
        local level = math.max(0, armor:level())

        if attacker:adjacent(defender) and RPD.Random:Int(level + 7) >= 6 then

            local dmg = RPD.Random:IntRange(1, damage)
            attacker:damage(dmg, RPD.Electricity.INSTANCE)
            dmg = RPD.Random:IntRange(1, dmg)
            defender:damage(dmg, RPD.Electricity.INSTANCE)
            if defender:getId() == RPD.Dungeon.hero:getId() then
                RPD.shakeCamera(2, 0.3)
            end

            RPD.Lightning:boltBetween(attacker, defender)
        end

        return damage
    end,
}
