--
-- User: mike
-- Date: 28.11.2017
-- Time: 22:20
-- This file is part of Remixed Pixel Dungeon.
--

local RPD = require "scripts/lib/commonClasses"

local mob = require"scripts/lib/mob"

return mob.init({
    stats = function(self)
        self:immunities():add(RPD.Buffs.Roots)
    end,
    move = function(self, cell)
        RPD.placeBlob( RPD.Blobs.Regrowth, self:getPos(), 10 );
    end
})



