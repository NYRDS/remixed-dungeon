---
--- Blessed: attack and defence skill bonus (was actors/buffs/Blessed.java)
---
local RPD  = require "scripts/lib/commonClasses"
local buff = require "scripts/lib/buff"

return buff.init{
    desc  = function ()
        return {
            icon          = 42, -- BuffIndicator.BLEESSED
            name          = "BlessedBuff_Name",
            info          = "BlessedBuff_Info",
        }
    end,

    defenceSkillBonus = function(self, buff)
        return buff:level()
    end,

    attackSkillBonus = function(self, buff)
        return buff:level()
    end
}
