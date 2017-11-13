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
        local level = RPD.Dungeon.level

        if math.random() > 0.5 then
            local item = RPD.ItemFactory:itemByName(items[math.random(1,#items)])
            item:upgrade()
            level:drop(item,cell)
        else
            local mob = RPD.MobFactory:mobByName(mobs[math.random(1,#mobs)])
            local mobPos = level:getEmptyCellNextTo(cell)
            if level:cellValid(mobPos) then
                mob:setPos(mobPos)
                level:spawnMob(mob)
            end
        end
    end
)

