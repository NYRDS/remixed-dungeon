---
--- ChaosSword: data weapon kind (was weapon/melee/ChaosSword.java / guts / common).
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
            upgradable = false,
            twoHanded = true,
            imageFile = "items/chaosSword.png",
        }
    end,

    anim = function(self, item)
        return "heavy"
    end,
    ownerTakesDamage = function(self, item, damage)
        weapon.chaosOwnerTakesDamage(self, item, damage, 3, 5)
    end,

    ownerDoesDamage = function(self, item, damage)
        weapon.chaosOwnerDoesDamage(self, item, damage, 5)
    end,

    saveData = weapon.chaosSaveData,
    loadData = weapon.chaosLoadData,

    loadLegacy = function(self, item, bundle)
        -- pre-migration java field: @Packable charge
        self.data = { charge = bundle:getInt("charge") }
        weapon.chaosSelectImage(self, item)
    end,
}
