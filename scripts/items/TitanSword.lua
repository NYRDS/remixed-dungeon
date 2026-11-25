---
--- TitanSword: data weapon kind (was weapon/melee/TitanSword.java / guts / common).
---
local RPD      = require "scripts/lib/commonClasses"
local itemLib  = require "scripts/lib/item"
local weapon   = require "scripts/lib/weapon"

return weapon.init{
    desc = function()
        return {
            tier      = 20,
            acu       = 1.5,
            dly       = 4.0,
            image     = 6,
            upgradeNoop = true,
            imageFile = "items/swords.png",
        }
    end,

}
