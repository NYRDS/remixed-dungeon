---
--- Blindness: shrinks view distance while active (was actors/buffs/Blindness.java)
---
local RPD  = require "scripts/lib/commonClasses"
local buff = require "scripts/lib/buff"

return buff.init{
    desc  = function ()
        return {
            icon          = 16, -- BuffIndicator.BLINDNESS
            name          = "BlindnessBuff_Name",
            info          = "BlindnessBuff_Info",
        }
    end,

    attached = function(self, buff)
        buff.target:observe()
    end,

    detach = function(self, buff)
        buff.target:observe()
    end
}
