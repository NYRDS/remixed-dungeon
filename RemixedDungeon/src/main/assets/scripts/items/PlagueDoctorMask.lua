---
--- Generated by EmmyLua(https://github.com/EmmyLua)
--- Created by mike.
--- DateTime: 4/1/20 8:29 PM
---


local RPD = require "scripts/lib/commonClasses"

local item = require "scripts/lib/item"


local PlagueDoctorMask = luajava.newInstance("com.nyrds.pixeldungeon.items.accessories.PlagueDoctorMask")

return item.init{
    desc  = function ()
        return {
            image     = 26,
            imageFile = "items/artifacts.png",
            name      = "PlagueDoctorMask_Name",
            info      = "PlagueDoctorMask_Info",
            price     = 20,
            equipable = RPD.Slots.artifact
        }
    end,

    activate = function(self, item, hero)
        PlagueDoctorMask:equip(true)
        RPD.permanentBuff(hero, "GasesImmunity")
    end,

    deactivate = function(self, item, hero)
        PlagueDoctorMask:unequip(true)
        RPD.removeBuff(hero, "GasesImmunity")
    end
}
