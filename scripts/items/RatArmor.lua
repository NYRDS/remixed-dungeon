---
--- RatArmor: data armor kind (was armor/RatArmor.java / common/armor).
---
local RPD    = require "scripts/lib/commonClasses"
local weapon = require "scripts/lib/weapon"

return weapon.init{
    desc = function()
        return {
            tier      = 1,
            image     = 24,
            drBonus   = 3,
        }
    end,
}
