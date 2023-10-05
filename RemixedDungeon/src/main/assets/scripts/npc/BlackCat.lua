--
-- User: mike
-- Date: 23.11.2017
-- Time: 21:04
-- This file is part of Remixed Pixel Dungeon.
--

local RPD = require "scripts/lib/commonClasses"

local mob = require"scripts/lib/mob"

return mob.init({
    interact = function(self, chr)
        self:say("BlackCat_Phrases",math.random(0,2))
        self:playExtra("sleep")
    end,

    die = function(self, cause)
        local hero = RPD.Dungeon.hero
        hero:STR(math.max(hero:STR()-1,1))
        hero:getSprite():emitter():burst( RPD.Sfx.ShadowParticle.CURSE, 6 )
        hero:getSprite():showStatus( 0xFF0000, RPD.textById("Str_lose"))
        RPD.playSound( "snd_cursed" )
    end,

    spawn = function(me, level)
        RPD.setAi(me,"BlackCat")
    end
})


