---
--- Sleep: magical sleep, AI dispatch handled in Mob.add (was actors/buffs/Sleep.java)
---
local RPD  = require "scripts/lib/commonClasses"
local buff = require "scripts/lib/buff"

return buff.init{
    desc  = function ()
        return {
            icon          = -1, -- no indicator icon
            name          = "SleepBuff_Name",
            info          = "SleepBuff_Info",
        }
    end,

    attachVisual = function(self, buff)
        buff.target:getSprite():idle()
    end
}
