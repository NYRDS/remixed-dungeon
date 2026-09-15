--[[
    mob lua migration batch 16b: replaces java NecromancerNPC
    (placed by PrisonLevel at depth 7, skipped for NECROMANCER heroes)
]]

local RPD = require "scripts/lib/commonClasses"

local mob = require "scripts/lib/mob"

local phrases = {
    "NecromancerNPC_Message1",
    "NecromancerNPC_Message2",
    "NecromancerNPC_Message3",
    "NecromancerNPC_Message4"
}

return mob.init({
    interact = function(self, chr)
        local data = mob.restoreData(self)

        if not data.introduced then
            RPD.showQuestWindow(self, "NecromancerNPC_Intro2")

            data.introduced = true
            mob.storeData(self, data)

            chr:collectAnimated(RPD.item("SkeletonKey"))
        else
            self:say(phrases[math.random(#phrases)])
        end

        return true
    end
})
