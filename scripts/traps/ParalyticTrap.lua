local RPD = require "scripts/lib/commonClasses"
local trap = require "scripts/lib/trap"

-- java ParalyticTrap: seed a ParalyticGas blob sized by depth.
return trap.init(
    function (cell, char, data)
        RPD.placeBlob(RPD.Blobs.ParalyticGas, cell, 80 + 5 * RPD.Dungeon.depth)
    end
)
