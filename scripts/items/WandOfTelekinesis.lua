---
--- WandOfTelekinesis: the force beam pushes chars, grabs heaps, opens chests
--- and discharges traps (was wands/WandOfTelekinesis.java). Beam sweep stays
--- java-side.
---
local RPD  = require "scripts/lib/commonClasses"
local wand = require "scripts/lib/wand"

return wand.init{
    desc = function()
        return {
            fx         = "force",
            hitChars   = false,
            hitObjects = true,
        }
    end,
    zap = function(self, item, cell, victim)
        RPD.ItemUtils:telekinesisZap(item, cell, victim)
    end,
}
