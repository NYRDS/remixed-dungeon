--
-- User: mike
-- Date: 29.01.2019
-- Time: 20:33
-- This file is part of Remixed Pixel Dungeon.
--

local RPD = require "scripts/lib/commonClasses"

local item = require "scripts/lib/item"

local shields = require "scripts/lib/shields"

return item.init{
    desc  = function (self, item)

        return {
            image         = 2,
            imageFile     = "items/shields.png",
            name          = "StrongShield_name",
            info          = "StrongShield_desc",
            price         = 20,
            equipable     = "left_hand"
        }
    end,

    activate = function(self, item, hero)
        RPD.affectBuff(hero,"ShieldLeft", 2):level(2)
    end,

    deactivate = function(self, item, hero)
        RPD.removeBuff(hero,"ShieldLeft")
    end,

    info = function(self, item)
        local hero = RPD.Dungeon.hero --TODO fix me
        local str = hero:effectiveSTR()

        return shields.info("StrongShield_desc", str, 2)
    end
}
