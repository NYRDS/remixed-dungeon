---
--- RingOfPower: wand effective level bonus; consumed kind-side by
--- Wand.effectiveLevel via BuffFactory.RING_OF_POWER (was rings/RingOfPower.java).
---
local ring = require "scripts/lib/ring"

return ring.init{
    desc = function()
        return {
            buff = "Power",
            price = 80,
        }
    end,
}
