---
--- ChaosBlade: data weapon kind (was weapon/melee/ChaosBlade.java / guts / common).
---
local RPD      = require "scripts/lib/commonClasses"
local itemLib  = require "scripts/lib/item"
local weapon   = require "scripts/lib/weapon"

return weapon.init{
    desc = function()
        return {
            tier      = 2,
            acu       = 1.0,
            dly       = 1.0,
            image     = 0,
            upgradable = false,
            imageFile = "items/chaosSword.png",
        }
    end,

    ownerTakesDamage = function(self, item, damage)
        weapon.chaosOwnerTakesDamage(self, item, damage, 2, 7)
    end,

    ownerDoesDamage = function(self, item, damage)
        weapon.chaosOwnerDoesDamage(self, item, damage, 7)
    end,

    saveData = weapon.chaosSaveData,
    loadData = weapon.chaosLoadData,

    loadLegacy = function(self, item, bundle)
        -- pre-migration java field: @Packable charge
        self.data = { charge = bundle:getInt("charge") }
        weapon.chaosSelectImage(self, item)
    end,
}
