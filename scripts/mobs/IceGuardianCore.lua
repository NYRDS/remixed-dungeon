--[[
  IceGuardianCore (batch 17d-1, was mobs/icecaves/IceGuardianCore.java).
  Amber heart of the guardian pair: killing it takes every IceGuardian on
  the level with it (same cause), and badges the kill. Carries the ice
  key and an upgraded WandOfIcebolt.
]]
local RPD = require "scripts/lib/commonClasses"
local mob = require "scripts/lib/mob"

return mob.init{
    spawn = function(self, level)
        local bag = self:getBelongings()

        if not bag:getItem("WandOfIcebolt") then
            self:collect(RPD.item("WandOfIcebolt"):upgrade(1))
        end

        if not bag:getItem("IceKey") then
            self:collect(RPD.item("IceKey"))
        end
    end,

    die = function(self, cause)
        for _, m in pairs(RPD.Dungeon.level:getMobs()) do
            if m:getEntityKind() == "IceGuardian" then
                m:die(cause)
            end
        end
        RPD.CharUtils:validateBossSlain("ICE_GUARDIAN_SLAIN")
    end,
}
