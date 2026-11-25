---
--- RingOfShadows: stealth bonus (was rings/RingOfShadows.java).
---
local ring = require "scripts/lib/ring"

return ring.init{
    desc = function()
        return {
            buff = "Shadows",
            price = 80,
        }
    end,
    stealthBonus = function(self, item, buff, chr)
        return buff:level()
    end,
}
