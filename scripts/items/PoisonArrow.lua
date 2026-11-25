---
--- PoisonArrow: data arrow kind (was weapon/missiles/PoisonArrow.java). Bow coupling
--- (firedFrom stat scaling, bow enchant relay) stays java-side; the kind
--- supplies base stats + the special proc.
---
local RPD     = require "scripts/lib/commonClasses"
local weapon  = require "scripts/lib/weapon"

return weapon.init{
    desc = function()
        return {
            baseMin   = 3,
            baseMax   = 4,
            baseDly   = 0.75,
            image     = 9,
            pricePerUnit = 5,
            randomLo  = 15,
            randomHi  = 30,
        }
    end,
    attackProc = function(self, item, attacker, defender, damage)
        if item:activateSpecial(attacker, defender, damage) then
            local poisonFactor = 1
        local fired = item.firedFrom
        if fired ~= nil then poisonFactor = poisonFactor + fired:level() end
        RPD.affectBuff(defender, "Poison", RPD.CharUtils:durationFactor(defender) * poisonFactor)
        end
        return damage
    end,
}
