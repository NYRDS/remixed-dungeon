---
--- Sorrowmoss: poisons the pressing char (was plants/Sorrowmoss.java)
---
local RPD = require "scripts/lib/commonClasses"
local plant = require "scripts/lib/plant"

return plant.init{
    effect = function(self, plantObject, pos, presser, activator)
        if presser ~= nil and RPD.CharUtils:isChar(presser) then
            RPD.affectBuff(presser, "Poison", RPD.CharUtils:durationFactor(presser) * (4 + math.floor(RPD.Dungeon.depth / 2)))
        end

        if RPD.Dungeon:isCellVisible(pos) then
            RPD.Sfx.CellEmitter:center(pos):burst(RPD.Sfx.PoisonParticle.SPLASH, 3)
        end
    end
}
