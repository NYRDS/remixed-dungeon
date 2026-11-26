---
--- Fadeleaf.Seed: eats into a random teleport plus Vertigo (was
--- plants/FadeleafSeed.java). Plants into the Fadeleaf herb via the java
--- framework (plantKind = "Fadeleaf").
---
local RPD  = require "scripts/lib/commonClasses"
local seed = require "scripts/lib/seed"

return seed.init{
    desc = function()
        return {
            plantName   = "Fadeleaf_Name",
            descText    = "Fadeleaf_Desc",
            image       = 6,
            alchemyKind = "PotionOfMindVision",
        }
    end,
    onEat = function(self, item, chr)
        chr:interrupt()
        RPD.CharUtils:teleportRandom(chr)
        chr:spend(1) -- Actor.TICK
        RPD.affectBuff(chr, "Vertigo", 10 * 2)
    end,
}
