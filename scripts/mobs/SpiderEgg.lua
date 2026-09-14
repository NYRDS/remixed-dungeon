local RPD = require "scripts/lib/commonClasses"
local mob = require "scripts/lib/mob"

-- java SpiderEgg (batch 12): hatches a random level mob after ~20 ticks, then
-- quietly removes itself; carries a treasury SEED roll 20% (java ctor loot).
-- Java postponed its turn by 20; the act hook cannot spend, so the delay is a
-- tick counter - same wall clock.
local HATCH_TICKS = 20

return mob.init{
    stats = function(self)
        local data = mob.restoreData(self)
        if data.rolled then
            return
        end
        if not self:getBelongings():isBackpackEmpty() then
            -- pre-migration saves already carry the seed their java rolled
            data.rolled = true
            return
        end
        data.rolled = true

        if math.random() <= 0.2 then
            self:collect(RPD.Treasury:getLevelTreasury():random("SEED"))
        end
    end,
    act = function(self)
        local data = mob.restoreData(self)
        data.ticks = (data.ticks or 0) + 1
        if data.ticks < HATCH_TICKS then
            return
        end
        data.ticks = 0
        local spider = RPD.MobSpawner:spawnRandomMob(RPD.Dungeon.level, self:getPos(), 25)
        if spider ~= nil and spider:valid() then
            self:remove()
        end
    end
}
