---
--- Paralysis: stuns the defender (was weapon/enchantments/Paralysis.java).
---
local RPD = require "scripts/lib/commonClasses"

return {
    desc = function()
        return {
            chance  = 2,
            glowing = { color = 0xCCAA44 },
        }
    end,

    proc = function(self, enchant, weapon, attacker, defender, damage)
        -- lvl 0 - 13% / lvl 1 - 22% / lvl 2 - 30%
        local level = math.max(0, weapon:level())

        if RPD.Random:Int(level + 8) >= 7 then
            RPD.Buffs.Buff:prolong(defender, "Stun", RPD.Random:Float(1, 1.5 + level))
            return true
        end

        return false
    end,
}
