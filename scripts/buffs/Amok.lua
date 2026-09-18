---
--- Amok: berserk state, AI dispatch handled in Mob.add (was actors/buffs/Amok.java)
---
local RPD  = require "scripts/lib/commonClasses"
local buff = require "scripts/lib/buff"

local CharSprite = luajava.bindClass("com.watabou.pixeldungeon.sprites.CharSprite")

return buff.init{
    desc  = function ()
        return {
            icon          = 9, -- BuffIndicator.AMOK
            name          = "AmokBuff_Name",
            info          = "AmokBuff_Info",
        }
    end,

    attachVisual = function(self, buff)
        buff.target:showStatus(CharSprite.NEGATIVE, RPD.textById("Char_StaAmok"))
    end
}
