---
--- Swing: splashes the hit around the defender to every adjacent char (was
--- weapon/enchantments/Swing.java).
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

            local p = attacker:getPos()
            -- Level.NEIGHBOURS8 (initSizeDependentStuff order kept verbatim)
            local W = attacker:level():getWidth()
            local dirs = { 1, -1, W, -W, 1 + W, 1 - W, -1 + W, -1 - W }
            for i = 1, #dirs do
                local ch = RPD.Actor:findChar(dirs[i] + p)
                if ch and ch:getId() ~= defender:getId() and ch:isAlive() then
                    local dr = ch:defenceRoll(attacker)
                    local dmg = RPD.Random:Int(1, maxDamage)
                    local effectiveDamage = math.max(dmg - dr, 0)

                    ch:damage(effectiveDamage, enchant)

                    ch:getSprite():bloodBurstA(attacker:getSprite():center(), effectiveDamage)
                    ch:getSprite():flash()
                end
            end

            return true
        end

        return false
    end,
}
