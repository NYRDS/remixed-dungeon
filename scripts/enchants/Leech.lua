---
--- Leech: drains the defender into the attacker's wounds (was
--- weapon/enchantments/Leech.java).
---
local RPD = require "scripts/lib/commonClasses"

return {
    desc = function()
        return {
            chance  = 1,
            glowing = { color = 0x660022 },
        }
    end,

    proc = function(self, enchant, weapon, attacker, defender, damage)
        local level = math.max(0, weapon:level())

        -- lvl 0 - 33% / lvl 1 - 43% / lvl 2 - 50%
        local maxValue = math.floor(damage * (level + 2) / (level + 6))
        local effValue = math.min(RPD.Random:IntRange(0, maxValue), attacker:ht() - attacker:hp())

        if effValue > 0 then
            attacker:heal(effValue, enchant)
            attacker:showStatus(RPD.CharSprite.POSITIVE, tostring(effValue))
            return true
        end

        return false
    end,
}
