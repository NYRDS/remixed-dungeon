---
--- RingOfAccuracy: attack skill bonus (was rings/RingOfAccuracy.java).
---
local ring = require "scripts/lib/ring"

return ring.init{
    desc = function()
        return {
            buff = "Accuracy",
            price = 80,
        }
    end,
    attackSkillBonus = function(self, item, buff, chr)
        return buff:level()
    end,
}
