---
--- Dart: data dart kind (was weapon/missiles/Dart.java).
---
local RPD     = require "scripts/lib/commonClasses"
local weapon  = require "scripts/lib/weapon"

return weapon.init{
    desc = function()
        return {
            image     = 1,
            STR       = 10,
            min       = 1,
            max       = 4,
            pricePerUnit = 2,
            randomLo  = 5,
            randomHi  = 15,
        }
    end,
}
