---
--- HeartOfDarkness: azuterron trade artifact; defending hits spawn pets of
--- the Spirit of Pain via the registered HeartOfDarknessBuff
--- (was guts/HeartOfDarkness.java).
---
local RPD  = require "scripts/lib/commonClasses"
local item = require "scripts/lib/item"

return item.init{
    desc = function()
        return {
            image      = 18,
            imageFile  = "items/artifacts.png",
            name       = "HeartOfDarkness_Name",
            info       = "HeartOfDarkness_Info",
            stackable  = false,
            upgradable = false,
            identified = true,
            price      = 0,
            equipable  = RPD.Slots.artifact,
        }
    end,

    activate = function(self, item, hero)
        RPD.ItemUtils:attachArtifactBuff(hero, item, "HeartOfDarknessBuff")
    end,

    deactivate = function(self, item, hero)
        RPD.ItemUtils:detachArtifactBuff(hero, item)
    end,
}
