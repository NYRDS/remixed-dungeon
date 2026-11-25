---
--- RingOfMending: boosts natural regeneration (was rings/RingOfMending.java).
---
local ring = require "scripts/lib/ring"

return ring.init{
    desc = function()
        return {
            buff = "Rejuvenation",
            price = 80,
        }
    end,
    regenerationBonus = function(self, item, buff, chr)
        return buff:level()
    end,
}
