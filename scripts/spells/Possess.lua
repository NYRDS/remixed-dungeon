--[[
    Created by mike.
    DateTime: 26.08.18 16:10
    This file is part of Remixed Pixel Dungeon
]]


local RPD = require "scripts/lib/commonClasses"

local spell = require "scripts/lib/spell"

return spell.init{
    desc  = function ()
        return {
            image         = 0,
            imageFile     = "spellsIcons/possession.png",
            name          = "Possess_Name",
            info          = "Possess_Info",
            magicAffinity = "Necromancy",
            targetingType = "char_not_self",
            level         = 4,
            spellCost     = 15,
            castTime      = 0.5
        }
    end,

    castOnChar = function(self, spell, caster, target)

        if not target:canBePet() then
            RPD.glogn("Possess_PossessionFailed",target:getName())
            return true
        end

        RPD.Mob:makePet(target, caster)
        target:setState(RPD.MobAi:getStateByTag("ControlledAi"))
        RPD.Dungeon.hero:setControlTarget(target)
        RPD.glogp("Possess_Possessed",target:getName())
        return true

    end
}
