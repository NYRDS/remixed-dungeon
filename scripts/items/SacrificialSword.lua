---
--- SacrificialSword: data weapon kind (was weapon/melee/SacrificialSword.java / guts / common).
---
local RPD      = require "scripts/lib/commonClasses"
local itemLib  = require "scripts/lib/item"
local weapon   = require "scripts/lib/weapon"

return weapon.init{
    desc = function()
        return {
            tier      = 2,
            acu       = 1.0,
            dly       = 1.0,
            image     = 4,
            enchatable = false,
            imageFile = "items/swords.png",
        }
    end,

    postAttack = function(self, item, tgt)
        -- was SacrificialSword.postAttack: convert the struck foe into a pet
        if tgt:isBoss() or tgt:isNpc() then return end
        local user = item:getOwner()
        local armorPenalty = user:getItemFromSlot(RPD.Slots.armor):effectiveDr() / 8.0

        local classBonus = 1
        local sub = tostring(user:getSubClass())
        local cls = tostring(user:getHeroClass())
        if sub == "WARDEN" then classBonus = 1.5 end
        if sub == "SHAMAN" then classBonus = 2.0 end
        if cls == "NECROMANCER" or cls == "DOCTOR" then classBonus = 3.0 end

        local conversionChance = (0.25 + (user:lvl() * 4 + item:level() ^ 2) * 0.01)
            * classBonus / armorPenalty
            - tgt:defenseSkill(user) * 0.01 * tgt:speed()
            - tgt:hp() * 0.01

        if math.random() < conversionChance then
            RPD.Mob:makePet(tgt, user:getId())
        end
    end,
    glowing = function(self, item)
        -- pulsing red, visible ~30% of the time (was SacrificialSword.glowing)
        if RPD.Random:Float(1) < 0.3 then
            return itemLib.makeGlowing(0xAA0000, 1)
        end
        return nil
    end,
}
