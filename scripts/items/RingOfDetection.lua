---
--- RingOfDetection: widens the search radius and sweeps the ground once on
--- equip (was rings/RingOfDetection.java).
---
local ring = require "scripts/lib/ring"

return ring.init{
    desc = function()
        return {
            buff = "Detection",
            price = 80,
        }
    end,
    afterEquip = function(self, item, chr)
        -- was Dungeon.hero.search(false) after a successful equip
        RPD.Dungeon.hero:search(false)
    end,
}
