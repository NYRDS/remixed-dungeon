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
            image     = 14,
            imageFile = "items/food.png",
            name      = "frozen fish",
            info      = "Frozen fish, looks like it alive, but frozen",
            stackable = true
        }
    end,
    onThrow = function(self, item, cell)
        local level = RPD.Dungeon.level

        if level.water[cell] then
            local mob = RPD.MobFactory:mobByName("Piranha")
            mob:setPos(cell)
            level:spawnMob(mob)
        else
            item:dropTo(cell)
        end
    end
}
