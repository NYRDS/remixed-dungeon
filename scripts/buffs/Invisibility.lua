---
--- Invisibility: maintains the Char.invisible counter, breaks on casting
--- while seen (was actors/buffs/Invisibility.java)
---
local RPD  = require "scripts/lib/commonClasses"
local buff = require "scripts/lib/buff"

local CharSprite = luajava.bindClass("com.watabou.pixeldungeon.sprites.CharSprite")

return buff.init{
    desc  = function ()
        return {
            icon          = 12, -- BuffIndicator.INVISIBLE
            name          = "InvisibilityBuff_Name",
            info          = "InvisibilityBuff_Info",
        }
    end,

    attachTo = function(self, buff, target)
        target:adjustInvisibility(1)
        return true
    end,

    detach = function(self, buff)
        buff.target:adjustInvisibility(-1)
    end,

    spellCasted = function(self, buff, caster, spell)
        RPD.CharUtils:dispelInvisibility(buff.target)
    end,

    charSpriteStatus = function(self, buff)
        return "INVISIBLE"
    end,

    attachVisual = function(self, buff)
        buff.target:showStatus(CharSprite.POSITIVE, RPD.textById("Char_StaInvisible"))
    end
}
