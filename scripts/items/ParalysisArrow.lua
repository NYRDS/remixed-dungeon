---
--- ParalysisArrow: data arrow kind (was weapon/missiles/ParalysisArrow.java). Bow coupling
--- (firedFrom stat scaling, bow enchant relay) stays java-side; the kind
--- supplies base stats + the special proc.
---
local RPD     = require "scripts/lib/commonClasses"
local weapon  = require "scripts/lib/weapon"

return weapon.init{
    desc = function()
        return {
            baseMin   = 0,
            baseMax   = 4,
            baseDly   = 0.75,
            image     = 10,
            pricePerUnit = 5,
            randomLo  = 15,
            randomHi  = 30,
        }
    end,
    attackProc = function(self, item, attacker, defender, damage)
        if item:activateSpecial(attacker, defender, damage) then
            RPD.affectBuff(defender, "Stun", 4)
        end
        return damage
    end,
}
