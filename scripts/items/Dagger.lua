---
--- Dagger: data weapon kind (was weapon/melee/Dagger.java / guts / common).
---
local RPD      = require "scripts/lib/commonClasses"
local itemLib  = require "scripts/lib/item"
local weapon   = require "scripts/lib/weapon"

return weapon.init{
    desc = function()
        return {
            tier      = 1,
            acu       = 1.2,
            dly       = 1.0,
            image     = 19,
        }
    end,

}
