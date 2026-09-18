---
--- Dreamweed: confusion gas on the pressed cell (was plants/Dreamweed.java)
---
local RPD = require "scripts/lib/commonClasses"
local plant = require "scripts/lib/plant"

return plant.init{
    effect = function(self, plantObject, pos, presser, activator)
        RPD.placeBlob(RPD.Blobs.ConfusionGas, pos, 300 + 20 * RPD.Dungeon.depth)
    end
}
