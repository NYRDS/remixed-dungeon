--
-- User: mike
-- Date: 26.05.2018
-- Time: 21:32
-- This file is part of Remixed Pixel Dungeon.
--

local RPD = require "scripts/lib/commonClasses"

local item = require "scripts/lib/item"


return item.init{
    desc  = function ()
        return {
            image     = 15,
            imageFile = "items/food.png",
            name      = "RottenFish_Name",
            info      = "RottenFish_Info",
            stackable = true
        }
    end,
    actions = function() return {RPD.Actions.eat} end,
    execute = function(self, item, hero, action)
        if action == RPD.Actions.eat then
            RPD.affectBuff(hero,RPD.Buffs.Poison,2*math.random(1, hero:lvl()))
            hero:eat(item,RPD.Buffs.Hunger.HUNGRY / 4,"RottenFish_Taste")
        end
    end
}
