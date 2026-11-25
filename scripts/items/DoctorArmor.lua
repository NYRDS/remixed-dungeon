---
--- DoctorArmor: class-armor data kind (was armor/DoctorArmor.java). The special action id,
--- equip gate and visual flags live in the desc; the special itself is the
--- framework flag ("leaper"/"teleporter"/...) or a hook below.
---
local RPD    = require "scripts/lib/commonClasses"
local weapon = require "scripts/lib/weapon"

return weapon.init{
    desc = function()
        return {
            tier      = 6,
            image     = 31,
            upgradable = false,
            identified = true,
            price     = 0,
            special   = "DoctorSpecial",
            gateClass = "DOCTOR",
            hasHelmet = true,
            hasCollar = true,
            coverHair = true,
            coverFacialHair = true,
            visualName = PlagueDoctorArmor,
        }
    end,
    afterEquip = function(self, item, hero)
        -- the built-in beaked mask is the real one: permanent gas immunity
        RPD.permanentBuff(hero, "GasesImmunity")
    end,

    beforeUnequip = function(self, item, hero)
        -- immunity lives while either source is worn (mask or this armor)
        local mask = hero:getItemFromSlot(RPD.Slots.artifact)
        if mask == nil or not mask:valid() or mask:getEntityKind() ~= "PlagueDoctorMask" then
            RPD.removeBuff(hero, "GasesImmunity")
        end
    end,
}
