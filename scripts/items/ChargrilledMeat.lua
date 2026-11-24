---
--- ChargrilledMeat: mystery meat, cooked (was items/food/ChargrilledMeat.java).
---
local food = require "scripts/lib/food"

return food.init{
    desc = function()
        return {
            image  = 114, -- ItemSpriteSheet.STEAK
            energy = 120, -- Hunger.STARVING - Hunger.HUNGRY
            price  = 7,
        }
    end,
    poison = function(self, item, cell)
        return "RottenMeat"
    end,
}
