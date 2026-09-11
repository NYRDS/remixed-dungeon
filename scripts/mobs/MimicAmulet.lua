local RPD = require "scripts/lib/commonClasses"
local mob = require "scripts/lib/mob"

return mob.init{
    -- floats constantly
    stats = function(self)
        RPD.permanentBuff(self, RPD.Buffs.Levitation)
    end
}
