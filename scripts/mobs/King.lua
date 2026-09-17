--[[
  King (batch 17d-4, was actors/mobs/King.java).
  Dwarf king, City boss. While his army cap - which grows as he takes
  damage - allows more servants, he heads for the pedestals and raises
  undead city mobs there; the walk and the summoning live in the
  KingPedestal ai state (scripts/ai/KingPedestal). This script only
  decides when that state is worth entering, keeps the skeleton key and
  the armor kit in his belongings, and runs the boss yells.
]]
local RPD = require "scripts/lib/commonClasses"
local mob = require "scripts/lib/mob"
local kstate = require "scripts/ai/KingPedestal"

return mob.init{
    spawn = function(self, level)
        local bag = self:getBelongings()

        if not bag:getItem("SkeletonKey") then
            self:collect(RPD.item("SkeletonKey"))
        end

        if not bag:getItem("ArmorKit") then
            self:collect(RPD.item("ArmorKit"))
        end
    end,

    act = function(self)
        -- sleeping/passive king stays put; java also only retargeted to
        -- pedestals once he was up and moving (Wandering or Hunting)
        local tag = self:getState():getTag()
        if tag ~= "HUNTING" and tag ~= "WANDERING" then
            return
        end

        local data = mob.restoreData(self)
        data.targetPedestal = kstate.nearestPedestal(self, data)

        if kstate.canTryToSummon(self, data) then
            RPD.setAi(self, "KingPedestal")
        end
    end,

    die = function(self, cause)
        RPD.CharUtils:validateBossSlain("BOSS_SLAIN_4")
        self:yell(RPD.textById("King_Info1"):format(RPD.Dungeon.hero:getHeroClass():title()))
    end,

    notice = function(self)
        self:yell(RPD.textById("King_Info3"))
    end,
}
