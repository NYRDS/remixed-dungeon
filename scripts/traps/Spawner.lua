--
-- User: mike
-- Date: 12.11.2017
-- Time: 22:16
-- This file is part of Remixed Pixel Dungeon.
--

local RPD = require "scripts/lib/commonClasses"

local trap = require"scripts/lib/trap"

return trap.init(
    function (cell, char, data)
        local mobs  = {"Rat","Gnoll"}
        local items = {"Dagger","RatHide"}
        local objects = {"Barrel","Sorrowmoss"}

        local level = RPD.Dungeon.level

        local pos  = level:getEmptyCellNextTo(cell)
        local roll = math.random()

        if roll < 0.33 then
            local item = RPD.item(items[math.random(1,#items)],1)
            item:upgrade()
            level:drop(item,pos)
            return
        end

        if roll < 0.66 then
            local mob = RPD.MobFactory:mobByName(mobs[math.random(1,#mobs)])
            mob:setPos(pos)
            level:spawnMob(mob)
            return
        end

        RPD.levelObject(objects[math.random(1,#objects)],pos)

    end
)

