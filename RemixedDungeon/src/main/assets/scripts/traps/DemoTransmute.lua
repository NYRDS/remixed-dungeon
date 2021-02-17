--
-- User: mike
-- Date: 18.11.2017
-- Time: 23:27
-- This file is part of Remixed Pixel Dungeon.
--

local RPD = require "scripts/lib/commonClasses"

local trap = require"scripts/lib/trap"

return trap.init(
    function (cell, char, data)

        local recipe = {}
        recipe["RatHide"] = false
        recipe["ClothArmor"] = false

        local result = "RatArmor"

        local heap = RPD.Dungeon.level:getHeap(cell)
        if not heap then
            return
        end

        local items = heap.items
        local totalItems = items:size()

        local elements = {}

        for i = 0, totalItems - 1 do
            local item = items:get(i)
            local itemName = item:getEntityKind()

            if recipe[itemName] == false then
                table.insert(elements, item)
                recipe[itemName] = true
            end
        end

        local haveAllElements = true
        for item, haveIt in pairs(recipe) do
            haveAllElements = haveAllElements and haveIt
        end

        if haveAllElements then
            for i,element in ipairs(elements) do
                items:remove(element)
            end

            local item = RPD.ItemFactory:itemByName(result)
            RPD.Dungeon.level:drop(item,cell)
        else
            local emitter = RPD.Sfx.CellEmitter:get(cell)
            emitter:burst(RPD.Sfx.SnowParticle.FACTORY, 20)
        end

    end
)