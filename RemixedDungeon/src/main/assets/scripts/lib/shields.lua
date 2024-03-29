---
--- Generated by EmmyLua(https://github.com/EmmyLua)
--- Created by mike.
--- DateTime: 11/5/19 11:02 PM
---

local RPD = require "scripts/lib/commonClasses"

local shields = {}

local stats = require "scripts.stats.shields"

local strForLevel = stats.strForLevel
local chanceForLevel = stats.chanceForLevel
local blockForLevel = stats.blockForLevel

function damageMin (str, shieldLevel)
    return math.max(str - 10, 0)
end

function damageMax (str, shieldLevel)
    return math.max(str - 10, 0) ^ (1 + shieldLevel * 0.1) + shieldLevel
end

---@param shieldLevel number
---@param itemLevel number
shields.blockDamage = function(shieldLevel, itemLevel)
    return blockForLevel[shieldLevel] * math.pow(1.3, itemLevel)
end

---@param shieldLevel number
---@param str number
shields.blockChance = function(shieldLevel, str)
    local weightPenalty = math.max(strForLevel[shieldLevel] - str, 0)
    return chanceForLevel[shieldLevel] * (1 - weightPenalty * 0.1)
end

---@param shieldLevel number
---@param str number
shields.rechargeTime = function(shieldLevel, str)
    local weightPenalty = math.max(strForLevel[shieldLevel] - str, 0)
    return 5 + weightPenalty
end

---@param shieldLevel number
---@param str number
shields.waitAfterBlockTime = function(shieldLevel, str)
    return math.max(str - strForLevel[shieldLevel], 0)
end

---@param baseDesc string
---@param shieldLevel number
---@param str number
shields.info = function(baseDesc, str, shieldLevel, itemLevel)

    local infoTemplate = RPD.textById("ShieldInfoTemplate")
    local strTemplate = RPD.textById("ShieldStrTemplate")

    return RPD.textById(baseDesc)
            .. "\n\n"
            .. RPD.format(infoTemplate,
            shields.blockDamage(shieldLevel, itemLevel),
            shields.blockChance(shieldLevel, str) * 100,
            shields.rechargeTime(shieldLevel, str))
            .. "\n\n"
            .. RPD.format(strTemplate, strForLevel[shieldLevel])
end

shields.infoWeapon = function(baseDesc, str, shieldLevel, itemLevel)

    local infoWeaponTemplate = RPD.textById("ShieldWeaponInfo")

    local avgDamage = damageMin(str, shieldLevel)
            + damageMax(str, shieldLevel)

    return RPD.textById(baseDesc)
            .. "\n\n"
            .. RPD.format(infoWeaponTemplate,
            avgDamage)
end

shields.makeShield = function(shieldLevel, shieldDesc, shieldBuff)
    return {
        activate = function(self, item, hero)
            if item:slotName() == RPD.Slots.leftHand then
                local shieldBuff = RPD.affectBuff(hero, shieldBuff or "ShieldLeft",
                        shields.rechargeTime(shieldLevel, hero:effectiveSTR()))
                shieldBuff:level(shieldLevel)
                shieldBuff:setSource(item)
            end
        end,

        info = function(self, item)
            local hero = item:getOwner()
            local str = hero:effectiveSTR()

            if item:slotName() == RPD.Slots.weapon then
                return shields.infoWeapon(shieldDesc, str, shieldLevel, item:level())
            else
                return shields.info(shieldDesc, str, shieldLevel, item:level())
            end
        end,

        typicalSTR = function(self, item)
            return strForLevel[shieldLevel]
        end,

        requiredSTR = function(self, item)
            return strForLevel[shieldLevel]
        end,

        slot = function(self, item, belongings)
            local rb, lb = belongings:slotBlocked(RPD.Slots.weapon), belongings:slotBlocked(RPD.Slots.leftHand)

            if rb and lb then
                return RPD.Slots.leftHand
            end

            if lb then
                return RPD.Slots.weapon
            end

            return RPD.Slots.leftHand
        end,

        accuracyFactor = function(self, item, user)
            return 1
        end,

        damageRoll = function(self, item, user)
            local str = user:effectiveSTR()
            return math.random(damageMin(str, shieldLevel),
                    damageMax(str, shieldLevel))
        end,

        attackDelayFactor = function(self, item, user)
            return 1
        end,

        attackProc = function(self, item, attacker, defender, damage)
            if item:slotName() == RPD.Slots.weapon then
                attacker:spend(1)
            end

            return damage
        end,

        goodForMelee = function(self, item)
            return item:slotName() == RPD.Slots.weapon
        end
    }
end

return shields