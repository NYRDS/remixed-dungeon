---
--- ClothArmor: data armor kind (was armor/ClothArmor.java / common/armor).
---
local RPD    = require "scripts/lib/commonClasses"
local weapon = require "scripts/lib/weapon"

return weapon.init{
    desc = function()
        return {
            tier      = 1,
            image     = 0,
            noAsh     = true,
        }
    end,
}
