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

        return {
            image         = 0,
            imageFile     = "items/shields.png",
            name          = "WoodenShield_name",
            info          = "WoodenShield_desc",
            price         = 20,
            equipable     = "left_hand"
        }
    end,

    activate = function(self, item, hero)
        RPD.affectBuff(hero,"ShieldLeft", 2):level(1)
    end,

    deactivate = function(self, item, hero)
        RPD.removeBuff(hero,"ShieldLeft")
    end
}
