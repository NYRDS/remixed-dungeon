---
--- CommonArrow: data arrow kind (was weapon/missiles/CommonArrow.java). Bow coupling
--- (firedFrom stat scaling, bow enchant relay) stays java-side; the kind
--- supplies base stats + the special proc.
---
local RPD     = require "scripts/lib/commonClasses"
local weapon  = require "scripts/lib/weapon"

return weapon.init{
    desc = function()
        return {
            baseMin   = 2,
            baseMax   = 5,
            baseDly   = 0.75,
            image     = 7,
            pricePerUnit = 3,
            randomLo  = 15,
            randomHi  = 30,
        }
    end,
}
