---
--- RingOfSatiety: slows hunger; consumed kind-side by Hunger via
--- BuffFactory.RING_OF_SATIETY buffLevel (was rings/RingOfSatiety.java).
---
local ring = require "scripts/lib/ring"

return ring.init{
    desc = function()
        return {
            buff = "Satiety",
            price = 80,
        }
    end,
}
