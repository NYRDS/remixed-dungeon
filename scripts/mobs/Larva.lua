local RPD = require "scripts/lib/commonClasses"
local mob = require "scripts/lib/mob"

-- java Larva: hunts from spawn; once leveled up (mob leveling) it bursts
-- into an imago at its own cell and dies. Java cursed its sprite emitter -
-- done here as a center cell burst, the closest lua-reachable effect.
local imago = {"Scorpio", "Worm", "Eye", "Scorpio"}

return mob.init{
    stats = function(self)
        -- java ctor lvl(1): a floor, so earnExp-levelled larvae (fresh or
        -- pre-migration saves) keep their level
        if self:lvl() < 1 then
            self:lvl(1)
        end
    end,
    spawn = function(self, level)
        RPD.setAi(self, "Hunting")
    end,
    act = function(self)
        if self:lvl() < 2 then
            return
        end
        RPD.Sfx.CellEmitter:center(self:getPos()):burst(RPD.Sfx.ShadowParticle.CURSE, 4)
        RPD.playSound("snd_cursed")
        local grown = RPD.MobFactory:mobByName(imago[math.random(#imago)])
        grown:setPos(self:getPos())
        RPD.Dungeon.level:spawnMob(grown)
        self:die(self)
    end
}
