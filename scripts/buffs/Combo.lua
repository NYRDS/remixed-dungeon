---
--- Combo: gladiator combo counter; hit math in CharUtils.comboHit
--- (was actors/buffs/Combo.java)
---
local RPD  = require "scripts/lib/commonClasses"
local buff = require "scripts/lib/buff"

return buff.init{
    desc  = function ()
        return {
            icon          = 17, -- BuffIndicator.COMBO
            name          = "ComboBuff_Name",
            info          = "ComboBuff_Info",
        }
    end,

    act = function(self, buff)
        buff:detach()
    end
}
