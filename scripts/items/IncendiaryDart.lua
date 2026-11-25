---
--- IncendiaryDart: data dart kind (was weapon/missiles/IncendiaryDart.java).
---
local RPD     = require "scripts/lib/commonClasses"
local weapon  = require "scripts/lib/weapon"

return weapon.init{
    desc = function()
        return {
            image     = 3,
            STR       = 12,
            min       = 1,
            max       = 2,
            pricePerUnit = 10,
            randomLo  = 3,
            randomHi  = 6,
            incendiary = true,
        }
    end,
    attackProc = function(self, item, attacker, defender, damage)
        RPD.CharUtils:ignite(defender)
        return damage
    end,
}
