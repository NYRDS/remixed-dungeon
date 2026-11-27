---
--- PseudoAmulet: a fake Amulet of Yendor - picking it may spawn the amulet
--- mimic (was guts/PseudoAmulet.java).
---
local RPD  = require "scripts/lib/commonClasses"
local item = require "scripts/lib/item"

return item.init{
    desc = function()
        return {
            image      = 87, -- ItemSpriteSheet.AMULET (identical sprite on purpose)
            imageFile  = "items.png",
            name       = "Amulet_Name",
            info       = "Amulet_Info",
            stackable  = false,
            upgradable = false,
            identified = true,
            price      = 0,
        }
    end,

    pick = function(self, item, ch, pos)
        return RPD.CharUtils:tryToSpawnMimic(item, ch, pos, "MimicAmulet")
    end,
}
