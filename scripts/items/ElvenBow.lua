---
--- ElvenBow: data bow/crossbow kind (was guts/weapon/ranged or melee bow class).
--- The arrow-scaling factors take the bow level; max damage == min.
---
local RPD      = require "scripts/lib/commonClasses"
local itemLib  = require "scripts/lib/item"
local weapon   = require "scripts/lib/weapon"

return weapon.init{
    desc = function()
        return {
            tier      = 1,
            acu       = 1.2,
            dly       = 1.0,
            image     = 6,
            anim      = "bow",
            noBurn    = true,
            visualName = "WoodenBow",
        }
    end,
        acuFactor = function(self, item, lvl)
        return 1 + 0.15 * lvl
    end,
        dmgFactor = function(self, item, lvl)
        return 1 + 0.30 * lvl
    end,
}
