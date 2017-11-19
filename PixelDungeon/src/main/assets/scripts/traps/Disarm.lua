--
-- User: mike
-- Date: 20.11.2017
-- Time: 0:11
-- This file is part of Remixed Pixel Dungeon.
--

local RPD = require "scripts/lib/commonClasses"

local trap = require"scripts/lib/trap"

return trap.init(
    function (cell, char, data)
        local hero = RPD.Dungeon.hero
        local belongings = RPD.Dungeon.hero.belongings
        local level = RPD.Dungeon.level

        local items = {"weapon","armor","ring1","ring2" }

        for _,slot in pairs(items) do

           local item = belongings[slot]

           if item then
               item:removeItemFrom(hero)
               local cellToDrop = level:getEmptyCellNextTo(cell)
               if not level:cellValid(cellToDrop) then
                   cellToDrop = cell
               end

               level:drop(item,cellToDrop).sprite:drop(cell)
           end
        end
    end
)

