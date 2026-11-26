---
--- Icecap.Seed: eats into a long Frost plus a small partial heal (was
--- plants/IcecapSeed.java). Plants into the Icecap herb via the java
--- framework (plantKind = "Icecap").
---
local RPD  = require "scripts/lib/commonClasses"
local seed = require "scripts/lib/seed"

return seed.init{
    desc = function()
        return {
            plantName   = "Icecap_Name",
            descText    = "Icecap_Desc",
            image       = 1,
            alchemyKind = "PotionOfFrost",
        }
    end,
    onEat = function(self, item, chr)
        -- java: Buff.prolong(chr, FROST, CharUtils.durationFactor(chr) * 5f * 2)
        RPD.Buffs.Buff:prolong(chr, "Frost", RPD.CharUtils:durationFactor(chr) * 10)

        -- java: heal(Random.Int(0, max((ht - hp) / 4, 10)), this)
        local maxHeal = math.max(math.floor((chr:ht() - chr:hp()) / 4), 10)
        chr:heal(math.random(0, maxHeal - 1), item)
    end,
}
