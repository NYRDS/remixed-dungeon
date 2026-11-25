---
--- IceKey: a plain Item wearing the artifacts sprite (was icecaves/IceKey.java;
--- NOT a level-bound Key - it has no unlock semantics).
---
local RPD  = require "scripts/lib/commonClasses"
local item = require "scripts/lib/item"

return item.init{
    desc = function()
        return {
            image      = 22,
            imageFile  = "items/artifacts.png",
            name       = "IceKey_Name",
            info       = "IceKey_Info",
            stackable  = false,
            upgradable = false,
            identified = true,
            price      = 0,
            equipable  = "",
        }
    end,

    bag = function(self, item)
        return "Keyring"
    end,
}
