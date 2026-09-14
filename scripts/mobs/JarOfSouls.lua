local RPD = require "scripts/lib/commonClasses"
local mob = require "scripts/lib/mob"

-- java JarOfSouls (batch 12): while it sees the enemy it plays its attack
-- animation and spawns a random level mob, limitless. Java postponed its turn
-- by 15; the act hook cannot spend, so the delay is a tick counter.
local SPAWN_TICKS = 15

return mob.init{
    act = function(self)
        if not self.enemySeen then
            return
        end
        local data = mob.restoreData(self)
        data.ticks = (data.ticks or 0) + 1
        if data.ticks < SPAWN_TICKS then
            return
        end
        data.ticks = 0
        local enemy = self:getEnemy()
        if enemy ~= nil then
            self:playAttack(enemy:getPos())
        end
        RPD.MobSpawner:spawnRandomMob(RPD.Dungeon.level, self:getPos(), -1)
    end
}
