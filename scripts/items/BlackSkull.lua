---
--- BlackSkull: pet-death charge accumulator (was necropolis/BlackSkull.java).
--- Each non-pet pettable death charges it; at 10 charges it awakens and the
--- next pet death is resurrected for the full charge. Script-flagged mobs
--- (chess pieces, noResurrection) neither charge nor resurrect.
---
local RPD     = require "scripts/lib/commonClasses"
local itemLib = require "scripts/lib/item"

local RESURRECTION_COST = 10
local MAXIMUM_CHARGE    = 10

local ACTIVATED_IMAGE = 20
local BASIC_IMAGE     = 19

return itemLib.init{
    desc = function()
        return {
            image      = BASIC_IMAGE,
            imageFile  = "items/artifacts.png",
            name       = "BlackSkull_Name",
            info       = "BlackSkull_Info",
            stackable  = false,
            upgradable = false,
            identified = true,
            price      = 0,
            equipable  = RPD.Slots.artifact,
            data       = { activated = false, charge = 0 },
        }
    end,

    charDied = function(self, item, mob, cause)
        local hero = item:getOwner()
        if hero == nil then
            return
        end

        local pets = hero:getPets_l()
        for _, pet in pairs(pets) do
            if pet:getId() == mob:getId() then
                return
            end
        end

        -- caveman: script-flagged mobs (chess pieces) neither charge nor resurrect
        if mob.noResurrection then
            return
        end

        if mob:canBePet() then
            local data = self.data
            if data.activated then
                mob:resurrect(hero)
                RPD.glogw(RPD.textById("BlackSkull_Ressurrect"))
                data.charge = data.charge - RESURRECTION_COST
                if data.charge <= 0 then
                    RPD.glogw(RPD.textById("BlackSkull_Deactivated"))
                    data.activated = false
                end
            else
                data.charge = data.charge + 1
                if data.charge >= MAXIMUM_CHARGE then
                    RPD.glogw(RPD.textById("BlackSkull_Activated"))
                    data.activated = true
                end
            end
        end
    end,

    image = function(self, item)
        if self.data.activated then
            return ACTIVATED_IMAGE
        end
        return BASIC_IMAGE
    end,

    name = function(self, item)
        if self.data.activated then
            return RPD.textById("BlackSkull_Name_Awakened")
        end
        return RPD.textById("BlackSkull_Name")
    end,

    info = function(self, item)
        if self.data.activated then
            return RPD.textById("BlackSkull_Info_Awakened")
        end
        return RPD.textById("BlackSkull_Info")
    end,

    loadLegacy = function(self, item, bundle)
        -- pre-migration @Packable fields
        self.data = {
            activated = bundle:getBoolean("activated"),
            charge    = bundle:getInt("charge"),
        }
    end,
}
