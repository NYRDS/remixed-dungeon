---
--- RatHide: data material kind (was material/RatHide.java).
---
local RPD     = require "scripts/lib/commonClasses"
local item    = require "scripts/lib/item"

return item.init{
    desc = function()
        return {
            image      = 23,
            imageFile  = "items/artifacts.png",
            name       = "RatHide_Name",
            info       = "RatHide_Info",
            stackable  = true,
            upgradable = false,
            identified = true,
            price      = 0,
        }
    end,
}
