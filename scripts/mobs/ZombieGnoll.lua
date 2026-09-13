local RPD = require "scripts/lib/commonClasses"
local mob = require "scripts/lib/mob"

local CharSprite = luajava.bindClass("com.watabou.pixeldungeon.sprites.CharSprite")

-- 35% chance to rise again unless burned down (java ZombieGnoll.die before batch 6)
return mob.init{
    die = function(self, cause)
        if math.random(100) > 65
                and cause ~= nil
                and cause:getEntityKind() ~= "Burning" then

            self:resurrect()

            RPD.Sfx.CellEmitter:center(self:getPos()):start(
                RPD.Sfx.Speck:factory(RPD.Sfx.Speck.BONE), 0.3, 3)
            RPD.playSound("snd_death")

            self:showStatus(CharSprite.NEGATIVE, RPD.textById("Goo_StaInfo1"))
            RPD.glogn(RPD.textById("ZombieGnoll_Info"))
        end
    end
}
