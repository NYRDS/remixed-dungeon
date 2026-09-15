--[[
    mob lua migration batch 16b: replaces java TownGuardNPC
]]

local RPD = require "scripts/lib/commonClasses"

local mob = require "scripts/lib/mob"

local phrases = {
    "TownGuardNPC_Message1",
    "TownGuardNPC_Message2",
    "TownGuardNPC_Message3"
}

return mob.init({
    interact = function(self, chr)
        RPD.showQuestWindow(self, phrases[math.random(#phrases)])
    end
})
