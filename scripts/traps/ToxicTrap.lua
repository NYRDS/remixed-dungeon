local RPD = require "scripts/lib/commonClasses"
local trap = require "scripts/lib/trap"

-- java ToxicTrap: seed a ToxicGas blob sized by depth.
return trap.init(
    function (cell, char, data)
        RPD.placeBlob(RPD.Blobs.ToxicGas, cell, 300 + 20 * RPD.Dungeon.depth)
    end
)
