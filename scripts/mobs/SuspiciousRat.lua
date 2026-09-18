local RPD = require "scripts/lib/commonClasses"
local mob = require "scripts/lib/mob"
-- java SuspiciousRat (batch 12): once it sees the enemy it twitches, then
-- becomes a wererat (PseudoRat) ~4 ticks later. Java spent 4 ticks at once;
-- the act hook cannot spend, so the delay is a tick counter - same wall clock.
local TRANSFORM_TICKS = 4

return mob.init{
    act = function(self)
        if not self.enemySeen then
            return
        end
        local data = mob.restoreData(self)
        if not data.transforming then
            data.transforming = true
            data.ticks = 0
            self:showStatus(RPD.CharSprite.NEGATIVE, RPD.textById("Goo_StaInfo1"))
            RPD.glogn(RPD.textById("SuspiciousRat_Info1"))
            local enemy = self:getEnemy()
            if enemy ~= nil then
                self:getSprite():zap(enemy:getPos())
            end
            return
        end
        data.ticks = data.ticks + 1
        if data.ticks < TRANSFORM_TICKS then
            return
        end
        local pos = self:getPos()
        if RPD.Dungeon.level:cellValid(pos) then
            local wererat = RPD.MobFactory:mobByName(RPD.MobFactory.PSEUDO_RAT)
            wererat:setPos(pos)
            RPD.Dungeon.level:spawnMob(wererat)
            RPD.playSound("snd_cursed")
        end
        self:die(self)
    end
}
