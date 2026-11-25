---
--- WardenArmor: class-armor data kind (was armor/WardenArmor.java). The special action id and
--- equip gate live in the desc; the special itself is the framework flag.
---
local weapon = require "scripts/lib/weapon"

return weapon.init{
    desc = function()
        return {
            tier      = 6,
            image     = 16,
            upgradable = false,
            identified = true,
            price     = 0,
            special   = "HuntressArmor_ACSpecial",
            gateClass = "WARDEN",
            huntressSpecial = true,
            hasHelmet = true,
            coverHair = true,
        }
    end,
}
