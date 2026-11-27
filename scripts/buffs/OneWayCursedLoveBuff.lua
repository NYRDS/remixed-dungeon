---
--- OneWayCursedLoveBuff: cursed DriedRose charm-state marker (was
--- quest.DriedRose.OneWayCursedLoveBuff).
---
local buff = require "scripts/lib/buff"

return buff.init{
    desc  = function ()
        return {
            icon          = 31, -- BuffIndicator.CURSED_ROSE
            name          = "DriedRoseCursedBuff_Name",
            info          = "DriedRoseCursedBuff_Info",
        }
    end,
}
