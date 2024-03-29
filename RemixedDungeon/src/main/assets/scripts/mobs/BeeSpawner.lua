--
-- User: Logodum
-- Date: 16.12.2021
-- Time: 15:50
-- Generated by EmmyLua(https://github.com/EmmyLua)
-- This file is part of Remixed Pixel Dungeon.
--

local RPD = require "scripts/lib/commonClasses"
local mob = require "scripts/lib/mob"
local mobBeeCountMin = 3
local mobBeeCountMax = 8
local mobBeeCount = math.random(mobBeeCountMin, mobBeeCountMax)

return mob.init({
    spawn = function(self, level)
        RPD.setAi(self, "None")
    end,

    die = function(self, cause)
        local level = RPD.Dungeon.level
        if mobBeeCount >= 1 then
            for _ = 1, mobBeeCount do
                local mobBee = RPD.MobFactory:mobByName("Bee")
                local pos = self:emptyCellNextTo()
                if level:cellValid(pos) and mobBeeCount > 0 then
                    mobBeeCount = mobBeeCount - 1
                    mobBee:lvl(self:lvl())
                    mobBee:setPos(pos)
                    RPD.setAi(mobBee, "Hunting")
                    level:spawnMob(mobBee)
                end
            end
        end
    end,

    damage = function(self, dmg, src)
        local level = RPD.Dungeon.level

        local mobBee = RPD.MobFactory:mobByName("Bee")
        local pos = self:emptyCellNextTo()
        if level:cellValid(pos) and mobBeeCount > 0 then
            mobBeeCount = mobBeeCount - 1
            mobBee:lvl(self:lvl())
            mobBee:setPos(pos)
            RPD.setAi(mobBee, "Hunting")
            level:spawnMob(mobBee)
        end

    end
})
