---
--- RingOfHerbalism: dew/seed trample bonus; consumed kind-side by HighGrass
--- via BuffFactory.RING_OF_HERBALISM (was rings/RingOfHerbalism.java).
---
local ring = require "scripts/lib/ring"

return ring.init{
    desc = function()
        return {
            buff = "Herbalism",
            price = 80,
        }
    end,
}
