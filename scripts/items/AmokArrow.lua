---
--- AmokArrow: data arrow kind (was weapon/missiles/AmokArrow.java). Bow coupling
--- (firedFrom stat scaling, bow enchant relay) stays java-side; the kind
--- supplies base stats + the special proc.
---
local RPD     = require "scripts/lib/commonClasses"
local weapon  = require "scripts/lib/weapon"

return weapon.init{
    desc = function()
        return {
            baseMin   = 1,
            baseMax   = 1,
            baseDly   = 1.0,
            image     = 15,
            pricePerUnit = 5,
            randomLo  = 15,
            randomHi  = 30,
        }
    end,
    attackProc = function(self, item, attacker, defender, damage)
        if item:activateSpecial(attacker, defender, damage) then
            RPD.affectBuff(defender, "Amok", 20)
        RPD.affectBuff(defender, "Speed", 20)
        RPD.permanentBuff(defender, "RageBuff")
        end
        return damage
    end,
}
