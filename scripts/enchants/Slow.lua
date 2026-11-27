---
--- Slow: slows the defender (was weapon/enchantments/Slow.java).
---
local RPD = require "scripts/lib/commonClasses"

return {
    desc = function()
        return {
            chance  = 2,
            glowing = { color = 0x0044FF },
        }
    end,

    proc = function(self, enchant, weapon, attacker, defender, damage)
        -- lvl 0 - 25% / lvl 1 - 40% / lvl 2 - 50%
        local level = math.max(0, weapon:level())

        if RPD.Random:Int(level + 4) >= 3 then
            RPD.Buffs.Buff:prolong(defender, "Slow", RPD.Random:Float(1, 1.5 + level))
            return true
        end

        return false
    end,
}
