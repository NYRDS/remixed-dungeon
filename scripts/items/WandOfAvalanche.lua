---
--- WandOfAvalanche: rockfall in the blast radius, stun chance by distance
--- (was wands/WandOfAvalanche.java). Grid sweep stays java-side.
---
local RPD  = require "scripts/lib/commonClasses"
local wand = require "scripts/lib/wand"

return wand.init{
    desc = function()
        return {
            fx       = "earth",
            hitChars = false,
        }
    end,
    zap = function(self, item, cell, victim)
        RPD.ItemUtils:avalancheZap(item, cell)
    end,
}
