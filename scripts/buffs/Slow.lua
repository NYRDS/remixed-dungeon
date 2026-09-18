---
--- Slow: heavy haste penalty (was actors/buffs/Slow.java)
---
local RPD  = require "scripts/lib/commonClasses"
local buff = require "scripts/lib/buff"

local CharSprite = luajava.bindClass("com.watabou.pixeldungeon.sprites.CharSprite")

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
        buff.target:showStatus(CharSprite.NEGATIVE, RPD.textById("Char_StaSlowed"))
    end
}
