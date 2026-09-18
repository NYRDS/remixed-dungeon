---
--- Stun: paralyses while active (was actors/buffs/Stun.java)
---
local RPD  = require "scripts/lib/commonClasses"
local buff = require "scripts/lib/buff"

return buff.init{
    desc  = function ()
        return {
            icon          = 4, -- BuffIndicator.PARALYSIS
            name          = "StunBuff_Name",
            info          = "StunBuff_Info",
        }
    end,

    attached = function(self, buff)
        buff.target:paralyse(true)
    end,

    detach = function(self, buff)
        buff.target:paralyse(false)
    end,

    charSpriteStatus = function(self, buff)
        return "PARALYSED"
    end,

    attachVisual = function(self, buff)
        buff.target:showStatus(RPD.CharSprite.NEGATIVE, RPD.textById("Char_StaStunned"))
    end
}
