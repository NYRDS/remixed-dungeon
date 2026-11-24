---
--- RottenPasty: a pasty that went off (was items/food/RottenPasty.java).
---
local rotten = require "scripts/lib/rotten_food"

return rotten.init{
    desc = function()
        return {
            image = 117, -- ItemSpriteSheet.ROTTEN_PASTY
        }
    end,
    purify = function(self, item)
        return "Pasty"
    end,
}
