--
-- User: mike
-- Date: 29.01.2019
-- Time: 20:33
-- This file is part of Remixed Pixel Dungeon.
--

local RPD = require "scripts/lib/commonClasses"

local itemLib = require "scripts/lib/item"

local candle =
{
    kind="Deco",
    object_desc="candle"
}

local chest =
{
    kind="Deco",
    object_desc="chest_3"
}

local trap =
{
    kind="Trap",
    uses=10,
    trapKind="scriptFile",
    script="scripts/traps/Spawner"
}


return itemLib.init{
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
            heapScale     = 3.;
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
                    "action4",
                    "inputText",
                    "checkText",
                    "runAsCommand",
                    "listPets",
                    tostring(item:getId()),
                    tostring(self.data.activationCount),
                    tostring(self)
            }
        end
    end,

    cellSelected = function(self, thisItem, action, cell)

        local owner = thisItem:getOwner()

        RPD.glog("cellSelected owner: %s", tostring(owner))

        if action == "action1" then

            local function cellAction(cell)
                RPD.placeBlob(RPD.Blobs.ToxicGas,cell, 50)
            end

            --[[
            local tgt = RPD.forEachCellOnRay(owner:getPos(),
                                             cell,
                                             false,
                                             true,
                                             true,
                                             cellAction)
]]
            --RPD.glogp("performing "..action.."on cell"..tostring(cell).."\n")
            --RPD.zapEffect(thisItem:getOwner():getPos(), cell, "Lightning")
            --local book = RPD.creteItem("PotionOfHealing", {text="Test codex"})
            --RPD.Dungeon.level:drop(book, cell)
            --RPD.createLevelObject(trap, cell)
            --RPD.GameScene:particleEffect("BloodSink", cell);
            local object = RPD.Dungeon.level:getTopLevelObject(cell)

            if not object then
                RPD.glog("no object in cell %d", cell)
            else
                RPD.glog("There is a %s in cell %d", object:getEntityKind(), cell)
            end

            end
    end,

    execute = function(self, item, hero, action)

        local owner = item:getOwner()

        RPD.glog("execute owner: %s", tostring(owner))

        if action == "action1" then

            local ads = require("scripts/lib/ads")


            --[[
            if ads.rewardVideoReady() then
                ads.rewardVideoShow(RPD.createItem("Gold",'{"quantity":500}'))
            else
                RPD.glogn("Reward video not ready")
            end
]]

            --ads.interstitialShow()

            --RPD.affectBuff(hero, RPD.Buffs.Invisibility ,200)
            item:selectCell("action1","Please select cell for action 1")
            --RPD.playMusic("surface",true);
        end

        if action == "action2" then
            self.data.activationCount = self.data.activationCount + 1
            RPD.glogp(tostring(item:getId()).." "..action)
            RPD.affectBuff(hero,"Counter",1):level(10)
        end

        if action == "action3" then
            RPD.glogn(tostring(item:getId()).." "..action)
            item:detach(hero:getBelongings().backpack)
        end

        if action == "action4" then
            local packedItem = RPD.packEntity(item)
            RPD.glog(packedItem)
            local restoredItem = RPD.unpackEntity(packedItem)
            local luaDesc = RPD.toLua(restoredItem)
            restoredItem = RPD.fromLua(luaDesc)
            packedItem = RPD.packEntity(restoredItem)
            RPD.glog(packedItem)
        end

        if action == "inputText" then
            --RPD.System.Input:showInputDialog("Text title", "Text subtitle")
        end

        if action == "checkText" then
            local userText = RPD.System.Input:getInputString()
            RPD.glog(userText)
        end

        if action == "runAsCommand" then
            local userText = RPD.System.Input:getInputString()
            local res, ret = pcall(load(userText, nil,nil, RPD))
            if not res then
                RPD.glogn(ret)
            end
        end

        if action == "listPets" then
            local pets = hero:getPets_l()
            for i,v in ipairs(pets) do
                RPD.glog(v:getEntityKind())
            end
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

    act = function(self,item)
        self.data.counter = (self.data.counter or 0) + 1

        if item:getOwner():valid() then
            item:getOwner():getSprite():showStatus( 0xFF00FF, tostring(self.data.counter))
        end

        item:spend(1)
    end,

    glowing = function(self, item)
        if self.data.activationCount >= 1 then
            return itemLib.makeGlowing(0xFF7A792B, 1)
        end
        return nil
    end
--[[
    bag = function(self, item)
        return "SeedPouch"
    end
 ]]
}
