local RPD = require "scripts/lib/commonClasses"

local item = require "scripts/lib/item"

local shields = require "scripts/lib/shields"

local shieldLevel = 1
local shieldDesc  = "ChaosShield_desc"


local function chargeForLevel(item)
    return 5 * math.pow(item:level(), 1.5)
end

local baseDesc = shields.makeShield(shieldLevel,shieldDesc, "ChaosShieldLeft")

baseDesc.ownerTakesDamage = function(self, item, damage)
    self.data.charges = self.data.charges or 0
    self.data.charges = self.data.charges - 1

    if self.data.charges < 0 then
        if item:level() > 3 then
            item:degrade()
            self.data.charges = chargeForLevel(item)
        end
    end

    --RPD.debug("Charges: %d", self.data.charges)
end

baseDesc.ownerDoesDamage = function(self, item, damage)
    if item:isCursed() then
        return
    end

    self.data.charges = self.data.charges or 0
    self.data.charges = self.data.charges + 1

    if self.data.charges > chargeForLevel(item) then
        item:upgrade()
        self.data.charges = 0
    end

    --RPD.debug("Charges: %d", self.data.charges)
end


baseDesc.desc = function (self, item)
    return {
        image         = 0,
        imageFile     = "items/chaosShield.png",
        name          = "ChaosShield_name",
        info          = shieldDesc,
        price         = 20 * shieldLevel,
        equipable     = "left_hand",
        upgradable    = true
    }
end

baseDesc.image = function(self, item)
    return math.max(0, math.min(item:level()/3, 4))
end


local ret = item.init(baseDesc)


return ret