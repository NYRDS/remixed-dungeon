---
--- PotionOfPurity: a moment of freshness - short gas immunity for the
--- drinker; on shatter it clears the gases around the impact cell
--- (was items/potions/PotionOfPurity.java). The grid sweep is the
--- purityShatter java helper.
---
local RPD    = require "scripts/lib/commonClasses"
local potion = require "scripts/lib/potion"

return potion.init{
    desc = function()
        return {
            labelIndex = 4,
            price      = 40,
        }
    end,
    apply = function(self, item, chr)
        RPD.glogw(RPD.textById("PotionOfPurity_NoSmell"))
        RPD.affectBuff(chr, "GasesImmunity", 5 * item:qualityFactor())
    end,
    shatter = function(self, item, cell)
        item:purityShatter(cell)
    end,
}
