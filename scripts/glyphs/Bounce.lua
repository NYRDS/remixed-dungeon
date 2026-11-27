---
--- Bounce: knocks the attacker back on hits (was armor/glyphs/Bounce.java).
--- Name formatting rides the framework default: <Kind>_Txt with the armor name.
---
local RPD = require "scripts/lib/commonClasses"

return {
    desc = function()
        return {
            chance = 1,
        }
    end,

    defenceProc = function(self, glyph, armor, attacker, defender, damage)
        local armorLevel = math.max(0, armor:level())

        if attacker:isMovable() and attacker:adjacent(defender) and RPD.Random:Int(armorLevel + 5) >= 4 then
            attacker:push(defender)
        end

        return damage
    end,
}
