---
--- CompositeCrossbow: data bow/crossbow kind (was guts/weapon/ranged or melee bow class).
--- The arrow-scaling factors take the bow level; max damage == min.
---
local RPD      = require "scripts/lib/commonClasses"
local itemLib  = require "scripts/lib/item"
local weapon   = require "scripts/lib/weapon"

return weapon.init{
    desc = function()
        return {
            tier      = 4,
            acu       = 1.1,
            dly       = 1.6,
            image     = 3,
            anim      = "crossbow",
        }
    end,
        acuFactor = function(self, item, lvl)
        return 1 + 0.50 * lvl
    end,
        dmgFactor = function(self, item, lvl)
        return 1 + 0.75 * lvl
    end,
        dlyFactor = function(self, item, lvl)
        return 1.1
    end,
}
