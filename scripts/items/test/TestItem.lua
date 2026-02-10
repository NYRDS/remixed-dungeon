--
-- User: mike
-- Date: 29.01.2019
-- Time: 20:33
-- This file is part of Remixed Pixel Dungeon.
--

local RPD = require "scripts/lib/commonClasses"
local storage = require "scripts/lib/storage"
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


-- Function to run bitmap data tests
local function runBitmapDataTests()
    -- Full test - test all bitmap data interface methods
    local success, errorMsg = pcall(function()
        RPD.glog("Starting BitmapData interface tests...")
        
        -- Test 1: Create bitmap using constructor
        RPD.glog("Test 1: Creating BitmapData using constructor...")
        local bitmap = RPD.new("com.nyrds.platform.gfx.BitmapData", 100, 100)
        
        if not bitmap then
            RPD.glogn("FAILED: Could not create bitmap")
            return
        end
        
        RPD.glog("SUCCESS: Created bitmap with size " .. bitmap:getWidth() .. "x" .. bitmap:getHeight())
        
        -- Test 2: Test basic drawing methods
        RPD.glog("Test 2: Testing basic drawing methods...")
        
        -- Test clear method - fill entire bitmap with blue
        bitmap:clear(0xFF0000FF)  -- Blue color
        RPD.glog("SUCCESS: clear() method works")
        
        -- Test fillRect method with a simple small rectangle
        bitmap:fillRect(0, 0, 50, 50, 0xFFFF0000)  -- Red color
        RPD.glog("SUCCESS: fillRect() method works")
        
        -- Test fillCircle method with a small circle
        bitmap:fillCircle(50, 50, 20, 0xFF00FF00)  -- Green color
        RPD.glog("SUCCESS: fillCircle() method works")
        
        -- Test drawing a simple line
        bitmap:drawLine(0, 0, 50, 50, 0xFFFFFFFF)  -- White line
        RPD.glog("SUCCESS: drawLine() method works")
        
        -- Test 3: Test copyRect method
        RPD.glog("Test 3: Testing copyRect method...")
        local bitmap2 = RPD.new("com.nyrds.platform.gfx.BitmapData", 100, 100)
        if bitmap2 then
            -- Fill second bitmap with yellow background
            bitmap2:clear(0xFFFFFF00)  -- Yellow color
            
            -- Test copyRect - copy a region from bitmap to bitmap2
            bitmap2:copyRect(bitmap, 25, 25, 50, 50, 25, 25)
            
            -- Save the second bitmap to see the copied region
            bitmap2:savePng("test_bitmap_copy_output.png")
            RPD.glog("Saved copy output to: test_bitmap_copy_output.png")
            
            -- Clean up second bitmap
            bitmap2:dispose()
            RPD.glog("SUCCESS: copyRect() method works")
        else
            RPD.glogn("FAILED: Could not create second bitmap for copyRect test")
        end
        
        -- Test 4: Test rectCopy method with alpha blending
        RPD.glog("Test 4: Testing rectCopy method with alpha blending...")
        local bitmap3 = RPD.new("com.nyrds.platform.gfx.BitmapData", 100, 100)
        if bitmap3 then
            -- Fill third bitmap with purple background
            bitmap3:clear(0xFF800080)  -- Purple color
            
            -- Create a semi-transparent bitmap for testing alpha blending
            local alphaBitmap = RPD.new("com.nyrds.platform.gfx.BitmapData", 50, 50)
            if alphaBitmap then
                -- Fill with semi-transparent red
                alphaBitmap:clear(0x80FF0000)  -- 50% transparent red
                
                -- Test rectCopy - copy with alpha blending
                bitmap3:rectCopy(alphaBitmap, 0, 0, 50, 50, 25, 25)
                
                -- Save the third bitmap to see the alpha-blended result
                bitmap3:savePng("test_bitmap_rectcopy_output.png")
                RPD.glog("Saved rectcopy output to: test_bitmap_rectcopy_output.png")
                
                -- Clean up alpha bitmap
                alphaBitmap:dispose()
                RPD.glog("SUCCESS: rectCopy() method with alpha blending works")
            else
                RPD.glogn("FAILED: Could not create alpha bitmap for rectCopy test")
            end
            
            -- Clean up third bitmap
            bitmap3:dispose()
        else
            RPD.glogn("FAILED: Could not create third bitmap for rectCopy test")
        end
        
        -- Test 5: Test getPixel method
        RPD.glog("Test 5: Testing getPixel method...")
        local pixel = bitmap:getPixel(10, 10)
        RPD.glog("Pixel at (10,10): " .. string.format("0x%08X", pixel))
        RPD.glog("SUCCESS: getPixel() method works")
        
        -- Test 6: Save the main result
        RPD.glog("Test 6: Saving main bitmap...")
        bitmap:savePng("test_bitmap_output.png")
        RPD.glog("Saved main output to: test_bitmap_output.png")
        
        -- Clean up
        bitmap:dispose()
        
        RPD.glog("BitmapData interface tests completed successfully!")
        RPD.glog("Files saved:")
        RPD.glog("- test_bitmap_output.png (main result)")
        RPD.glog("- test_bitmap_copy_output.png (copyRect result)")
        RPD.glog("- test_bitmap_rectcopy_output.png (rectCopy result)")
    end)
    
    if not success then
        RPD.glogn("BitmapData interface tests failed: " .. tostring(errorMsg))
        RPD.glogn("Please check the console output for stack traces to help debug the issue.")
    end
end


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
            heapScale     = 3.,
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
                    "testBitmapData",
                    "debugBitmapData",  -- New debug action
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

        --RPD.affectBuff(owner, RPD.Buffs.Blindness, 100)

        RPD.glog("execute owner: %s", tostring(owner))

        if action == "action1" then
            owner:playExtra("zan")
            --local ads = require("scripts/lib/ads")
--[[
            local a = storage.gameGet("action1")
            local b = storage.get("action1")
            local c = storage.modGet("action1")

            RPD.glog("stored data: "..tostring(a).."|"..tostring(b)..""..tostring(c))


            storage.gamePut("action1", true)
            storage.put("action1", true)
            storage.modPut("action1", true)

            a = storage.gameGet("action1")
            b = storage.get("action1")
            c = storage.modGet("action1")

            RPD.glog("stored data: "..tostring(a).."|"..tostring(b)..""..tostring(c))


            if ads.rewardVideoReady() then
                ads.rewardVideoShow(RPD.createItem("Gold",'{"quantity":500}'))
            else
                RPD.glogn("Reward video not ready")
            end

            --ads.interstitialShow()

            --RPD.affectBuff(hero, RPD.Buffs.Invisibility ,200)
            --item:selectCell("action1","Please select cell for action 1")
            --RPD.playMusic("surface",true);

            local banner = RPD.new(RPD.Objects.Ui.Banner,"amulet.png")
            banner:show(0xFFAA55, 5, 10)
            RPD.GameScene:showBanner(banner) ]]--
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

            local function errFunc(arg, lvl)
                local var = 1
                local var2 = "abc"
                lvl = (lvl or 0)+ 1
                if lvl > 5 then
                    error("test error")
                else
                    errFunc(arg, lvl)
                end
            end

            local packedItem = RPD.packEntity(item)
            RPD.glog(packedItem)
            local restoredItem = RPD.unpackEntity(packedItem)
            local luaDesc = RPD.toLua(restoredItem)
            restoredItem = RPD.fromLua(luaDesc)
            packedItem = RPD.packEntity(restoredItem)
            RPD.glog(packedItem)
            --errFunc(packedItem)

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

        if action == "testBitmapData" then
            runBitmapDataTests()
        end

        if action == "debugBitmapData" then
            -- Comprehensive debug test to check BitmapData creation and all methods
            RPD.glog("Comprehensive BitmapData debug test started...")
            
            -- Test 1: Basic object creation
            RPD.glog("Test 1: Basic object creation...")
            local bitmap1 = RPD.new("com.nyrds.platform.gfx.BitmapData", 100, 100)
            if bitmap1 then
                RPD.glog("SUCCESS: BitmapData created with size " .. bitmap1:getWidth() .. "x" .. bitmap1:getHeight())
                
                -- Test 2: Test all methods
                RPD.glog("Test 2: Testing all methods...")
                
                -- Test clear
                local success, errorMsg = pcall(function()
                    bitmap1:clear(0xFF0000FF)
                    RPD.glog("SUCCESS: clear() method works")
                end)
                if not success then
                    RPD.glogn("FAILED: clear() method - " .. tostring(errorMsg))
                end
                
                -- Test fillRect
                success, errorMsg = pcall(function()
                    bitmap1:fillRect(0, 0, 50, 50, 0xFFFF0000)
                    RPD.glog("SUCCESS: fillRect() method works")
                end)
                if not success then
                    RPD.glogn("FAILED: fillRect() method - " .. tostring(errorMsg))
                end
                
                -- Test fillCircle
                success, errorMsg = pcall(function()
                    bitmap1:fillCircle(50, 50, 20, 0xFF00FF00)
                    RPD.glog("SUCCESS: fillCircle() method works")
                end)
                if not success then
                    RPD.glogn("FAILED: fillCircle() method - " .. tostring(errorMsg))
                end
                
                -- Test drawLine
                success, errorMsg = pcall(function()
                    bitmap1:drawLine(0, 0, 50, 50, 0xFFFFFFFF)
                    RPD.glog("SUCCESS: drawLine() method works")
                end)
                if not success then
                    RPD.glogn("FAILED: drawLine() method - " .. tostring(errorMsg))
                end
                
                -- Test getPixel
                success, errorMsg = pcall(function()
                    local pixel = bitmap1:getPixel(10, 10)
                    RPD.glog("SUCCESS: getPixel() method works - pixel value: " .. string.format("0x%08X", pixel))
                end)
                if not success then
                    RPD.glogn("FAILED: getPixel() method - " .. tostring(errorMsg))
                end
                
                -- Test savePng
                success, errorMsg = pcall(function()
                    bitmap1:savePng("debug_test_output.png")
                    RPD.glog("SUCCESS: savePng() method works")
                end)
                if not success then
                    RPD.glogn("FAILED: savePng() method - " .. tostring(errorMsg))
                end
                
                -- Clean up
                bitmap1:dispose()
            else
                RPD.glogn("FAILED: Could not create BitmapData object")
            end
            
            RPD.glog("BitmapData comprehensive debug test completed.")
        end
    end,

    activate = function(self, item, hero)

      --  local Buff = RPD.affectBuff(hero,"NotImplementedTestBuff", 10)
      --  Buff:level(3)
      --  Buff:setSource(item)
    end,

    deactivate = function(self, item, hero)
      --  RPD.removeBuff(hero,"NotImplementedTestBuff")
    end,

    act = function(self,item)
        self.data.counter = (self.data.counter or 0) + 1

        if item:getOwner():valid() then
            item:getOwner():showStatus( 0xFF00FF, tostring(self.data.counter))
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
