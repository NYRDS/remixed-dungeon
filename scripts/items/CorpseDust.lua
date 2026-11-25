---
--- CorpseDust: cursed quest artifact; wearing it aggroes everything nearby
--- via the registered RageBuff (was quest/CorpseDust.java).
---
local RPD  = require "scripts/lib/commonClasses"
local item = require "scripts/lib/item"

return item.init{
    desc = function()
        return {
            image      = 5,
            imageFile  = "items/artifacts.png",
            name       = "CorpseDust_Name",
            info       = "CorpseDust_Info",
            stackable  = false,
            upgradable = false,
            identified = true,
            cursed     = true,
            price      = 0,
            equipable  = RPD.Slots.artifact,
        }
    end,

    activate = function(self, item, hero)
        RPD.ItemUtils:attachArtifactBuff(hero, item, "RageBuff")
    end,

    deactivate = function(self, item, hero)
        RPD.ItemUtils:detachArtifactBuff(hero, item)
    end,

    info = function(self, item)
        return RPD.textById("CorpseDust_Info") .. "\n\n" .. RPD.textById("CorpseDust_Info2")
    end,
}
