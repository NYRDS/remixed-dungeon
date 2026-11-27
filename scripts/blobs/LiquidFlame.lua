---
--- LiquidFlame: the spreading fire - NEIGHBOURS5 spread into flammable
--- terrain plus a diffusion pass, everything touched burns (was
--- LiquidFlame.java).
---
local RPD  = require "scripts/lib/commonClasses"
local blob = require "scripts/lib/blobbase"

return blob.init{
    desc = function()
        return {
            mode     = "liquidFlame",
            tileDesc = "Fire_Info",
        }
    end,

    affect = function(self, gas, cells)
        for i = 1, cells.length do
            RPD.ScriptBlob:burn(cells[i])
        end
    end,
}
