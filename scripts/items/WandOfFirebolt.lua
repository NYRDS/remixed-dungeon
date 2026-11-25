---
--- WandOfFirebolt: fire seeds along the beam, the victim ignites and burns
--- (was wands/WandOfFirebolt.java). Grid sweep stays java-side.
---
local RPD  = require "scripts/lib/commonClasses"
local wand = require "scripts/lib/wand"

return wand.init{
    desc = function()
        return {
            fx = "fire",
        }
    end,
    zap = function(self, item, cell, victim)
        RPD.ItemUtils:fireboltZap(item, cell)
    end,
}
