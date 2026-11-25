---
--- Mace: data weapon kind (was weapon/melee/Mace.java / guts / common).
---
local RPD      = require "scripts/lib/commonClasses"
local itemLib  = require "scripts/lib/item"
local weapon   = require "scripts/lib/weapon"

return weapon.init{
    desc = function()
        return {
            tier      = 3,
            acu       = 1.0,
            dly       = 0.8,
            image     = 18,
            anim      = "sword",
        }
    end,

}
