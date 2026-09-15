--[[
    mob lua migration batch 16b: replaces java TownsfolkSilentNPC
]]

local RPD = require "scripts/lib/commonClasses"

local mob = require "scripts/lib/mob"

local phrases = {
    "TownsfolkSilentNPC_Message1",
    "TownsfolkSilentNPC_Message2",
    "TownsfolkSilentNPC_Message3"
}

return mob.init({
    interact = function(self, chr)
        RPD.showQuestWindow(self, phrases[math.random(#phrases)])
    end
})
