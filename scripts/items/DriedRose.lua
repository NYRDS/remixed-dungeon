---
--- DriedRose: the sad-ghost reward; its one-way-love charm states live in
--- the registered OneWayLoveBuff/OneWayCursedLoveBuff java buffs, picked by
--- the cursed flag (was quest/DriedRose.java).
---
local RPD  = require "scripts/lib/commonClasses"
local item = require "scripts/lib/item"

return item.init{
    desc = function()
        return {
            image      = 4,
            imageFile  = "items/artifacts.png",
            name       = "DriedRose_Name",
            info       = "DriedRose_Info",
            stackable  = false,
            upgradable = false,
            identified = true,
            price      = 0,
            equipable  = RPD.Slots.artifact,
        }
    end,

    activate = function(self, item, hero)
        if item:isCursed() then
            RPD.ItemUtils:attachArtifactBuff(hero, item, "OneWayCursedLoveBuff")
        else
            RPD.ItemUtils:attachArtifactBuff(hero, item, "OneWayLoveBuff")
        end
    end,

    deactivate = function(self, item, hero)
        RPD.ItemUtils:detachArtifactBuff(hero, item)
    end,

    info = function(self, item)
        return RPD.textById("DriedRose_Info") .. "\n\n" .. RPD.textById("DriedRose_Info2")
    end,
}
