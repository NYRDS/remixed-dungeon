---
--- SnipersMark: marks a target for the sniper (was actors/buffs/SnipersMark.java)
---
local RPD  = require "scripts/lib/commonClasses"
local buff = require "scripts/lib/buff"

return buff.init{
    desc  = function ()
        return {
            icon          = 27, -- BuffIndicator.MARK
            name          = "SnipersMarkBuff_Name",
            info          = "SnipersMarkBuff_Info",
        }
    end
}
