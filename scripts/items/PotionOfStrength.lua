---
--- PotionOfStrength: +1 STR for the drinking hero, amok arrows when moistened
--- (was items/potions/PotionOfStrength.java).
---
local RPD    = require "scripts/lib/commonClasses"
local potion = require "scripts/lib/potion"

return potion.init{
    desc = function()
        return {
            labelIndex = 6,
            price      = 100,
        }
    end,
    apply = function(self, item, chr)
        item:setKnown()
        if chr:isHero() then
            chr:STR(chr:STR() + 1)
            chr:showStatus(RPD.CharSprite.POSITIVE, RPD.textById("PotionOfStrength_StaApply"))
            RPD.glogp(RPD.textById("PotionOfStrength_Apply"))
            RPD.Badges:validateStrengthAttained(chr)
        end
    end,
    moistenArrow = function(self, item, arrow, owner)
        return "AmokArrow"
    end,
}
