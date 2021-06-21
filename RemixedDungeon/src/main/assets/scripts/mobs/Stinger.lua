--
-- User: Logodum
-- Date: 21.06.2021
-- Time: 2:55
-- This file is part of Remixed Pixel Dungeon.
--

local RPD = require "scripts/lib/commonClasses"
local mob = require "scripts/lib/mob"

return mob.init{
  attackProc = function(self, enemy, dmg)
    if math.random() > 0.25 then
        RPD.affectBuff(enemy, RPD.Buffs.Poison,1)
        return dmg
    else
        return dmg
    end
end
}
