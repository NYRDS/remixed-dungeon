local RPD = require "scripts/lib/commonClasses"
local trap = require "scripts/lib/trap"

-- java FireTrap: small Fire seed + flame burst.
return trap.init(
    function (cell, char, data)
        RPD.placeBlob(RPD.Blobs.Fire, cell, 2)
        RPD.Sfx.CellEmitter:get(cell):burst(RPD.Sfx.FlameParticle.FACTORY, 5)
    end
)
