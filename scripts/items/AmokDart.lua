---
--- AmokDart: data dart kind (was weapon/missiles/AmokDart.java).
---
local RPD     = require "scripts/lib/commonClasses"
local weapon  = require "scripts/lib/weapon"

return weapon.init{
    desc = function()
        return {
            image     = 16,
            STR       = 14,
            min       = 1,
            max       = 3,
            pricePerUnit = 12,
            randomLo  = 2,
            randomHi  = 5,
        }
    end,
    attackProc = function(self, item, attacker, defender, damage)
        RPD.affectBuff(defender, "Amok", 20)
        RPD.affectBuff(defender, "Speed", 20)
        RPD.permanentBuff(defender, "RageBuff")
        return damage
    end,
}
