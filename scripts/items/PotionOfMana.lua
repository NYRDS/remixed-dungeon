---
--- PotionOfMana: restores a third of the drinker's skill points
--- (was items/potions/PotionOfMana.java).
---
local RPD    = require "scripts/lib/commonClasses"
local potion = require "scripts/lib/potion"

return potion.init{
    desc = function()
        return {
            labelIndex = 12,
            price      = 40,
        }
    end,
    apply = function(self, item, chr)
        item:setKnown()
        RPD.glog(RPD.textById("Drink_Message"))
        chr:setSkillPoints(chr:getSkillPoints() + math.floor(chr:getSkillPointsMax() / 3))
        RPD.Sfx.SpellSprite:show(chr, RPD.Sfx.SpellSprite.FOOD)
    end,
}
