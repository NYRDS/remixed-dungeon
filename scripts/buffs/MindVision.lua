---
--- MindVision: see through walls; view recalculated on detach
--- (was actors/buffs/MindVision.java)
---
local RPD  = require "scripts/lib/commonClasses"
local buff = require "scripts/lib/buff"

return buff.init{
    desc  = function ()
        return {
            icon          = 0, -- BuffIndicator.MIND_VISION
            name          = "MindVisionBuff_Name",
            info          = "MindVisionBuff_Info",
        }
    end,

    detach = function(self, buff)
        buff.target:observe()
    end,

    attachVisual = function(self, buff)
        buff.target:showStatus(RPD.CharSprite.POSITIVE, RPD.textById("Char_StaMind"))
        buff.target:showStatus(RPD.CharSprite.POSITIVE, RPD.textById("Char_StaVision"))
    end
}
