---
--- Poison: poisons the defender (was weapon/enchantments/Poison.java).
---
local RPD = require "scripts/lib/commonClasses"

return {
    desc = function()
        return {
            chance  = 10,
            glowing = { color = 0x4400AA },
        }
    end,

    proc = function(self, enchant, weapon, attacker, defender, damage)
        -- lvl 0 - 33% / lvl 1 - 50% / lvl 2 - 60%
        local level = math.max(0, weapon:level())

        if RPD.Random:Int(level + 3) >= 2 then
            RPD.Buffs.Buff:affect(defender, "Poison", RPD.CharUtils:durationFactor(defender) * (level + 1))
            return true
        end

        return false
    end,
}
