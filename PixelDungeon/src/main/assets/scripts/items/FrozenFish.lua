--
-- User: mike
-- Date: 26.05.2018
-- Time: 21:32
-- This file is part of Remixed Pixel Dungeon.
--

local RPD = require "scripts/lib/commonClasses"

local fish = {}

function fish.desc(tbl, item)
    return {
        image     = 14,
        imageFile = "items/food.png",
        name      = "frozen fish",
        info      = "Frozen fish, looks like it alive, but frozen"
    }
end

function fish.actions()
    return {RPD.Actions.eat}
end

function fish.execute(tbl, item, hero, action)
    if action == RPD.Actions.eat then
        hero:eat(item,RPD.Buffs.Hunger.HUNGRY,"Fishy!")
    end
end

function fish.burn(tbl, item, hero, action)
    return item
end

function fish.freeze(tbl, item, hero, action)
    return item
end

function fish.poison(tbl, item, hero, action)
    return RPD.ItemFactory:itemByName("RottenFish")
end


return fish