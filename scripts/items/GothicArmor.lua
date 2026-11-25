---
--- GothicArmor: data armor kind (was armor/GothicArmor.java / common/armor).
---
local RPD    = require "scripts/lib/commonClasses"
local weapon = require "scripts/lib/weapon"

return weapon.init{
    desc = function()
        return {
            tier      = 6,
            image     = 20,
        }
    end,
}
