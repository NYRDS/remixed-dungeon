---
--- ManaPotion: drinkable skill-point potion, no identification flow
--- (was drinks/ManaPotion.java; distinct from the data-kind PotionOfMana).
---
local RPD  = require "scripts/lib/commonClasses"
local item = require "scripts/lib/item"

return item.init{
    desc = function()
        return {
            image      = 0,
            imageFile  = "items/drinks.png",
            name       = "ManaPotion_Name",
            info       = "ManaPotion_Info",
            stackable  = true,
            upgradable = false,
            identified = true,
            price      = 100,
        }
    end,

    actions = function(self, item, hero)
        return { RPD.CommonActions.AC_DRINK }
    end,

    execute = function(self, item, hero, action)
        if action == RPD.CommonActions.AC_DRINK then
            item:detach(hero:getBelongings().backpack)
            RPD.GLog:i(RPD.textById("Drink_Message"), {})
            hero:setSkillPoints(hero:getSkillPoints() + math.floor(hero:getSkillPointsMax() / 3))
            hero:doOperate(1)
            RPD.Sfx.SpellSprite:show(hero, RPD.Sfx.SpellSprite.FOOD)
            RPD.playSound("snd_drink")
        end
    end,
}
