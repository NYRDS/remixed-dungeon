---
--- RingOfHaggler: never upgradable, spawns at level +1, identifies itself on
--- pickup with its badge; haggling consumed kind-side via
--- BuffFactory.RING_OF_HAGGLING (was rings/RingOfHaggler.java).
---
local RPD  = require "scripts/lib/commonClasses"
local ring = require "scripts/lib/ring"

return ring.init{
    desc = function()
        return {
            buff       = "Haggling",
            price      = 80,
            upgradable = false,
        }
    end,
    random = function(self, item)
        -- was level(+1): set the level to 1, exactly as the java did
        item:level(1)
        return true
    end,
    doPickUp = function(self, item, chr)
        item:identify()
        RPD.Badges:validateRingOfHaggler()
        RPD.Badges:validateItemLevelAcquired(item)
        return nil -- fall through to the default collect, as the java did
    end,
}
