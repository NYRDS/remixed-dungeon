---
--- OverpricedRation: the shopkeeper's ration at shop prices
--- (was items/food/OverpricedRation.java).
---
local food = require "scripts/lib/food"

return food.init{
    desc = function()
        return {
            image   = 115, -- ItemSpriteSheet.OVERPRICED
            energy  = 120, -- Hunger.STARVING - Hunger.HUNGRY
            message = "OverpricedRation_Message",
            price   = 20,
        }
    end,
    poison = function(self, item, cell)
        return "RottenRation"
    end,
}
