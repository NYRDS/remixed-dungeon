---
--- Pasty: a hearty baked pasty - the generous food drop
--- (was items/food/Pasty.java).
---
local food = require "scripts/lib/food"

return food.init{
    desc = function()
        return {
            image  = 112, -- ItemSpriteSheet.PASTY
            energy = 400, -- Hunger.STARVING
            price  = 25,
        }
    end,
    poison = function(self, item, cell)
        return "RottenPasty"
    end,
}
