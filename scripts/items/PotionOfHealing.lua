---
--- PotionOfHealing: heals a third of max hp at half effect when shattered,
--- cures poison/cripple/weakness/bleeding (was items/potions/PotionOfHealing.java).
--- The java-side Potion.heal(ch, portion) static keeps the same effects for
--- ChaosStaff-style callers.
---
local RPD    = require "scripts/lib/commonClasses"
local potion = require "scripts/lib/potion"

return potion.init{
    desc = function()
        return {
            labelIndex = 5,
            price      = 30,
        }
    end,
    apply = function(self, item, chr)
        item:setKnown()
        potion.heal(self, item, chr, 1)
    end,
    shatter = function(self, item, cell)
        local ch = potion.splashIdentify(self, item, cell)
        if ch ~= nil then
            potion.heal(self, item, ch, 0.5)
        end
    end,
}
