---
--- RottenPumpkinPie: a pie that went off (was nyrds items/food/RottenPumpkinPie.java).
---
local rotten = require "scripts/lib/rotten_food"

return rotten.init{
    desc = function()
        return {
            image = 10, -- items/food.png (items/food/RottenPumpkinPie.java)
        }
    end,
    purify = function(self, item)
        return "PumpkinPie"
    end,
}
