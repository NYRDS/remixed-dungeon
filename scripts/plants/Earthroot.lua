---
--- Earthroot: roots the pressing char with an Earthroot Armor pool
--- (was plants/Earthroot.java; the Armor buff itself is scripts/buffs/Armor.lua)
---
local RPD = require "scripts/lib/commonClasses"
local plant = require "scripts/lib/plant"

return plant.init{
    effect = function(self, plantObject, pos, presser, activator)
        if presser ~= nil and RPD.CharUtils:isChar(presser) then
            local armor = RPD.affectBuff(presser, "Armor")
            if armor ~= nil then
                -- java wrote the raw field here (overwrites), unlike the
                -- raise-only Entanglement glyph caller
                armor:level(presser:ht())
            end
        end

        if RPD.Dungeon:isCellVisible(pos) then
            RPD.Sfx.CellEmitter:bottom(pos):start(RPD.Sfx.EarthParticle.FACTORY, 0.05, 8)
            RPD.shakeCamera(1, 0.4)
        end
    end
}
