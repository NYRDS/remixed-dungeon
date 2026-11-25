---
--- ElvenDagger: data weapon kind (was weapon/melee/ElvenDagger.java / guts / common).
---
local RPD      = require "scripts/lib/commonClasses"
local itemLib  = require "scripts/lib/item"
local weapon   = require "scripts/lib/weapon"

return weapon.init{
    desc = function()
        return {
            tier      = 1,
            acu       = 1.5,
            dly       = 0.7,
            image     = 8,
            STR       = 9,
            imageFile = "items/swords.png",
            visualName = "Dagger",
        }
    end,

    anim = function(self, item)
        return "sword"
    end,
}
