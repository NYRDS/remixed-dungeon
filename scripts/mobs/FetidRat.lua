local RPD = require "scripts/lib/commonClasses"
local mob = require "scripts/lib/mob"

-- java Ghost$FetidRat (batch 12): seeds a ParalyticGas cloud under itself
-- whenever it is hit.
return mob.init{
    defenceProc = function(self, enemy, dmg)
        RPD.placeBlob(RPD.Blobs.ParalyticGas, self:getPos(), 20)
        return dmg
    end
}
