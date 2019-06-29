--
-- User: mike
-- Date: 29.01.2019
-- Time: 20:33
-- This file is part of Remixed Pixel Dungeon.
--

local RPD = require "scripts/lib/commonClasses"

local item = require "scripts/lib/item"

return item.init{
    desc  = function (self, item)

        RPD.glog("Created item with id:"..tostring(item:getId()))

        return {
            image     = 12,
            imageFile = "items/food.png",
            name      = "Test item",
            info      = "Item for script tests",
            stackable = false,
            defaultAction = "action1",
            price         = 7
        }
    end,
    actions = function(self, item, hero)
        return {"action1",
                "action2",
                "action3",
                tostring(item:getId())} end,

    cellSelected = function(self, thisItem, action, cell)
        if action == "action1" then
            RPD.glogp("performing "..action.."on cell"..tostring(cell).."\n")
            RPD.zapEffect(thisItem:getUser():getPos(), cell, "Lightning")
        end
    end,

    execute = function(self, item, hero, action)
        if action == "action1" then
            item:selectCell("action1","Please select cell for action 1")
        end

        if action == "action2" then
            RPD.glogp(tostring(item:getId()).." "..action)
        end

        if action == "action3" then
            RPD.glogn(tostring(item:getId()).." "..action)
        end
    end,
}
