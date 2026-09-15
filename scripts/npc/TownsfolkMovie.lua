--[[
    mob lua migration batch 16b: replaces java TownsfolkMovieNPC
]]

local RPD = require "scripts/lib/commonClasses"

local mob = require "scripts/lib/mob"

return mob.init({
    interact = function(self, chr)
        RPD.showQuestWindow(self, "TownsfolkMovieNPC_Message")
    end
})
