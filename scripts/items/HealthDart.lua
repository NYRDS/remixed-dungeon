---
--- HealthDart: data dart kind (was weapon/missiles/HealthDart.java).
---
local RPD     = require "scripts/lib/commonClasses"
local potion  = require "scripts/lib/potion"
local weapon  = require "scripts/lib/weapon"

return weapon.init{
    desc = function()
        return {
            image     = 14,
            STR       = 8,
            min       = 1,
            max       = 3,
            pricePerUnit = 12,
            randomLo  = 2,
            randomHi  = 5,
        }
    end,
    attackProc = function(self, item, attacker, defender, damage)
        potion.heal(nil, item, defender, 0.1)
        return damage
    end,
}
