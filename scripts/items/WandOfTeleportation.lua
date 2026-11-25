---
--- WandOfTeleportation: the caster teleports randomly; non-boss enemies are
--- banished to a random cell (was wands/WandOfTeleportation.java).
---
local RPD  = require "scripts/lib/commonClasses"
local wand = require "scripts/lib/wand"

return wand.init{
    desc = function()
        return {
            fx = "coldLight",
        }
    end,
    zap = function(self, item, cell, victim)
        local owner = item:getOwner()
        if victim == owner then
            item:setKnown()
            RPD.CharUtils:teleportRandom(owner)
        elseif victim ~= nil and not victim:isBoss() and victim:isMovable() then
            RPD.CharUtils:teleportAway(victim, owner)
        else
            RPD.GLog:i(RPD.textById("WandOfTeleportation_Info2"))
        end
    end,
}
