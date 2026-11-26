---
--- Dreamweed.Seed: eats into Vertigo plus one turn of MindVision (was
--- plants/DreamweedSeed.java). Plants into the Dreamweed herb via the java
--- framework (plantKind = "Dreamweed").
---
local RPD  = require "scripts/lib/commonClasses"
local seed = require "scripts/lib/seed"

return seed.init{
    desc = function()
        return {
            plantName   = "Dreamweed_Name",
            descText    = "Dreamweed_Desc",
            image       = 3,
            alchemyKind = "PotionOfInvisibility",
        }
    end,
    onEat = function(self, item, chr)
        -- java: Vertigo 10f * 2 turns, MindVision affect(..., 1)
        RPD.affectBuff(chr, "Vertigo", 10 * 2)
        RPD.affectBuff(chr, "MindVision", 1)
    end,
}
