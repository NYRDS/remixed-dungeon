---
--- Boomerang: data missile kind (was weapon/missiles/Boomerang.java).
---
local RPD     = require "scripts/lib/commonClasses"
local serpent = require "scripts/lib/serpent"
local weapon  = require "scripts/lib/weapon"

return weapon.init{
    desc = function()
        return {
            STR       = 10,
            min       = 1,
            max       = 4,
            dly       = 1.0,
            image     = 106,
            returnsToHand = true,
            rerollForbiddenEnchants = true,
            upgradable = true,
            price     = 100,
            time2equip = 0,
            isFliesStraight = false,
            isFliesFastRotating = true,
        }
    end,

}
