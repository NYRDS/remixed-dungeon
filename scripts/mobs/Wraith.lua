local RPD = require "scripts/lib/commonClasses"
local mob = require "scripts/lib/mob"

-- java Wraith (batch 7): stats scale with the depth the wraith manifests at,
-- and that depth is the only depth it ever lives on, so the stats hook
-- re-derives them from the current level depth (same on ctor and restore).
return mob.init{
    stats = function(mob)
        local d = RPD.Dungeon.depth
        mob:setDmgMax(3 + d)
        mob:setBaseAttackSkill(10 + d)
        mob:setBaseDefenseSkill((10 + d) * 5)
        mob.enemySeen = true
    end,
}
