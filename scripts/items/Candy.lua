---
--- Candy: a sweet treat with its own little eat ritual - no hunger in it
--- (was nyrds items/food/Candy.java).
---
local RPD  = require "scripts/lib/commonClasses"
local food = require "scripts/lib/food"

return food.init{
    desc = function()
        return {
            image     = 21,
            imageFile = "items/artifacts.png",
            price     = 20,
        }
    end,
    eat = function(self, item, chr)
        item:detach(chr:getBelongings():backpack())
        RPD.GLog:w(RPD.textById("Candy_Warning_1"), {})
        chr:doOperate(3) -- Food.TIME_TO_EAT
        RPD.Sfx.SpellSprite:show(chr, RPD.Sfx.SpellSprite.FOOD)
        RPD.playSound("snd_eat")
    end,
}
