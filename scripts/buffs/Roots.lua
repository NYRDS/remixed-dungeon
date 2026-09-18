---
--- Roots: pins the target in place (was actors/buffs/Roots.java)
---
local RPD  = require "scripts/lib/commonClasses"
local buff = require "scripts/lib/buff"

local CharSprite = luajava.bindClass("com.watabou.pixeldungeon.sprites.CharSprite")

return buff.init{
    desc  = function ()
        return {
            icon          = 11, -- BuffIndicator.ROOTS
            name          = "RootsBuff_Name",
            info          = "RootsBuff_Info",
        }
    end,

    attachTo = function(self, buff, target)
        return not target:isFlying()
    end,

    attachVisual = function(self, buff)
        buff.target:showStatus(CharSprite.NEGATIVE, RPD.textById("Char_StaRooted"))
    end
}
