---
--- RogueArmor: class-armor data kind (was armor/RogueArmor.java). The special action id and
--- equip gate live in the desc; the special itself is the framework flag.
---
local weapon = require "scripts/lib/weapon"

return weapon.init{
    desc = function()
        return {
            tier      = 6,
            image     = 8,
            upgradable = false,
            identified = true,
            price     = 0,
            special   = "RogueArmor_ACSpecial",
            gateClass = "ROGUE",
            teleporter = true,
        }
    end,
}
