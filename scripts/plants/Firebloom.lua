---
--- Firebloom: ignites the pressed cell (was plants/Firebloom.java)
---
local RPD = require "scripts/lib/commonClasses"
local plant = require "scripts/lib/plant"

return plant.init{
    effect = function(self, plantObject, pos, presser, activator)
        RPD.placeBlob(RPD.Blobs.Fire, pos, 2)

        if RPD.Dungeon:isCellVisible(pos) then
            RPD.Sfx.CellEmitter:get(pos):burst(RPD.Sfx.FlameParticle.FACTORY, 5)
        end
    end
}
