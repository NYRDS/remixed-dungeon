---
--- ChaosArmor: charge-driven self-upgrading armor (was chaos/ChaosArmor.java).
--- Owner hits charge it (upgrade(true) at cap), owner damage taken drains it
--- (degrade + glyph strip at 0 past level 3); image follows the level.
---
local RPD      = require "scripts/lib/commonClasses"
local weapon   = require "scripts/lib/weapon"

return weapon.init{
    desc = function()
        return {
            tier      = 3,
            image     = 0,
            imageFile = "items/chaosArmor.png",
            upgradable = false,
        }
    end,
    effectiveDr = function(self, item, dr)
        return dr
    end,
    ownerTakesDamage = function(self, item, damage)
        local data = weapon.chaosState(self, item)
        data.charge = data.charge - 1
        if data.charge < 0 then data.charge = 0 end
        if item:level() > 3 and data.charge == 0 then
            item:degrade()
            item:inscribe(nil)
            data.charge = weapon.chaosChargeForLevel(self, item, 5)
            weapon.chaosSelectImage(self, item)
        end
    end,
    ownerDoesDamage = function(self, item, damage)
        if damage > 0 then
            local data = weapon.chaosState(self, item)
            data.charge = data.charge + 1
            if data.charge >= weapon.chaosChargeForLevel(self, item, 5) then
                item:upgrade(true)
                weapon.chaosSelectImage(self, item)
                data.charge = 0
            end
        end
    end,
    saveData = weapon.chaosSaveData,
    loadData = weapon.chaosLoadData,
    loadLegacy = function(self, item, bundle)
        -- pre-migration @Packable charge
        self.data = { charge = bundle:getInt("charge") }
        weapon.chaosSelectImage(self, item)
    end,
}
