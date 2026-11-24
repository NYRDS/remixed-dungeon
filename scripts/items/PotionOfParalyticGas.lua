---
--- PotionOfParalyticGas: a paralyzing cloud on shatter, paralysis arrows
--- when moistened (was items/potions/PotionOfParalyticGas.java).
---
local RPD    = require "scripts/lib/commonClasses"
local potion = require "scripts/lib/potion"

return potion.init{
    desc = function()
        return {
            labelIndex = 8,
            price      = 40,
        }
    end,
    shatter = function(self, item, cell)
        item:setKnown()
        item:splash(cell)
        RPD.playSound("snd_shatter")
        RPD.placeBlob(RPD.Blobs.ParalyticGas, cell, 1000 * item:qualityFactor())
    end,
    moistenArrow = function(self, item, arrow, owner)
        return "ParalysisArrow"
    end,
}
