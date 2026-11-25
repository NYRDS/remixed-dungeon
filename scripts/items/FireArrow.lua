---
--- FireArrow: data arrow kind (was weapon/missiles/FireArrow.java). Bow coupling
--- (firedFrom stat scaling, bow enchant relay) stays java-side; the kind
--- supplies base stats + the special proc.
---
local RPD     = require "scripts/lib/commonClasses"
local weapon  = require "scripts/lib/weapon"

return weapon.init{
    desc = function()
        return {
            baseMin   = 1,
            baseMax   = 6,
            baseDly   = 0.75,
            image     = 8,
            pricePerUnit = 5,
            randomLo  = 15,
            randomHi  = 30,
        }
    end,
    attackProc = function(self, item, attacker, defender, damage)
        if item:activateSpecial(attacker, defender, damage) then
            RPD.CharUtils:ignite(defender)
        end
        return damage
    end,
}
