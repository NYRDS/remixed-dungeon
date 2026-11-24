---
--- PotionOfInvisibility: hides the drinker; moistening a scroll with it
--- erases the rune into a blank scroll (was items/potions/PotionOfInvisibility.java).
---
local RPD    = require "scripts/lib/commonClasses"
local potion = require "scripts/lib/potion"

return potion.init{
    desc = function()
        return {
            labelIndex = 3,
            price      = 40,
        }
    end,
    apply = function(self, item, chr)
        item:setKnown()
        RPD.affectBuff(chr, "Invisibility", 15 * item:qualityFactor())
        RPD.glog(RPD.textById("PotionOfInvisibility_Apply"))
        RPD.playSound("snd_meld")
    end,
    moistenScroll = function(self, item, scroll, owner)
        item:detachMoistenItems(scroll, math.floor(3 * item:qualityFactor()))
        RPD.glog(RPD.textById("Potion_RuneDissaperaed"), scroll:name())
        item:moistenEffective(owner)
        return "BlankScroll" -- replacement kind; quantity handled by the framework
    end,
}
