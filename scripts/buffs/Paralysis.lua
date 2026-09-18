---
--- Paralysis: paralyses until damage shake-off (was actors/buffs/Paralysis.java)
---
local RPD  = require "scripts/lib/commonClasses"
local buff = require "scripts/lib/buff"

return buff.init{
    desc  = function ()
        return {
            icon          = 4, -- BuffIndicator.PARALYSIS
            name          = "ParalysisBuff_Name",
            info          = "ParalysisBuff_Info",
        }
    end,

    attached = function(self, buff)
        buff.target:paralyse(true)
    end,

    detach = function(self, buff)
        buff.target:paralyse(false)
    end,

    damage = function(self, buff, dmg, src)
        local target = buff.target
        if RPD.Random:Int(dmg) >= RPD.Random:Int(target:hp()) then
            buff:detach()
            if RPD.CharUtils:isVisible(target) then
                RPD.GLog:i(RPD.JavaUtils:format(RPD.textById("Char_OutParalysis"),
                    { target:getName_objective() }), {})
            end
        end
        return dmg
    end,

    charSpriteStatus = function(self, buff)
        return "PARALYSED"
    end,

    attachVisual = function(self, buff)
        buff.target:showStatus(RPD.CharSprite.NEGATIVE, RPD.textById("Char_StaParalysed"))
    end
}
