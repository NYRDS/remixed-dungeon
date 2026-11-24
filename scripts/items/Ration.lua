---
--- Ration: bread and cheese, the standard food (was items/food/Ration.java).
---
local food = require "scripts/lib/food"

return food.init{
    desc = function()
        return {
            image  = 4,   -- ItemSpriteSheet.RATION
            energy = 280, -- Hunger.HUNGRY
            price  = 10,
        }
    end,
    poison = function(self, item, cell)
        return "RottenRation"
    end,
}
