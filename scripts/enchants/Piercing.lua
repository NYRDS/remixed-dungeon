---
--- Piercing: carries the hit through the defender down the attack line (was
--- weapon/enchantments/Piercing.java).
---
local RPD = require "scripts/lib/commonClasses"

return {
    desc = function()
        return {
            chance = 3,
        }
    end,

    proc = function(self, enchant, weapon, attacker, defender, damage)
        local level = math.max(0, weapon:level())

        local maxDamage = math.floor(damage * 2 ^ (-1 / (level + 1)))
        if maxDamage >= 1 then

            local d = defender:getPos() - attacker:getPos()
            local pos = defender:getPos() + d

            while true do
                local ch = RPD.Actor:findChar(pos)
                if not ch then
                    break
                end

                local dr = ch:defenceRoll(attacker)
                local dmg = RPD.Random:Int(1, maxDamage)
                local effectiveDamage = math.max(dmg - dr, 0)

                ch:damage(effectiveDamage, enchant)

                ch:getSprite():bloodBurstA(attacker:getSprite():center(), effectiveDamage)
                ch:getSprite():flash()

                pos = pos + d
                if pos < 0 or pos >= RPD.Dungeon.level:getLength() then
                    break
                end
            end

            return true
        end

        return false
    end,
}
