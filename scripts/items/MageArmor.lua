---
--- MageArmor: class-armor data kind (was armor/MageArmor.java). The special action id and
--- equip gate live in the desc; the special itself is the framework flag.
---
local weapon = require "scripts/lib/weapon"

return weapon.init{
    desc = function()
        return {
            tier      = 6,
            image     = 11,
            upgradable = false,
            identified = true,
            price     = 0,
            special   = "MageArmor_ACSpecial",
            gateClass = "MAGE",
            mageSpecial = true,
        }
    end,
}
