---
--- WandOfFlock: a lifespan-limited flock of sheep around the impact
--- (was wands/WandOfFlock.java). Spawn sweep stays java-side.
---
local RPD  = require "scripts/lib/commonClasses"
local wand = require "scripts/lib/wand"

return wand.init{
    desc = function()
        return {
            fx = "wool",
        }
    end,
    zap = function(self, item, cell, victim)
        RPD.ItemUtils:flockZap(item, cell)
    end,
}
