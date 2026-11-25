---
--- RingOfHaste: haste level for the speed formula
--- (was rings/RingOfHaste.java).
---
local ring = require "scripts/lib/ring"

return ring.init{
    desc = function()
        return {
            buff = "Haste",
            price = 80,
        }
    end,
    hasteLevel = function(self, item, buff, chr)
        return buff:level()
    end,
}
