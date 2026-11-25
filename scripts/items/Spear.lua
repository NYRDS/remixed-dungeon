---
--- Spear: data weapon kind (was weapon/melee/Spear.java / guts / common).
---
local RPD      = require "scripts/lib/commonClasses"
local itemLib  = require "scripts/lib/item"
local weapon   = require "scripts/lib/weapon"

return weapon.init{
    desc = function()
        return {
            tier      = 2,
            acu       = 1.0,
            dly       = 1.5,
            image     = 29,
            range     = 2,
        }
    end,

    anim = function(self, item)
        return "spear"
    end,
}
