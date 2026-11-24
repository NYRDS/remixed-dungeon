---
--- PotionOfFrost: freezes everything within 2 cells of the impact, frost
--- arrows when moistened (was items/potions/PotionOfFrost.java). The grid
--- sweep is the frostShatter java helper.
---
local RPD    = require "scripts/lib/commonClasses"
local potion = require "scripts/lib/potion"

return potion.init{
    desc = function()
        return {
            labelIndex = 0,
            price      = 50,
        }
    end,
    shatter = function(self, item, cell)
        item:frostShatter(cell)
    end,
    moistenArrow = function(self, item, arrow, owner)
        return "FrostArrow"
    end,
}
