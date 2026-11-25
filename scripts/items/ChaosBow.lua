---
--- ChaosBow: data bow/crossbow kind (was guts/weapon/ranged or melee bow class).
--- The arrow-scaling factors take the bow level; max damage == min.
---
local RPD      = require "scripts/lib/commonClasses"
local itemLib  = require "scripts/lib/item"
local weapon   = require "scripts/lib/weapon"

return weapon.init{
    desc = function()
        return {
            tier      = 3,
            acu       = 1.0,
            dly       = 1.0,
            image     = 0,
            anim      = "bow",
            imageFile = "items/chaosBow.png",
            upgradable = false,
        }
    end,
        acuFactor = function(self, item, lvl)
        return 1
    end,
        dmgFactor = function(self, item, lvl)
        return 1 + 0.30 * lvl
    end,
        onMiss = function(self, item, user)
        RPD.ChaosCommon:doChaosMark(user:getPos(), (weapon.chaosState(self, item)).charge + 3 * item:level())
    end,
        ownerTakesDamage = function(self, item, damage)
        weapon.chaosOwnerTakesDamage(self, item, damage, 5, 5)
    end,

    ownerDoesDamage = function(self, item, damage)
        weapon.chaosOwnerDoesDamage(self, item, damage, 5)
    end,

    saveData = weapon.chaosSaveData,
    loadData = weapon.chaosLoadData,

    loadLegacy = function(self, item, bundle)
        self.data = { charge = bundle:getInt("charge") }
        weapon.chaosSelectImage(self, item)
    end,
}
