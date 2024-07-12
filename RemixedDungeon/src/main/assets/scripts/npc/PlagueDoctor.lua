--[[
    Created by mike.
    DateTime: 2024.05.31
    This file is part of pixel-dungeon-remix
]]

local RPD = require "scripts/lib/commonClasses"

local mob = require"scripts/lib/mob"

questList = {
    {
        prologue = {text = "Kill Black Rats!"},
        requirements = {kind= "Carcass of Rat", quantity = 5},
        in_progress = {text = "Killing Black Rats..."},
        reward = {kind = "Gold", quantity = 100},
        epilogue = {text = "Great Job!"}
    }
}

return mob.init({
    interact = function(self, chr)
        data = mob.restoreData(self)

        local questIndex = data["questIndex"]

        if data["questIndex"] > #questList then
            self:say("All quests complete!")
            return
        end

        if not data["questInProgress"] then
            RPD.showQuestWindow(self, questList[questIndex].prologue.text)

            data["questInProgress"] = true

            mob.storeData(self,data)
            return
        else

            local wantedItem = chr:checkItem(questList[questIndex].requirements.kind)
            local wantedQty = questList[questIndex].requirements.quantity
            local actualQty = wantedItem:quantity()
            if actualQty >= wantedQty then
                if wantedQty == actualQty then
                    wantedItem:removeItem()
                else
                    wantedItem:quantity(actualQty - wantedQty)
                end

                RPD.showQuestWindow(self, questList[questIndex].epilogue.text)

                data["questInProgress"] = false
                data["questIndex"] = questIndex+ 1

                local reward = RPD.item(questList[questIndex].reward.kind, questList[questIndex].reward.quantity)
                chr:collectAnimated(reward)

                mob.storeData(self,data)
                return

            else
                RPD.showQuestWindow(self,questList[questIndex].in_progress.text)
                return
            end
        end

    end,
    spawn = function (self,level)
        level:setCompassTarget(self:getPos())
        data = mob.restoreData(self)
        data["questIndex"] = 1
        data["questInProgress"] = false
        mob.storeData(self,data)
    end
})
