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
            targetingType = "cell",
        }
    end,
    castOnCell = function(self, spell, chr, cell)
        local sacrifice = RPD.Actor:findChar(cell)

        if chr:getPets():contains(sacrifice) or chr == sacrifice then
           sacrifice:yell("My life is yours!")
           sacrifice:getSprite():emitter():burst( RPD.Sfx.ShadowParticle.CURSE, 6 )
           RPD.playSound( "snd_cursed.mp3" )
           RPD.placeBlob(RPD.Blobs.LiquidFlame, sacrifice:getPos(), sacrifice:hp()*chr:magicLvl())
           sacrifice:die(chr)
           return true
        end
        RPD.glog("Select your loyal servant to sacrifice")
        return false
    end
}
