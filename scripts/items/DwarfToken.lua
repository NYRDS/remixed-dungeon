---
--- DwarfToken: dwarf bread for the dwarf king's bounty
--- (was items/quest/DwarfToken.java).
---
local itemLib = require "scripts/lib/item"

return itemLib.init{
    desc = function()
        return {
            image     = 122, -- ItemSpriteSheet.TOKEN
            imageFile = "items.png",
            name      = "DwarfToken_Name",
            info      = "DwarfToken_Info",
            stackable = true,
            price     = 100, -- per quantity
        }
    end,
}
