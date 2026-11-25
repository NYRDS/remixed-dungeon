---
--- Halberd: data weapon kind (was weapon/melee/Halberd.java / guts / common).
---
local RPD      = require "scripts/lib/commonClasses"
local itemLib  = require "scripts/lib/item"
local weapon   = require "scripts/lib/weapon"

return weapon.init{
    desc = function()
        return {
            tier      = 6,
            acu       = 1.1,
            dly       = 1.4,
            image     = 2,
            range     = 2,
            imageFile = "items/polearms.png",
        }
    end,

    anim = function(self, item)
        return "staff"
    end,
}
