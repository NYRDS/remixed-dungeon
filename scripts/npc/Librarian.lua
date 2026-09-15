--[[
    mob lua migration batch 16b: replaces java LibrarianNPC
]]

local RPD = require "scripts/lib/commonClasses"

local mob = require "scripts/lib/mob"

return mob.init({
    interact = function(self, chr)
        RPD.showQuestWindow(self, "LibrarianNPC_Message_Instruction")
    end
})
