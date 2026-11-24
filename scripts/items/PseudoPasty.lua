---
--- PseudoPasty: the too-good-to-be-true pasty - a MimicPie trap
--- (was items/food/PseudoPasty.java). It never feeds anyone: picking it up
--- or eating it hatches one mimic per unit instead.
---
local RPD  = require "scripts/lib/commonClasses"
local food = require "scripts/lib/food"

return food.init{
    desc = function()
        return {
            image  = 112, -- ItemSpriteSheet.PASTY (identical sprite on purpose)
            energy = 400, -- Hunger.STARVING; authored data only - the eat hook never applies it
        }
    end,
    pick = function(self, item, ch, pos)
        return RPD.CharUtils:tryToSpawnMimic(item, ch, pos, "MimicPie")
    end,
    eat = function(self, item, chr)
        local pos = chr:level():getEmptyCellNextTo(chr:getPos())
        item:pick(chr, pos)
        item:removeItemFrom(chr)
    end,
}
