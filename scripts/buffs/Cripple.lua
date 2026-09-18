---
--- Cripple: halves movement speed (was actors/buffs/Cripple.java)
---
local RPD  = require "scripts/lib/commonClasses"
local buff = require "scripts/lib/buff"

return buff.init{
    desc  = function ()
        return {
            icon          = 23, -- BuffIndicator.CRIPPLE
            name          = "CrippleBuff_Name",
            info          = "CrippleBuff_Info",
        }
    end,

    speedMultiplier = function(self, buff)
        return 0.5
    end,

    attachVisual = function(self, buff)
        buff.target:showStatus(RPD.CharSprite.NEGATIVE, RPD.textById("Char_StaCrippled"))
    end
}
