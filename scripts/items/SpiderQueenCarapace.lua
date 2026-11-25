---
--- SpiderQueenCarapace: data material kind (was material/SpiderQueenCarapace.java).
---
local RPD     = require "scripts/lib/commonClasses"
local item    = require "scripts/lib/item"

return item.init{
    desc = function()
        return {
            image      = 22,
            imageFile  = "items/artifacts.png",
            name       = "SpiderQueenCarapace_Name",
            info       = "SpiderQueenCarapace_Info",
            stackable  = true,
            upgradable = false,
            identified = true,
            price      = 0,
        }
    end,
}
