---
--- GnollArmor: class-armor data kind (was armor/GnollArmor.java). The special action id and
--- equip gate live in the desc; the special itself is the framework flag.
---
local weapon = require "scripts/lib/weapon"

return weapon.init{
    desc = function()
        return {
            tier      = 6,
            image     = 25,
            upgradable = false,
            identified = true,
            price     = 0,
            special   = "GnollArmor_ACSpecial",
            gateClass = "GNOLL",
            gnollSpecial = true,
        }
    end,
}
