---
--- Javelin: data missile kind (was weapon/missiles/Javelin.java).
---
local RPD     = require "scripts/lib/commonClasses"
local serpent = require "scripts/lib/serpent"
local weapon  = require "scripts/lib/weapon"

return weapon.init{
    desc = function()
        return {
            STR       = 15,
            min       = 2,
            max       = 15,
            dly       = 1.0,
            image     = 110,
            randomLo  = 5,
            randomHi  = 15,
            pricePerUnit = 15,
        }
    end,

    randomQty = function(self, item, part)
        if part == 0 then return 5 end
        return 15
    end,
    attackProc = function(self, item, attacker, defender, damage)
        RPD.affectBuff(defender, "Cripple", 10)
        return damage
    end,
}
