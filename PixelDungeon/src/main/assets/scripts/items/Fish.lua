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
        image     = 12,
        imageFile = "items/food.png",
        name      = "fish",
        info      = "Raw fish, eat at your own risk"
    }
end

function fish.actions()
    return {RPD.Actions.eat}
end

function fish.execute(tbl, item, hero, action)
    if action == RPD.Actions.eat then
        item:detach(hero.belongings.backpack)
        hero:eat(item,RPD.Buffs.Hunger.HUNGRY,"Fishy!")
    end
end

return fish