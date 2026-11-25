---
--- Knuckles: data weapon kind (was weapon/melee/Knuckles.java / guts / common).
---
local RPD      = require "scripts/lib/commonClasses"
local itemLib  = require "scripts/lib/item"
local weapon   = require "scripts/lib/weapon"

return weapon.init{
    desc = function()
        return {
            tier      = 1,
            acu       = 1.0,
            dly       = 0.5,
            image     = 16,
        }
    end,

}
