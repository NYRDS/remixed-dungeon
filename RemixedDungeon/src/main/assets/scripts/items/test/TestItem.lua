--
-- User: mike
-- Date: 29.01.2019
-- Time: 20:33
-- This file is part of Remixed Pixel Dungeon.
--

local RPD = require "scripts/lib/commonClasses"

local item = require "scripts/lib/item"

local candle =
{
    kind="Deco",
    object_desc="candle"
}

return item.init{
    desc  = function (self, item)

        RPD.glog("Created item with id:"..tostring(item:getId()))

        return {
            image         = 12,
            imageFile     = "items/food.png",
            name          = "Test item",
            info          = "Item for script tests",
            stackable     = false,
            defaultAction = "action1",
            price         = 0,
            isArtifact    = true,
            data = {
                activationCount = 0
            }
        }
    end,

    actions = function(self, item, hero)

        for k,v in pairs(self) do
            RPD.glog(tostring(k).."->"..tostring(v))
        end

        if item:isEquipped(hero) then
            return {"eq_action1",
                    "eq_action2",
                    "eq_action3",
                    tostring(item:getId()),
                    tostring(self.data.activationCount),
                    tostring(self)
                    }
        else
            return {"action1",
                    "action2",
                    "action3",
                    tostring(item:getId()),
                    tostring(self.data.activationCount),
                    tostring(self)
            }
        end
    end,

    cellSelected = function(self, thisItem, action, cell)
        if action == "action1" then
            RPD.glogp("performing "..action.."on cell"..tostring(cell).."\n")
            RPD.zapEffect(thisItem:getUser():getPos(), cell, "Lightning")
            local book = RPD.creteItem("Codex", {text="Test codex"})
            RPD.Dungeon.level:drop(book, cell)
            --RPD.createLevelObject(candle, cell)
        end
    end,

    execute = function(self, item, hero, action)
        if action == "action1" then
            item:selectCell("action1","Please select cell for action 1")
        end

        if action == "action2" then
            self.data.activationCount = self.data.activationCount + 1
            RPD.glogp(tostring(item:getId()).." "..action)
        end

        if action == "action3" then
            RPD.glogn(tostring(item:getId()).." "..action)
        end
    end,

    activate = function(self, item, hero)

        local Buff = RPD.affectBuff(hero,"TestBuff", 10)
        Buff:level(3)
        Buff:setSource(item)
    end,

    deactivate = function(self, item, hero)
        RPD.removeBuff(hero,"TestBuff")
    end,

--[[
    bag = function(self, item)
        return "SeedPouch"
    end
 ]]
}
