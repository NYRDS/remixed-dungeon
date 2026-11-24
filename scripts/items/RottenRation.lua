---
--- RottenRation: a ration that went off (was items/food/RottenRation.java).
---
local rotten = require "scripts/lib/rotten_food"

return rotten.init{
    desc = function()
        return {
            image = 119, -- ItemSpriteSheet.ROTTEN_RATION
        }
    end,
    purify = function(self, item)
        return "Ration"
    end,
}
