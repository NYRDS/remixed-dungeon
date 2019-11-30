--
-- User: mike
-- Date: 29.01.2019
-- Time: 20:33
-- This file is part of Remixed Pixel Dungeon.
--
local RPD = require "scripts/lib/commonClasses"

local item = require "scripts/lib/item"

local shields = require "scripts/lib/shields"

local shieldLevel = 4
local shieldDesc  = "RoyalShield_desc"

local baseDesc = shields.makeShield(shieldLevel,shieldDesc)

baseDesc.desc = function (self, item)
    return {
        image         = 3,
        imageFile     = "items/shields.png",
        name          = "RoyalShield_name",
        info          = shieldDesc,
        price         = 160 * shieldLevel,
        equipable     = "left_hand",
        upgradable    = true
    }
end

return item.init(baseDesc)
