---
--- Invisibility: maintains the Char.invisible counter, breaks on casting
--- while seen (was actors/buffs/Invisibility.java)
---
local RPD  = require "scripts/lib/commonClasses"
local buff = require "scripts/lib/buff"

return buff.init{
    desc  = function ()
        return {
            icon          = 12, -- BuffIndicator.INVISIBLE
            name          = "InvisibilityBuff_Name",
            info          = "InvisibilityBuff_Info",
        }
    end,

    -- counter maintenance lives in CustomBuff.attachTo/detach for any
    -- INVISIBLE-state buff (beta.13 r3b) - no manual adjustInvisibility here

    spellCasted = function(self, buff, caster, spell)
        RPD.CharUtils:dispelInvisibility(buff.target)
    end,

    charSpriteStatus = function(self, buff)
        return "INVISIBLE"
    end,

    attachVisual = function(self, buff)
        buff.target:showStatus(RPD.CharSprite.POSITIVE, RPD.textById("Char_StaInvisible"))
    end
}
