local RPD = require "scripts/lib/commonClasses"

local item = require "scripts/lib/item"

local shields = require "scripts/lib/shields"

local shieldLevel = 1
local shieldDesc  = "ChaosShield_desc"

local baseDesc = shields.makeShield(shieldLevel,shieldDesc)

baseDesc.desc = function (self, item)
    return {
        image         = 0,
        imageFile     = "items/chaosShield.png",
        name          = "ChaosShield_name",
        info          = shieldDesc,
        price         = 20 * shieldLevel,
        equipable     = "left_hand",
        upgradable    = true
    }
end

return item.init(baseDesc)
