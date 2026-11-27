---
--- Fire: ignites and burns the defender (was weapon/enchantments/Fire.java).
---
local RPD = require "scripts/lib/commonClasses"

return {
    desc = function()
        return {
            chance  = 10,
            glowing = { color = 0xFF4400 },
        }
    end,

    proc = function(self, enchant, weapon, attacker, defender, damage)
        -- lvl 0 - 33% / lvl 1 - 50% / lvl 2 - 60%
        local level = math.max(0, weapon:level())

        if RPD.Random:Int(level + 3) >= 2 then
            if RPD.Random:Int(2) == 0 then
                RPD.CharUtils:ignite(defender)
            end
            defender:damage(RPD.Random:Int(1, level + 2), enchant)
            defender:getSprite():emitter():burst(RPD.Sfx.FlameParticle.FACTORY, level + 1)
            return true
        end

        return false
    end,
}
