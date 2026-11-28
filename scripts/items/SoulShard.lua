---
--- SoulShard: data material kind (was material/SoulShard.java).
---
local RPD     = require "scripts/lib/commonClasses"
local item    = require "scripts/lib/item"
local itemLib = require "scripts/lib/item"

return item.init{
    desc = function()
        return {
            image      = 21,
            imageFile  = "items/artifacts.png",
            name       = "SoulShard_Name",
            info       = "SoulShard_Info",
            stackable  = true,
            upgradable = false,
            identified = true,
            price      = 0,
        }
    end,
    glowing = function(self, item)
        return itemLib.makeGlowing(0x00FFFF, 1)
    end,
}
