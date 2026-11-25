---
--- GladiatorArmor: class-armor data kind (was armor/GladiatorArmor.java). The special action id and
--- equip gate live in the desc; the special itself is the framework flag.
---
local weapon = require "scripts/lib/weapon"

return weapon.init{
    desc = function()
        return {
            tier      = 6,
            image     = 7,
            upgradable = false,
            identified = true,
            price     = 0,
            special   = "WarriorArmor_ACSpecial",
            gateClass = "GLADIATOR",
            leaper    = true,
            hasHelmet = true,
            coverHair = true,
            name      = "WarriorArmor_Name",
        }
    end,
}
