---
--- Sungrass: plants the Health regen buff on the pressing char
--- (was plants/Sungrass.java; the Health buff itself is scripts/buffs/Health.lua)
---
local RPD = require "scripts/lib/commonClasses"
local plant = require "scripts/lib/plant"

return plant.init{
    effect = function(self, plantObject, pos, presser, activator)
        if presser ~= nil and RPD.CharUtils:isChar(presser) then
            RPD.affectBuff(presser, "Health")
        end

        if RPD.Dungeon:isCellVisible(pos) then
            RPD.Sfx.CellEmitter:get(pos):start(RPD.Sfx.ShaftParticle.FACTORY, 0.2, 3)
        end
    end
}
