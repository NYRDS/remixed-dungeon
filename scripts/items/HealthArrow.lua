---
--- HealthArrow: data arrow kind (was weapon/missiles/HealthArrow.java). Bow coupling
--- (firedFrom stat scaling, bow enchant relay) stays java-side; the kind
--- supplies base stats + the special proc.
---
local RPD     = require "scripts/lib/commonClasses"
local potion  = require "scripts/lib/potion"
local weapon  = require "scripts/lib/weapon"

return weapon.init{
    desc = function()
        return {
            baseMin   = 1,
            baseMax   = 1,
            baseDly   = 1.0,
            image     = 13,
            pricePerUnit = 10,
            randomLo  = 15,
            randomHi  = 30,
        }
    end,
    attackProc = function(self, item, attacker, defender, damage)
        if item:activateSpecial(attacker, defender, damage) then
            potion.heal(nil, item, defender, 0.15)
        end
        return damage
    end,
}
