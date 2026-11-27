---
--- RatterAura: the RatSkull marker aura (was quest.RatSkull.RatterAura).
--- Pure marker - the ratter flee policy lives in the rat AI scripts, which
--- query the buff by kind.
---
local buff = require "scripts/lib/buff"

return buff.init{
    desc  = function ()
        return {
            icon          = 33, -- BuffIndicator.RAT_SKULL
            name          = "RatSkullBuff_Name",
            info          = "RatSkullBuff_Info",
        }
    end,
}
