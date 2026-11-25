---
--- Sword: data weapon kind (was weapon/melee/Sword.java / guts / common).
---
local RPD      = require "scripts/lib/commonClasses"
local itemLib  = require "scripts/lib/item"
local weapon   = require "scripts/lib/weapon"

return weapon.init{
    desc = function()
        return {
            tier      = 3,
            acu       = 1.0,
            dly       = 1.0,
            image     = 20,
        }
    end,

    anim = function(self, item)
        return "sword"
    end,
}
