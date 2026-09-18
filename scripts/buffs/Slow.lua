---
--- Slow: heavy haste penalty (was actors/buffs/Slow.java)
---
local RPD  = require "scripts/lib/commonClasses"
local buff = require "scripts/lib/buff"

return buff.init{
    desc  = function ()
        return {
            icon          = 7, -- BuffIndicator.SLOW
            name          = "SlowBuff_Name",
            info          = "SlowBuff_Info",
        }
    end,

    hasteLevel = function(self, buff)
        return -7.27254
    end,

    attachVisual = function(self, buff)
        buff.target:showStatus(RPD.CharSprite.NEGATIVE, RPD.textById("Char_StaSlowed"))
    end
}
