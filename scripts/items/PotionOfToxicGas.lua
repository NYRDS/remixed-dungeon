---
--- PotionOfToxicGas: a toxic cloud on shatter, poison arrows when moistened
--- (was items/potions/PotionOfToxicGas.java).
---
local RPD    = require "scripts/lib/commonClasses"
local potion = require "scripts/lib/potion"

return potion.init{
    desc = function()
        return {
            labelIndex = 9,
            price      = 40,
        }
    end,
    shatter = function(self, item, cell)
        item:setKnown()
        item:splash(cell)
        RPD.playSound("snd_shatter")
        RPD.placeBlob(RPD.Blobs.ToxicGas, cell, 1000 * item:qualityFactor())
    end,
    moistenArrow = function(self, item, arrow, owner)
        return "PoisonArrow"
    end,
}
