--
-- User: mike
-- Date: 25.01.2018
-- Time: 0:26
-- This file is part of Remixed Pixel Dungeon.
--

local RPD = require "scripts/lib/commonClasses"

local mob = require"scripts/lib/mob"

local buffs = {
    RPD.Buffs.Invisibility,
    RPD.Buffs.Roots,
    RPD.Buffs.Paralysis,
    RPD.Buffs.Vertigo,
    RPD.Buffs.Invisibility,
    RPD.Buffs.Levitation
}

return mob.init{
    attackProc = function(self, enemy, dmg)
        RPD.affectBuff(enemy, buffs[math.random(1,#buffs)])
        return dmg
    end
}
