---
--- Death: the grim enchant - instant kills with a shadow burst (was
--- weapon/enchantments/Death.java).
---
local RPD = require "scripts/lib/commonClasses"

return {
    desc = function()
        return {
            chance  = 1,
            glowing = { color = 0x000000 },
        }
    end,

    proc = function(self, enchant, weapon, attacker, defender, damage)
        -- lvl 0 - 8% / lvl 1 ~ 9% / lvl 2 ~ 10%
        local level = math.max(0, weapon:level())

        if RPD.Random:Int(level + 100) >= 92 then
            defender:damage(defender:hp(), enchant)
            defender:getSprite():emitter():burst(RPD.Sfx.ShadowParticle.UP, 5)

            if not defender:isAlive() and attacker:getEntityKind() == "Hero" then
                RPD.Badges:validateGrimWeapon()
            end
            return true
        end

        return false
    end,
}
