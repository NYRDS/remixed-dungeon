---
--- Amulet of Yendor: the end of the road (was items/Amulet.java).
--- Cannot be thrown or dropped; End shows the amulet scene, picking it up
--- marks the victory.
---
local RPD  = require "scripts/lib/commonClasses"
local item = require "scripts/lib/item"

return item.init{
    desc = function()
        return {
            image     = 87, -- ItemSpriteSheet.AMULET
            imageFile = "items.png",
            name      = "Amulet_Name",
            info      = "Amulet_Info",
            price     = 0,
        }
    end,

    removeActions = function(self, item)
        return { "Item_ACThrow", "Item_ACDrop" }
    end,

    actions = function(self, item)
        return { "Amulet_ACEnd" }
    end,

    execute = function(self, item, chr, action)
        if action == "Amulet_ACEnd" then
            RPD.AmuletScene:enter(false)
        end
    end,

    pickedUp = function(self, item, hero)
        if not RPD.Statistics.amuletObtained then
            RPD.Statistics.amuletObtained = true
            RPD.Badges:validateVictory()
            RPD.AmuletScene:enter(true)
        end
    end,
}
