local RPD = require "scripts/lib/commonClasses"
local mob = require "scripts/lib/mob"

return mob.init{
    attackProc = function(self, enemy, dmg)
        -- 1/4 chance to chill: java did Freezing.affect on the victim cell -
        -- Frost (with duration), cell-side fire out, heap under the victim
        -- freezes. The Frost itself still shatters to the same hit's damage.
        if enemy ~= nil and math.random(4) == 1 then
            RPD.PseudoBlobs.Freezing:affect(enemy:getPos())
        end
        return dmg
    end
}
