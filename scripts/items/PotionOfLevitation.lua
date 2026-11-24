---
--- PotionOfLevitation: floats the drinker; moistening blows arrows/scrolls/
--- rotten food away (was items/potions/PotionOfLevitation.java).
---
local RPD    = require "scripts/lib/commonClasses"
local potion = require "scripts/lib/potion"

return potion.init{
    desc = function()
        return {
            labelIndex = 2,
            price      = 35,
        }
    end,
    apply = function(self, item, chr)
        item:setKnown()
        RPD.affectBuff(chr, "Levitation", 20 * item:qualityFactor())
        RPD.glog(RPD.textById("PotionOfLevitation_Apply"))
    end,
    moistenArrow = function(self, item, arrow, owner)
        item:detachMoistenItems(arrow, math.floor(10 * item:qualityFactor()))
        RPD.glog(RPD.textById("Potion_ItemFliesAway"), arrow:name())
        item:moistenEffective(owner)
    end,
    moistenScroll = function(self, item, scroll, owner)
        item:detachMoistenItems(scroll, math.floor(3 * item:qualityFactor()))
        RPD.glog(RPD.textById("Potion_ItemFliesAway"), scroll:name())
        item:moistenEffective(owner)
    end,
    moistenRottenFood = function(self, item, food, owner)
        item:detachMoistenItems(food, math.floor(1 * item:qualityFactor()))
        RPD.glog(RPD.textById("Potion_ItemFliesAway"), food:name())
        item:moistenEffective(owner)
    end,
}
