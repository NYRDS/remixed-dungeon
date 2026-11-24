---
--- ChristmasTurkey: the seasonal feast (was nyrds items/food/ChristmasTurkey.java).
---
local food = require "scripts/lib/food"

return food.init{
    desc = function()
        return {
            image  = 11,  -- items/food.png (items/food/ChristmasTurkey.java)
            energy = 400, -- Hunger.STARVING
            price  = 30,
        }
    end,
}
