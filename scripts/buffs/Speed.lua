---
--- Speed: haste bonus (was actors/buffs/Speed.java)
---
local RPD  = require "scripts/lib/commonClasses"
local buff = require "scripts/lib/buff"

return buff.init{
    desc  = function ()
        return {
            icon          = -1, -- no indicator icon
            name          = "SpeedBuff_Name",
            info          = "SpeedBuff_Info",
        }
    end,

    hasteLevel = function(self, buff)
        return 7.27254
    end
}
