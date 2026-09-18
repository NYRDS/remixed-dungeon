---
--- Rotberry: toxic burst, re-drops its seed, roots the pressing char.
--- WandMaker quest plant - name/desc come from WandMaker_* string ids.
--- (was plants/Rotberry.java)
---
local RPD = require "scripts/lib/commonClasses"
local plant = require "scripts/lib/plant"

return plant.init{
    name = function(self, plantObject, level)
        return RPD.textById("WandMaker_RotberryName")
    end,

    info = function(self, plantObject, level)
        return RPD.textById("WandMaker_RotberryDesc")
    end,

    effect = function(self, plantObject, pos, presser, activator)
        RPD.placeBlob(RPD.Blobs.ToxicGas, pos, 100)

        RPD.Dungeon.level:animatedDrop(RPD.item("Rotberry.Seed"), pos)

        if presser ~= nil and RPD.CharUtils:isChar(presser) then
            RPD.affectBuff(presser, "Roots", 3)
        end
    end
}
