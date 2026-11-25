---
--- Glaive: data weapon kind (was weapon/melee/Glaive.java / guts / common).
---
local RPD      = require "scripts/lib/commonClasses"
local itemLib  = require "scripts/lib/item"
local weapon   = require "scripts/lib/weapon"

return weapon.init{
    desc = function()
        return {
            tier      = 5,
            acu       = 1.2,
            dly       = 1.4,
            image     = 30,
            range     = 2,
        }
    end,

    anim = function(self, item)
        return "spear"
    end,
}
