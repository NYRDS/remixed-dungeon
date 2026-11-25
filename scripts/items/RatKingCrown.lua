---
--- RatKingCrown: rats struck by the wearer become pets via the registered
--- RatKingAuraBuff (was common/RatKingCrown.java).
---
local RPD  = require "scripts/lib/commonClasses"
local item = require "scripts/lib/item"

return item.init{
    desc = function()
        return {
            image      = 17,
            imageFile  = "items/artifacts.png",
            name       = "RatKingCrown_Name",
            info       = "RatKingCrown_Info",
            stackable  = false,
            upgradable = false,
            identified = true,
            price      = 0,
            equipable  = RPD.Slots.artifact,
        }
    end,

    activate = function(self, item, hero)
        RPD.ItemUtils:attachArtifactBuff(hero, item, "RatKingAuraBuff")
    end,

    deactivate = function(self, item, hero)
        RPD.ItemUtils:detachArtifactBuff(hero, item)
    end,
}
