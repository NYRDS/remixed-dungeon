---
--- Vertigo: disorients the target (was actors/buffs/Vertigo.java)
---
local RPD  = require "scripts/lib/commonClasses"
local buff = require "scripts/lib/buff"

return buff.init{
    desc  = function ()
        return {
            icon          = 29, -- BuffIndicator.VERTIGO
            name          = "VertigoBuff_Name",
            info          = "VertigoBuff_Info",
        }
    end,

    attachVisual = function(self, buff)
        buff.target:showStatus(RPD.CharSprite.NEGATIVE, RPD.textById("Char_StaDizzy"))
    end
}
