--
-- User: mike
-- Date: 02.06.2018
-- Time: 20:39
-- This file is part of Remixed Pixel Dungeon.
--

local RPD = require "scripts/lib/commonClasses"

local spell = require "scripts/lib/spell"

return spell.init{
    desc  = function ()
        return {
            image         = 1,
            imageFile     = "spellsIcons/necromancy.png",
            name          = "DarkSacrifice_Name",
            info          = "DarkSacrifice_Info",
            magicAffinity = "Necromancy",
            targetingType = "char",
            level         = 3,
            spellCost     = 3,
            castTime      = 0.5
        }
    end,

    castOnChar = function(self, spell, caster, sacrifice)

        local goodSacrifice = false

        if sacrifice then
            if sacrifice:getOwnerId()==caster:getId() then
                sacrifice:yell("DarkSacrifice_Ok")
                goodSacrifice = true
            end

            if goodSacrifice == false then
                sacrifice:yell("DarkSacrifice_Resist")
            end
        end

        if goodSacrifice then
           sacrifice:getSprite():emitter():burst( RPD.Sfx.ShadowParticle.CURSE, 6 )
           RPD.playSound( "snd_cursed" )
           RPD.placeBlob(RPD.Blobs.LiquidFlame, sacrifice:getPos(), sacrifice:hp() * caster:skillLevel())
           sacrifice:damage(sacrifice:hp(), caster)
           return true
        end

        RPD.glog("DarkSacrifice_Hint")
        return false
    end
}
