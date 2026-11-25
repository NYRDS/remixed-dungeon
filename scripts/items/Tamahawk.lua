---
--- Tamahawk: data missile kind (was weapon/missiles/Tamahawk.java).
---
local RPD     = require "scripts/lib/commonClasses"
local serpent = require "scripts/lib/serpent"
local weapon  = require "scripts/lib/weapon"

return weapon.init{
    desc = function()
        return {
            STR       = 17,
            min       = 4,
            max       = 20,
            dly       = 1.0,
            image     = 107,
            randomLo  = 5,
            randomHi  = 12,
            pricePerUnit = 20,
            anim      = "sword",
            visualName = "Tomahawk",
        }
    end,

    randomQty = function(self, item, part)
        if part == 0 then return 5 end
        return 12
    end,
    attackProc = function(self, item, attacker, defender, damage)
        local b = RPD.affectBuff(defender, "Bleeding")  b:level(damage)
        return damage
    end,
}
