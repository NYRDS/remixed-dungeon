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
        reward = {kind = "gold", quantity = 100},
        epilogue = {text = "Great Job!"}
    }
}

return mob.init({
    interact = function(self, chr)
        if questIndex > #questList then
            self:say("All quests complete!")
            return
        end

        if not self.data["questInProgress"] then
            self:say(questList[questIndex].prologue.text)
            self.data["questInProgress"] = true
            return
        else

            local wantedItem = chr:checkItem(questList[questIndex].requirements.kind)
            if wantedItem:quantity() >= questList[questIndex].requirements.quantity then
                self:say(questList[questIndex].epilogue.text)
                self.data["questInProgress"] = false
                self.data["questIndex"] = self.data["questIndex"] + 1
                return
            end
        end

    end
    spawn = function (self,level)
        level:setCompassTarget(self:getPos())
        self.data["questIndex"] = 1
        self.data["questInProgress"] = false
    end
})
