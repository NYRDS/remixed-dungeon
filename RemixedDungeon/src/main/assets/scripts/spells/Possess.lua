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
            targetingType = "cell",
            level         = 1,
            spellCost     = 1,
            castTime      = 0.5
        }
    end,
    castOnCell = function(self, spell, chr, cell)
        local target = RPD.Actor:findChar(cell)

        if target ~= nil then

            if not target:canBePet() then
                RPD.glog("Possess_PossessionFailed")
                return true
            end

            RPD.Mob:makePet(target, chr)
            target:setState(RPD.MobAi:getStateByTag("ControlledAi"))
            RPD.Dungeon.hero:setControlTarget(target)
            RPD.glog("Possess_Possessed")
            return true
        end

        RPD.glog("Possess_MustTargetChar")
        return false
    end
}
