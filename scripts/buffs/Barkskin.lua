---
--- Barkskin: decaying DR; level raised only by explicit callers (was actors/buffs/Barkskin.java)
---
local RPD  = require "scripts/lib/commonClasses"
local buff = require "scripts/lib/buff"

return buff.init{
    desc  = function ()
        return {
            icon          = 24, -- BuffIndicator.BARKSKIN
            name          = "BarkskinBuff_Name",
            info          = "BarkskinBuff_Info",
        }
    end,

    act = function(self, buff)
        local target = buff.target
        if target:isAlive() then
            buff:spend(1) -- Actor.TICK
            local lvl = buff:level() - 1
            buff:level(lvl)
            if lvl <= 0 then
                buff:detach()
            end
        else
            buff:detach()
        end
    end,

    drBonus = function(self, buff)
        return buff:level()
    end
}
