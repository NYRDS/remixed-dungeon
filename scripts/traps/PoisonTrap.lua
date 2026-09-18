local RPD = require "scripts/lib/commonClasses"
local trap = require "scripts/lib/trap"

-- java PoisonTrap: poison the character on the cell (falls back to whoever
-- stands there when the trap was set off by an item), splash visual either way.
return trap.init(
    function (cell, char, data)
        if char == nil then
            char = RPD.Actor:findChar(cell)
        end
        if char ~= nil then
            local duration = RPD.CharUtils:durationFactor(char) * (4 + math.floor(RPD.Dungeon.depth / 2))
            RPD.Buffs.Buff:affect(char, "Poison", duration)
        end
        RPD.Sfx.CellEmitter:center(cell):burst(RPD.Sfx.PoisonParticle.SPLASH, 3)
    end
)
