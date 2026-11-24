---
--- PotionOfExperience: grants exactly the experience needed for the next
--- level, also when shattered on someone (was items/potions/PotionOfExperience.java).
---
local RPD    = require "scripts/lib/commonClasses"
local potion = require "scripts/lib/potion"

local function grantLevelUp(chr)
    chr:earnExp(chr:expToLevel() - chr:getExpForLevelUp())
end

return potion.init{
    desc = function()
        return {
            labelIndex = 11,
            price      = 80,
        }
    end,
    apply = function(self, item, chr)
        item:setKnown()
        grantLevelUp(chr)
    end,
    shatter = function(self, item, cell)
        RPD.playSound("snd_shatter")
        item:splash(cell)
        local ch = RPD.Actor:findChar(cell)
        if ch ~= nil then
            item:setKnown()
            grantLevelUp(ch)
            return
        end
        RPD.glog(RPD.textById("Potion_Shatter"), item:color())
    end,
}
