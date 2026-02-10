--
-- User: mike
-- Date: 18.09.2025
-- Time: 12:00
-- This file is part of Remixed Pixel Dungeon.
--
-- Item selector helper for initiating backpack item selection from Lua scripts
--

local RPD = require "scripts/lib/commonClasses"

local itemSelector = {}

-- Function to select an item from the backpack
-- Parameters:
--   callback: function to call when an item is selected (receives item and selector as parameters)
--   mode: selection mode (optional, defaults to ALL)
--   title: window title (optional)
--   selector: character that is selecting the item (optional, defaults to hero)
function itemSelector.selectItem(callback, mode, title, selector)
    -- Validate callback
    if type(callback) ~= "function" then
        RPD.glog("Error: callback must be a function")
        return
    end
    
    -- Set defaults
    mode = mode or RPD.BackpackMode.ALL
    title = title or "Select an item"
    selector = selector or RPD.Dungeon.hero
    
    -- Store the callback in a way that can be accessed from Java
    -- We'll use a global table to store callbacks
    if not _G.ItemSelectionCallbacks then
        _G.ItemSelectionCallbacks = {}
    end
    
    -- Generate a unique ID for this callback
    local callbackId = #_G.ItemSelectionCallbacks + 1
    _G.ItemSelectionCallbacks[callbackId] = callback
    
    -- Create a listener using a custom Java class that can call back to Lua
    local listener = luajava.newInstance("com.nyrds.pixeldungeon.windows.LuaWndBagListener", callbackId)
    
    -- Show the backpack window
    local wndBag = luajava.newInstance("com.watabou.pixeldungeon.windows.WndBag", 
                          selector:getBelongings(), 
                          nil,  -- bag (nil for default)
                          listener, 
                          mode, 
                          title)
    RPD.GameScene:show(wndBag)
end

-- Convenience functions for common selection modes
function itemSelector.selectAnyItem(callback, title, selector)
    itemSelector.selectItem(callback, RPD.BackpackMode.ALL, title, selector)
end

function itemSelector.selectUnidentifiedItem(callback, title, selector)
    itemSelector.selectItem(callback, RPD.BackpackMode.UNIDENTIFED, title, selector)
end

function itemSelector.selectUpgradeableItem(callback, title, selector)
    itemSelector.selectItem(callback, RPD.BackpackMode.UPGRADEABLE, title, selector)
end

function itemSelector.selectWeapon(callback, title, selector)
    itemSelector.selectItem(callback, RPD.BackpackMode.WEAPON, title, selector)
end

function itemSelector.selectArmor(callback, title, selector)
    itemSelector.selectItem(callback, RPD.BackpackMode.ARMOR, title, selector)
end

function itemSelector.selectWand(callback, title, selector)
    itemSelector.selectItem(callback, RPD.BackpackMode.WAND, title, selector)
end

function itemSelector.selectSeed(callback, title, selector)
    itemSelector.selectItem(callback, RPD.BackpackMode.SEED, title, selector)
end

function itemSelector.selectArrow(callback, title, selector)
    itemSelector.selectItem(callback, RPD.BackpackMode.ARROWS, title, selector)
end

return itemSelector