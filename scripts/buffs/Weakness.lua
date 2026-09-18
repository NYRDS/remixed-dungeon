---
--- Weakness: discharges equipped items on attach (was actors/buffs/Weakness.java)
---
local RPD  = require "scripts/lib/commonClasses"
local buff = require "scripts/lib/buff"

return buff.init{
    desc  = function ()
        return {
            icon          = 14, -- BuffIndicator.WEAKNESS
            name          = "WeaknessBuff_Name",
            info          = "WeaknessBuff_Info",
        }
    end,

    attached = function(self, buff)
        buff.target:getBelongings():discharge()
    end
}
