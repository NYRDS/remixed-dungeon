---
--- Luck: rerolls the attacker's damage up to (best - i) per weapon level (was
--- weapon/enchantments/Luck.java).
---
local RPD = require "scripts/lib/commonClasses"

return {
    desc = function()
        return {
            chance  = 2,
            glowing = { color = 0x00FF00 },
        }
    end,

    proc = function(self, enchant, weapon, attacker, defender, damage)
        local level = math.max(0, weapon:level())

        local dmg = damage
        for i = 1, level + 1 do
            dmg = math.max(dmg, attacker:damageRoll() - i)
        end

        if dmg > damage then
            defender:damage(dmg - damage, enchant)
            return true
        end

        return false
    end,
}
