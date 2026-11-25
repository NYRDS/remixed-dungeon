---
--- Quarterstaff: data weapon kind (was weapon/melee/Quarterstaff.java / guts / common).
---
local RPD      = require "scripts/lib/commonClasses"
local itemLib  = require "scripts/lib/item"
local weapon   = require "scripts/lib/weapon"

return weapon.init{
    desc = function()
        return {
            tier      = 2,
            acu       = 1.0,
            dly       = 0.8,
            image     = 17,
            twoHanded = true,
        }
    end,

    anim = function(self, item)
        return "staff"
    end,
}
