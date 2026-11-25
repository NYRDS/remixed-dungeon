---
--- LeatherArmor: data armor kind (was armor/LeatherArmor.java / common/armor).
---
local RPD    = require "scripts/lib/commonClasses"
local weapon = require "scripts/lib/weapon"

return weapon.init{
    desc = function()
        return {
            tier      = 2,
            image     = 1,
        }
    end,
}
