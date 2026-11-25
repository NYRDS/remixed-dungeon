---
--- WandOfLightning: chaining arc damage - all of it happens in the fx pass
--- (framework fx kind "lightning" -> ItemUtils.lightningArc); the zap only
--- carries the self-kill report (was wands/WandOfLightning.java).
---
local wand = require "scripts/lib/wand"

return wand.init{
    desc = function()
        return {
            fx = "lightning",
        }
    end,
    zap = function(self, item, cell, victim)
        wand.selfKillReport(self, item, "WandOfLightning_Info1")
    end,
}
