---
--- Firebloom.Seed: eats into igniting the eater plus Speed (was
--- plants/FirebloomSeed.java). Plants into the Firebloom herb via the java
--- framework (plantKind = "Firebloom").
---
local RPD  = require "scripts/lib/commonClasses"
local seed = require "scripts/lib/seed"

return seed.init{
    desc = function()
        return {
            plantName   = "Firebloom_Name",
            descText    = "Firebloom_Desc",
            image       = 0,
            alchemyKind = "PotionOfLiquidFlame",
        }
    end,
    onEat = function(self, item, chr)
        RPD.CharUtils:ignite(chr)
        RPD.affectBuff(chr, "Speed", 10)
    end,
}
