---
--- ScrollHolder: data bag kind (was bags/ScrollHolder.java). Containment stays in the Bag
--- framework; the kind string is the bag() hook target.
---
local weapon = require "scripts/lib/weapon"

return weapon.init{
    desc = function()
        return {
            image     = 5,
        }
    end,
}
