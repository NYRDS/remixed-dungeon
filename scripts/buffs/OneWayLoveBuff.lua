---
--- OneWayLoveBuff: DriedRose charm-state marker (was
--- quest.DriedRose.OneWayLoveBuff). Charm.lua + CharUtils.charmDurationFactor
--- query this kind.
---
local buff = require "scripts/lib/buff"

return buff.init{
    desc  = function ()
        return {
            icon          = 30, -- BuffIndicator.ROSE
            name          = "DriedRoseBuff_Name",
            info          = "DriedRoseBuff_Info",
        }
    end,
}
