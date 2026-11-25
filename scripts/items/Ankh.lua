---
--- Ankh: a second chance, spent by CharUtils.ankhResurrect on death
--- (was items/Ankh.java).
---
local item = require "scripts/lib/item"

return item.init{
    desc = function()
        return {
            image     = 1, -- ItemSpriteSheet.ANKH
            imageFile = "items.png",
            name      = "Ankh_Name",
            info      = "Ankh_Info",
            stackable = true,
            price     = 50,
        }
    end,
}
