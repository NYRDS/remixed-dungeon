---
--- Instability: fires a fresh random enchantment on every proc; Boomerangs
--- never reroll into the line-shaped kinds (was
--- weapon/enchantments/Instability.java).
---
local RPD = require "scripts/lib/commonClasses"

return {
    desc = function()
        return {
            chance = 3,
        }
    end,

    proc = function(self, enchant, weapon, attacker, defender, damage)
        local ench = RPD.Enchantment:random()
        if weapon:getEntityKind() == "Boomerang" then
            while ench:getEntityKind() == "Piercing" or ench:getEntityKind() == "Swing" do
                ench = RPD.Enchantment:random()
            end
        end
        return ench:proc(weapon, attacker, defender, damage)
    end,
}
