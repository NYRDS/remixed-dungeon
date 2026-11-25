---
--- RingOfFrost: cyan glowing always-known ring; its FrostAura buff slows and
--- occasionally freezes melee attackers standing next to the wearer
--- (was common/rings/RingOfFrost.java).
---
local RPD     = require "scripts/lib/commonClasses"
local itemLib = require "scripts/lib/item"
local ring    = require "scripts/lib/ring"

return ring.init{
    desc = function()
        return {
            buff       = "FrostAura",
            price      = 80,
            identified = true,
            upgradable = true,
            image      = 13,
            imageFile  = "items/rings.png",
        }
    end,
    glowing = function(self, item)
        return itemLib.makeGlowing(0x00FFFF, 1) -- was Glowing(0x00FFFF) = period 1
    end,
    defenceProc = function(self, item, buff, defender, enemy, damage)
        -- was enemy.distance(defender) < 2
        if RPD.Dungeon.level:distance(enemy:getPos(), defender:getPos()) < 2 then
            if enemy:isAlive() then
                RPD.affectBuff(enemy, "Slow", RPD.CharUtils:durationFactor(enemy) * 10 / 5 + buff:level())
                if RPD.Random:Int(100) < 10 + buff:level() then
                    RPD.affectBuff(enemy, "Frost", RPD.CharUtils:durationFactor(enemy) * 5 / 5 + buff:level())
                end
                enemy:damage(buff:level() / 2, buff)
            end
        end
        return damage
    end,
    icon = function(self, item, buff)
        return RPD.BuffIndicator.FROSTAURA
    end,
}
