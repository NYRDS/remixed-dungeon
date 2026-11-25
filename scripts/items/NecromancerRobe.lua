---
--- NecromancerRobe: data armor kind (was armor/NecromancerRobe.java / common/armor).
---
local RPD    = require "scripts/lib/commonClasses"
local weapon = require "scripts/lib/weapon"

return weapon.init{
    desc = function()
        return {
            tier      = 1,
            image     = 23,
            gateClass = "NECROMANCER",
        }
    end,
    charDied = function(self, item, victim, cause)
        -- every death near the robe grants the wearer a skill point
        item:getOwner():accumulateSkillPoints(1)
    end,
}
