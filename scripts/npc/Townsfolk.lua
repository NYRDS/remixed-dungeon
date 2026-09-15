--[[
    mob lua migration batch 16b: replaces java TownsfolkNPC
]]

local RPD = require "scripts/lib/commonClasses"

local mob = require "scripts/lib/mob"

local phrases = {
    "TownsfolkNPC_Message1",
    "TownsfolkNPC_Message2",
    "TownsfolkNPC_Message3",
    "TownsfolkNPC_Message4"
}

return mob.init({
    interact = function(self, chr)
        RPD.showQuestWindow(self, phrases[math.random(#phrases)])
    end
})
