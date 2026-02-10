--
-- User: mike
-- Date: 23.11.2017
-- Time: 21:04
-- This file is part of Remixed Pixel Dungeon.
--

local RPD = require "scripts/lib/commonClasses"

local mob = require"scripts/lib/mob"

local function damage(chr, dmg)
    print(pcall(
        function(chr,dmg)
            print(chr,dmg)
            chr:damage(dmg,chr)
        end,
        chr,
        dmg
    ))
end

return mob.init({
    damage = function(self,dmg,src)
        print(dmg, src)
        damage(src, dmg)
    end
})


