---
--- Moongrace.Seed: eats into a Charm plus partial skill points (was
--- plants/MoongraceSeed.java). Plants into the Moongrace herb via the java
--- framework (plantKind = "Moongrace").
---
local RPD  = require "scripts/lib/commonClasses"
local seed = require "scripts/lib/seed"

return seed.init{
    desc = function()
        return {
            plantName   = "Moongrace_Name",
            descText    = "Moongrace_Desc",
            image       = 8,
            alchemyKind = "PotionOfMana",
        }
    end,
    onEat = function(self, item, chr)
        RPD.affectBuff(chr, "Charm",
            RPD.CharUtils:charmDurationFactor(chr) * math.random(10, 15))

        -- java: accumulateSkillPoints(Random.Int(0, max((max - cur) / 4, 15)))
        local maxSp = math.max(math.floor((chr:getSkillPointsMax() - chr:getSkillPoints()) / 4), 15)
        chr:accumulateSkillPoints(math.random(0, maxSp - 1))
    end,
}
