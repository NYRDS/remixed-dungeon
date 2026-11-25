---
--- BerserkArmor: class-armor data kind (was armor/BerserkArmor.java). The special action id and
--- equip gate live in the desc; the special itself is the framework flag.
---
local weapon = require "scripts/lib/weapon"

return weapon.init{
    desc = function()
        return {
            tier      = 6,
            image     = 6,
            upgradable = false,
            identified = true,
            price     = 0,
            special   = "WarriorArmor_ACSpecial",
            gateClass = "BERSERKER",
            leaper    = true,
            hasHelmet = true,
            coverHair = true,
            name      = "WarriorArmor_Name",
        }
    end,
}
