---
--- GnollTamahawk: data missile kind (was weapon/missiles/GnollTamahawk.java).
---
local RPD     = require "scripts/lib/commonClasses"
local serpent = require "scripts/lib/serpent"
local weapon  = require "scripts/lib/weapon"

return weapon.init{
    desc = function()
        return {
            STR       = 15,
            min       = 3,
            max       = 17,
            dly       = 1.0,
            randomLo  = 5,
            randomHi  = 12,
            pricePerUnit = 20,
            dropsToFloorOnThrow = true,
            notUsableInMelee = false,
            upgradable = true,
            imageFile = "items/gnoll_tomahawks.png",
            anim      = "sword",
            visualName = "Tomahawk",
        }
    end,

    randomQty = function(self, item, part)
        if part == 0 then return 5 end
        return 12
    end,
    loadLegacy = function(self, item, bundle)
        -- pre-migration @Packable imageIndex (random 0..8 gnoll variant)
        self.data = { imageIndex = bundle:getInt("imageIndex") }
        item:setImage(self.data.imageIndex)
    end,

    saveData = function(self)
        return serpent.dump(self.data or {})
    end,

    loadData = function(self, item, str)
        local _, data = serpent.load(str)
        self.data = data or {}
        if self.data.imageIndex ~= nil then
            item:setImage(self.data.imageIndex)
        end
    end,

    image = function(self, item)
        -- roll the art variant once, on first render (was the ctor roll)
        if self.data == nil or self.data.imageIndex == nil then
            self.data = { imageIndex = RPD.Random:Int(0, 8) }
        end
        return self.data.imageIndex
    end,
}
