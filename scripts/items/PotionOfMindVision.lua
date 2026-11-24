---
--- PotionOfMindVision: shared mind vision for the drinker
--- (was items/potions/PotionOfMindVision.java).
---
local RPD    = require "scripts/lib/commonClasses"
local potion = require "scripts/lib/potion"

return potion.init{
    desc = function()
        return {
            labelIndex = 10,
            price      = 35,
        }
    end,
    apply = function(self, item, chr)
        item:setKnown()
        RPD.affectBuff(chr, "MindVision", 20 * item:qualityFactor())
        RPD.CharUtils:reportMindVisionEffect()
    end,
}
