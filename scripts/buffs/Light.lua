---
--- Light: +1 view distance while active (was actors/buffs/Light.java)
---
local RPD  = require "scripts/lib/commonClasses"
local buff = require "scripts/lib/buff"

return buff.init{
    desc  = function ()
        return {
            icon          = 22, -- BuffIndicator.LIGHT
            name          = "LightBuff_Name",
            info          = "LightBuff_Info",
        }
    end,

    attached = function(self, buff)
        buff.target:observe()
    end,

    detach = function(self, buff)
        buff.target:observe()
    end,

    charSpriteStatus = function(self, buff)
        return "ILLUMINATED"
    end
}
