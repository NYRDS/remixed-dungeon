---
--- Stench: poisons the air around the attacker (was armor/glyphs/Stench.java).
---
local RPD = require "scripts/lib/commonClasses"

return {
    desc = function()
        return {
            chance  = 1,
            glowing = { color = 0x22CC44 },
        }
    end,

    defenceProc = function(self, glyph, armor, attacker, defender, damage)
        local level = math.max(0, armor:level())

        if attacker:adjacent(defender) and RPD.Random:Int(level + 5) >= 4 then
            RPD.placeBlob(RPD.Blobs.ToxicGas, attacker:getPos(), 20)
        end

        return damage
    end,
}
