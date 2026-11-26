---
--- Earthroot.Seed: eats into Roots plus Barkskin at STR/4 (was
--- plants/EarthrootSeed.java). Plants into the Earthroot herb via the java
--- framework (plantKind = "Earthroot").
---
local RPD  = require "scripts/lib/commonClasses"
local seed = require "scripts/lib/seed"

return seed.init{
    desc = function()
        return {
            plantName   = "Earthroot_Name",
            descText    = "Earthroot_Desc",
            image       = 5,
            alchemyKind = "PotionOfParalyticGas",
        }
    end,
    onEat = function(self, item, chr)
        RPD.affectBuff(chr, "Roots", 25)

        -- java: barkskin.level raised to effectiveSTR()/4 when lower
        local barkskin = RPD.Buffs.Buff:affect(chr, "Barkskin")
        local want = math.floor(chr:effectiveSTR() / 4)
        if barkskin:level() < want then
            barkskin:level(want)
        end
    end,
}
