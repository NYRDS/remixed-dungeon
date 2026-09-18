---
--- Awareness: item searching; view recalculated on detach
--- (was actors/buffs/Awareness.java)
---
local RPD  = require "scripts/lib/commonClasses"
local buff = require "scripts/lib/buff"

return buff.init{
    desc  = function ()
        return {
            icon          = -1, -- no indicator icon
            name          = "AwarenessBuff_Name",
            info          = "AwarenessBuff_Info",
        }
    end,

    detach = function(self, buff)
        buff.target:observe()
    end
}
