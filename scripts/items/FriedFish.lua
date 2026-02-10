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
            image         = 13,
            imageFile     = "items/food.png",
            name          = "FriedFish_Name",
            info          = "FriedFish_Info",
            stackable     = true,
            defaultAction = "Food_ACEat",
            price         = 30
        }
    end,
    actions = function() return {RPD.Actions.eat} end,
    execute = function(self, item, hero, action)
        if action == RPD.Actions.eat then
            hero:eat(item,RPD.Buffs.Hunger.STARVING,"FriedFish_Taste")
        end
    end,
    poison = function() return RPD.ItemFactory:itemByName("RottenFish") end
}