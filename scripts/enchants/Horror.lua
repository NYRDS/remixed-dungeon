---
--- Horror: terrifies mobs, vertigos the hero (was
--- weapon/enchantments/Horror.java).
---
local RPD = require "scripts/lib/commonClasses"

return {
    desc = function()
        return {
            chance  = 2,
            glowing = { color = 0x222222 },
        }
    end,

    proc = function(self, enchant, weapon, attacker, defender, damage)
        -- lvl 0 - 20% / lvl 1 - 33% / lvl 2 - 43%
        local level = math.max(0, weapon:level())

        if RPD.Random:Int(level + 5) >= 4 then
            if defender:getId() == RPD.Dungeon.hero:getId() then
                RPD.Buffs.Buff:affect(defender, "Vertigo", RPD.CharUtils:durationFactor(defender) * 10)
            else
                RPD.Buffs.Buff:affect(defender, "Terror", 10)
            end
            return true
        end

        return false
    end,
}
