---
--- Sorrowmoss.Seed: eats into Poison scaled by eater level plus two turns of
--- Invisibility (was plants/SorrowmossSeed.java). Plants into the Sorrowmoss
--- herb via the java framework (plantKind = "Sorrowmoss").
---
local RPD  = require "scripts/lib/commonClasses"
local seed = require "scripts/lib/seed"

return seed.init{
    desc = function()
        return {
            plantName   = "Sorrowmoss_Name",
            descText    = "Sorrowmoss_Desc",
            image       = 2,
            alchemyKind = "PotionOfToxicGas",
        }
    end,
    onEat = function(self, item, chr)
        -- java: Poison duration = CharUtils.durationFactor(chr) * chr.lvl()
        RPD.affectBuff(chr, "Poison", RPD.CharUtils:durationFactor(chr) * chr:lvl())
        RPD.affectBuff(chr, "Invisibility", 2)
    end,
}
