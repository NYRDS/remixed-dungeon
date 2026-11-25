---
--- WandOfRegrowth: grass along the beam and a Regrowth blob at the target
--- (was wands/WandOfRegrowth.java). Terrain sweep stays java-side.
---
local RPD  = require "scripts/lib/commonClasses"
local wand = require "scripts/lib/wand"

return wand.init{
    desc = function()
        return {
            fx = "foliage",
        }
    end,
    zap = function(self, item, cell, victim)
        RPD.ItemUtils:regrowthZap(item, cell)
    end,
}
