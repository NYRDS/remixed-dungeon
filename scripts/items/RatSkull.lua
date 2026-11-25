---
--- RatSkull: the fetid-rat loot aura (registered RatterAura buff)
--- (was quest/RatSkull.java).
---
local RPD  = require "scripts/lib/commonClasses"
local item = require "scripts/lib/item"

return item.init{
    desc = function()
        return {
            image      = 10,
            imageFile  = "items/artifacts.png",
            name       = "RatSkull_Name",
            info       = "RatSkull_Info",
            stackable  = false,
            upgradable = false,
            identified = true,
            price      = 100,
            equipable  = RPD.Slots.artifact,
        }
    end,

    activate = function(self, item, hero)
        RPD.ItemUtils:attachArtifactBuff(hero, item, "RatterAura")
    end,

    deactivate = function(self, item, hero)
        RPD.ItemUtils:detachArtifactBuff(hero, item)
    end,

    info = function(self, item)
        return RPD.textById("RatSkull_Info") .. "\n\n" .. RPD.textById("RatSkull_Info2")
    end,
}
