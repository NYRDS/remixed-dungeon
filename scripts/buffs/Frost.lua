---
--- Frost: freezes the target, extinguishes Burning, shatters carried
--- potions; any damage breaks it (was actors/buffs/Frost.java)
---
local RPD  = require "scripts/lib/commonClasses"
local buff = require "scripts/lib/buff"

return buff.init{
    desc  = function ()
        return {
            icon          = 15, -- BuffIndicator.FROST
            name          = "FrostBuff_Name",
            info          = "FrostBuff_Info",
        }
    end,

    attached = function(self, buff)
        local target = buff.target
        target:paralyse(true)
        RPD.removeBuff(target, "Burning")
        RPD.CharUtils:freezeCarriedItems(buff)
    end,

    detach = function(self, buff)
        buff.target:paralyse(false)
    end,

    damage = function(self, buff, dmg, src)
        buff:detach()
        return dmg
    end,

    charSpriteStatus = function(self, buff)
        return "FROZEN"
    end
}
