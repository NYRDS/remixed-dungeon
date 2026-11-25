---
--- ElfArmor: class-armor data kind (was armor/ElfArmor.java). The special action id and
--- equip gate live in the desc; the special itself is the framework flag.
---
local weapon = require "scripts/lib/weapon"

return weapon.init{
    desc = function()
        return {
            tier      = 6,
            image     = 17,
            upgradable = false,
            identified = true,
            price     = 0,
            special   = "ElfArmor_ACSpecial",
            gateClass = "ELF",
            elfSpecial = true,
            hasHelmet = true,
        }
    end,
}
