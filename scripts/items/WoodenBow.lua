---
--- WoodenBow: data bow/crossbow kind (was guts/weapon/ranged or melee bow class).
--- The arrow-scaling factors take the bow level; max damage == min.
---
local RPD      = require "scripts/lib/commonClasses"
local itemLib  = require "scripts/lib/item"
local weapon   = require "scripts/lib/weapon"

return weapon.init{
    desc = function()
        return {
            tier      = 1,
            acu       = 0.8,
            dly       = 1.5,
            image     = 0,
            anim      = "bow",
            noBurn    = true,
        }
    end,
        acuFactor = function(self, item, lvl)
        return 1 + 0.10 * lvl
    end,
        dmgFactor = function(self, item, lvl)
        return 1 + 0.25 * lvl
    end,
}
