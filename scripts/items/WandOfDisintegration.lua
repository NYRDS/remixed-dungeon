---
--- WandOfDisintegration: the beam chars through doors/barricades/grass and
--- damages everything along it (was wands/WandOfDisintegration.java). The
--- DeathRay visual + immediate fire live in the framework fx layer.
---
local RPD  = require "scripts/lib/commonClasses"
local wand = require "scripts/lib/wand"

return wand.init{
    desc = function()
        return {
            fx       = "deathray",
            hitChars = false,
        }
    end,
    zap = function(self, item, cell, victim)
        RPD.ItemUtils:disintegrationZap(item, cell)
    end,
}
