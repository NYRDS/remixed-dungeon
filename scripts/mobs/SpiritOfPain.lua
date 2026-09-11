local mob = require "scripts/lib/mob"

return mob.init{
    -- java act(): self-consumes 6 hp per turn (a shard of living darkness)
    act = function(self)
        self:damage(6, self)
        return true
    end
}
