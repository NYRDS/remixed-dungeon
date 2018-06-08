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
            name          = "Dark Sacrifice",
            info          = "Sacrifice loyal minion to cause explosion, explosion area depend on servant remaining health and your skill level",
            magicAffinity = "Necromancy",
            targetingType = "cell"
        }
    end,
    castOnCell = function(self, spell, chr, cell)
        local sacrifice = RPD.Actor:findChar(cell)

        local goodSacrifice = false

        if chr:getPets():contains(sacrifice) then
            sacrifice:yell("My life is yours!")
            goodSacrifice = true
        end

        if chr == sacrifice then
            sacrifice:yell("For the Scourge!")
            goodSacrifice = true
        end

        if sacrifice ~= nil and goodSacrifice == false then
            sacrifice:yell("You have no power over me!")
        end

        if goodSacrifice then
           sacrifice:getSprite():emitter():burst( RPD.Sfx.ShadowParticle.CURSE, 6 )
           RPD.playSound( "snd_cursed.mp3" )
           RPD.placeBlob(RPD.Blobs.LiquidFlame, sacrifice:getPos(), sacrifice:hp()*chr:magicLvl())
           sacrifice:damage(sacrifice:hp(),chr)
           return true
        end
        RPD.glog("Select your loyal servant to sacrifice")
        return false
    end
}
