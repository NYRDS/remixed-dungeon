---
--- CandyOfDeath: looks like candy, hits like a truck - every executed action
--- hurts the holder (was items/common/debug/CandyOfDeath.java).
--- DIE, DIE, DIE, DIE
---
local RPD  = require "scripts/lib/commonClasses"
local food = require "scripts/lib/food"

return food.init{
    desc = function()
        return {
            image     = 21,
            imageFile = "items/artifacts.png",
            price     = 20,
        }
    end,
    onExecute = function(self, item, chr, action)
        chr:damage(chr:ht(), item)
    end,
}
