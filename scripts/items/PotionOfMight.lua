---
--- PotionOfMight: +1 STR, +5 max hp and a small heal for the drinking hero
--- (was items/potions/PotionOfMight.java).
---
local RPD    = require "scripts/lib/commonClasses"
local potion = require "scripts/lib/potion"

return potion.init{
    desc = function()
        return {
            price = 200,
        }
    end,
    apply = function(self, item, chr)
        item:setKnown()
        if chr:getEntityKind() == "Hero" then
            chr:STR(chr:STR() + 1)
            chr:ht(chr:ht() + 5)
            chr:heal(5, item)
            chr:showStatus(RPD.CharSprite.POSITIVE, RPD.textById("PotionOfMight_StaApply"))
            RPD.glogp(RPD.textById("PotionOfMight_Apply"))
            RPD.Badges:validateStrengthAttained(chr)
        end
    end,
}
