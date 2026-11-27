---
--- Fire: burns everything it touches - chars ignite, heaps burn, level
--- objects burn, flammable terrain turns to embers (was Fire.java; the
--- spread walk stays in the java executor, the burn bodies run here).
---
local RPD  = require "scripts/lib/commonClasses"
local blob = require "scripts/lib/blobbase"

return blob.init{
    desc = function()
        return {
            mode     = "fire",
            tileDesc = "Fire_Info",
        }
    end,

    affect = function(self, gas, cells)
        for i = 1, cells.length do
            RPD.ScriptBlob:burn(cells[i])
        end
    end,
}
