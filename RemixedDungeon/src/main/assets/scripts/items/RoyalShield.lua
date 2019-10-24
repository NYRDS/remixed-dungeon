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
            image         = 3,
            imageFile     = "items/shields.png",
            name          = "RoyalShield_name",
            info          = "RoyalShield_desc",
            price         = 20,
            equipable     = "left_hand"
        }
    end,

    activate = function(self, item, hero)
        self.buff = RPD.affectBuff(hero,"ShieldLeft", 3)
        self.buff:level(4)
    end,

    deactivate = function(self, item, hero)
        self.buff:detach()
    end
}
