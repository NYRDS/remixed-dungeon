---
--- Terror: pets flee, non-pets get Horrified AI via Mob.add dispatch (was actors/buffs/Terror.java)
---
local RPD  = require "scripts/lib/commonClasses"
local buff = require "scripts/lib/buff"

return buff.init{
    desc  = function ()
        return {
            icon          = 10, -- BuffIndicator.TERROR
            name          = "TerrorBuff_Name",
            info          = "TerrorBuff_Info",
        }
    end,

    attached = function(self, buff)
        local target = buff.target
        if target:isMob() and not target:isNeutral() then
            target:releasePet()
        end
    end,

    attachVisual = function(self, buff)
        buff.target:showStatus(RPD.CharSprite.NEGATIVE, RPD.textById("Char_StaFrightened"))
    end
}
