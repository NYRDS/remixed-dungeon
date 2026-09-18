---
--- Fadeleaf: teleports the pressing char (was plants/Fadeleaf.java)
---
local RPD = require "scripts/lib/commonClasses"
local plant = require "scripts/lib/plant"

return plant.init{
    effect = function(self, plantObject, pos, presser, activator)
        if presser ~= nil and RPD.CharUtils:isChar(presser) then
            RPD.CharUtils:teleportRandom(presser)
        end

        if RPD.Dungeon:isCellVisible(pos) then
            RPD.Sfx.CellEmitter:get(pos):start(RPD.Sfx.Speck:factory(RPD.Sfx.Speck.LIGHT), 0.2, 3)
        end
    end
}
