---
--- BattleMageArmor: class-armor data kind (was armor/BattleMageArmor.java). The special action id and
--- equip gate live in the desc; the special itself is the framework flag.
---
local weapon = require "scripts/lib/weapon"

return weapon.init{
    desc = function()
        return {
            tier      = 6,
            image     = 12,
            upgradable = false,
            identified = true,
            price     = 0,
            special   = "MageArmor_ACSpecial",
            gateClass = "BATTLEMAGE",
            mageSpecial = true,
            hasCollar = true,
        }
    end,
}
