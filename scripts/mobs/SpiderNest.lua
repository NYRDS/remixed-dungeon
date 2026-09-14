local RPD = require "scripts/lib/commonClasses"
local mob = require "scripts/lib/mob"

-- java SpiderNest (batch 12): spawns a random level mob every ~20 ticks,
-- never removes itself; carries a PotionOfHealing roll 20% (json loot).
local SPAWN_TICKS = 20

return mob.init{
    act = function(self)
        local data = mob.restoreData(self)
        data.ticks = (data.ticks or 0) + 1
        if data.ticks < SPAWN_TICKS then
            return
        end
        data.ticks = 0
        RPD.MobSpawner:spawnRandomMob(RPD.Dungeon.level, self:getPos(), 20)
    end
}
