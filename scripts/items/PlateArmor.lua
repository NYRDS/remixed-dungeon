---
--- PlateArmor: data armor kind (was armor/PlateArmor.java / common/armor).
---
local RPD    = require "scripts/lib/commonClasses"
local weapon = require "scripts/lib/weapon"

return weapon.init{
    desc = function()
        return {
            tier      = 5,
            image     = 4,
        }
    end,
}
