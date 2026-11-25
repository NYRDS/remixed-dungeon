---
--- RingOfEvasion: defence skill bonus (was rings/RingOfEvasion.java).
---
local ring = require "scripts/lib/ring"

return ring.init{
    desc = function()
        return {
            buff = "Evasion",
            price = 80,
        }
    end,
    defenceSkillBonus = function(self, item, buff, chr)
        return buff:level()
    end,
}
