---
--- RottenMeat: meat that went off (was items/food/RottenMeat.java).
---
local rotten = require "scripts/lib/rotten_food"

return rotten.init{
    desc = function()
        return {
            image = 118, -- ItemSpriteSheet.ROTTEN_MEAT
        }
    end,
    purify = function(self, item)
        return "MysteryMeat"
    end,
}
