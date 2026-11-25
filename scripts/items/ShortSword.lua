---
--- ShortSword: data weapon kind (was weapon/melee/ShortSword.java / guts / common).
---
local RPD      = require "scripts/lib/commonClasses"
local itemLib  = require "scripts/lib/item"
local weapon   = require "scripts/lib/weapon"

return weapon.init{
    desc = function()
        return {
            tier      = 1,
            acu       = 1.0,
            dly       = 1.0,
            image     = 2,
            STR       = 11,
            min       = 1,
            max       = 12,
            reforger  = true,
        }
    end,

}
