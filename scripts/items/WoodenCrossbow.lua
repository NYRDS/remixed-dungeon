---
--- WoodenCrossbow: data bow/crossbow kind (was guts/weapon/ranged or melee bow class).
--- The arrow-scaling factors take the bow level; max damage == min.
---
local RPD      = require "scripts/lib/commonClasses"
local itemLib  = require "scripts/lib/item"
local weapon   = require "scripts/lib/weapon"

return weapon.init{
    desc = function()
        return {
            tier      = 2,
            acu       = 1.2,
            dly       = 1.6,
            image     = 1,
            anim      = "crossbow",
            noBurn    = true,
        }
    end,
        acuFactor = function(self, item, lvl)
        return 1 + 0.15 * lvl
    end,
        dmgFactor = function(self, item, lvl)
        return 1 + 0.35 * lvl
    end,
}
