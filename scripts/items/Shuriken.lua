---
--- Shuriken: data missile kind (was weapon/missiles/Shuriken.java).
---
local RPD     = require "scripts/lib/commonClasses"
local serpent = require "scripts/lib/serpent"
local weapon  = require "scripts/lib/weapon"

return weapon.init{
    desc = function()
        return {
            STR       = 13,
            min       = 2,
            max       = 6,
            dly       = 0.5,
            image     = 15,
            randomLo  = 5,
            randomHi  = 15,
            pricePerUnit = 15,
            isFliesStraight = false,
            isFliesFastRotating = true,
        }
    end,

    randomQty = function(self, item, part)
        if part == 0 then return 5 end
        return 15
    end,
}
