--
-- User: mike
-- Date: 25.11.2017
-- Time: 22:56
-- This file is part of Remixed Pixel Dungeon.
--

local RPD = require "scripts/lib/commonClasses"

local mob = require"scripts/lib/mob"

local quest = require"scripts/lib/quest"

local questName = "Demo Quest"

return mob.init({
    interact = function(self, chr)
        if not quest.isGiven(questName) then
            self:say("Hi adventurer! Kill Black Rats! And Reward Follows!")
            quest.give(questName, chr, {kills={"BlackRat"}})
            return
        end

        if quest.isCompleted(questName) then
            self:say("Thanks!")
            return
        end

        local ratsKilled = quest.state(questName).kills.BlackRat or 0

        if ratsKilled == 0 then
            return
        end

        if ratsKilled < 5 then
            self:say(ratsKilled.." killed so far, keep going!")
        else
            self:say("Great Work! Here your reward!")
            RPD.Dungeon.level:drop(RPD.ItemFactory:itemByName("RatArmor"),chr:getPos())
            quest.complete(questName)
        end
    end,
    spawn = function (self,level)
        level:setCompassTarget(self:getPos())
    end
})
