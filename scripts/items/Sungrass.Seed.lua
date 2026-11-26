---
--- Sungrass.Seed: eats into a Charm plus a partial heal (was
--- plants/SungrassSeed.java). Plants into the Sungrass herb via the java
--- framework (plantKind = "Sungrass").
---
local RPD  = require "scripts/lib/commonClasses"
local seed = require "scripts/lib/seed"

return seed.init{
    desc = function()
        return {
            plantName   = "Sungrass_Name",
            descText    = "Sungrass_Desc",
            image       = 4,
            alchemyKind = "PotionOfHealing",
        }
    end,
    onEat = function(self, item, chr)
        RPD.affectBuff(chr, "Charm",
            RPD.CharUtils:charmDurationFactor(chr) * math.random(10, 15))

        -- java: heal(Random.Int(0, max((ht - hp) / 4, 15)), this), Random.Int(0,b) = [0,b)
        local maxHeal = math.max(math.floor((chr:ht() - chr:hp()) / 4), 15)
        chr:heal(math.random(0, maxHeal - 1), item)
    end,
}
