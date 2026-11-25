---
--- PriestArmor: class-armor data kind (was armor/PriestArmor.java). The special action id,
--- equip gate and visual flags live in the desc; the special itself is the
--- framework flag ("leaper"/"teleporter"/...) or a hook below.
---
local RPD    = require "scripts/lib/commonClasses"
local weapon = require "scripts/lib/weapon"

return weapon.init{
    desc = function()
        return {
            tier      = 6,
            image     = 34,
            upgradable = false,
            identified = true,
            price     = 0,
            special   = "PriestArmor_ACSpecial",
            gateClass = "PRIEST",
        }
    end,
    -- doSpecial stays on the framework (priestSpecial bridge)
}
