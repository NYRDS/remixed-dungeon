---
--- CurareDart: data dart kind (was weapon/missiles/CurareDart.java).
---
local RPD     = require "scripts/lib/commonClasses"
local weapon  = require "scripts/lib/weapon"

return weapon.init{
    desc = function()
        return {
            image     = 4,
            STR       = 14,
            min       = 1,
            max       = 3,
            pricePerUnit = 12,
            randomLo  = 2,
            randomHi  = 5,
        }
    end,
    attackProc = function(self, item, attacker, defender, damage)
        RPD.affectBuff(defender, "Paralysis", 3)
        return damage
    end,
}
