---
--- PotionOfLiquidFlame: sets the cell on fire when shattered, fire arrows
--- when moistened (was items/potions/PotionOfLiquidFlame.java).
---
local RPD    = require "scripts/lib/commonClasses"
local potion = require "scripts/lib/potion"

return potion.init{
    desc = function()
        return {
            labelIndex = 1,
            price      = 40,
        }
    end,
    shatter = function(self, item, cell)
        item:setKnown()
        item:splash(cell)
        RPD.playSound("snd_shatter")
        RPD.placeBlob(RPD.Blobs.LiquidFlame, cell, 10 * item:qualityFactor())
    end,
    moistenArrow = function(self, item, arrow, owner)
        return "FireArrow"
    end,
}
