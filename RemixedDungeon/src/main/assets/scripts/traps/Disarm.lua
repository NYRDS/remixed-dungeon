--
-- User: mike
-- Date: 20.11.2017
-- Time: 0:11
-- This file is part of Remixed Pixel Dungeon.
--

local RPD = require "scripts/lib/commonClasses"

local trap = require "scripts/lib/trap"

return trap.init(function(cell, char, data)
    local hero = RPD.Dungeon.hero
    local belongings = hero:getBelongings()
    local level = RPD.Dungeon.level

    local notRemoveClass = data or " "

    local items = { "weapon", "armor", "ring1", "ring2" }

    local function removeItemFromHero(item)
        if item and not ( string.len(notRemoveClass) > 0 and string.match(tostring(item:getClass()), notRemoveClass) ) then
            item:removeItemFrom(hero)
            local cellToDrop = level:getEmptyCellNextTo(cell)

            if not level:cellValid(cellToDrop) then
                cellToDrop = cell
            end

            level:drop(item, cellToDrop).sprite:drop(cell)
        end
    end

    for _, slot in pairs(items) do
        local item = belongings[slot]
        removeItemFromHero(item)
    end

    local itemsToRemove = {}

    for i = 0, belongings.backpack.items:size()-1 do
       table.insert(itemsToRemove,belongings.backpack.items:get(i))
    end

    for _,item in pairs(itemsToRemove) do
        removeItemFromHero(item)
    end
end)

