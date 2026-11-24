---
--- PumpkinPie: seasonal baked treat (was nyrds items/food/PumpkinPie.java).
---
local food = require "scripts/lib/food"

return food.init{
    desc = function()
        return {
            image  = 9,   -- ItemSpriteSheet.PIE (items/food.png)
            energy = 400, -- Hunger.STARVING
            price  = 30,
        }
    end,
    poison = function(self, item, cell)
        return "RottenPumpkinPie"
    end,
}
