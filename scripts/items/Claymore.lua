---
--- Claymore: data weapon kind (was weapon/melee/Claymore.java / guts / common).
---
local RPD      = require "scripts/lib/commonClasses"
local itemLib  = require "scripts/lib/item"
local weapon   = require "scripts/lib/weapon"

return weapon.init{
    desc = function()
        return {
            tier      = 6,
            acu       = 1.0,
            dly       = 1.0,
            image     = 6,
            twoHanded = true,
            imageFile = "items/swords.png",
        }
    end,

    anim = function(self, item)
        return "heavy"
    end,
}
