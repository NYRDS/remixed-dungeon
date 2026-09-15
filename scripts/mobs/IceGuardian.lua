local RPD = require "scripts/lib/commonClasses"
local mob = require "scripts/lib/mob"

-- java IceGuardian: on death it feeds its life force to every core on the
-- level (150 damage); a core that survives rebuilds the guardian twice over
-- (fresh spawns at the dying body's cell).
return mob.init{
    die = function(self, cause)
        local mobs = RPD.Dungeon.level:getMobs()
        for i = 1, #mobs do
            local core = mobs[i]
            if core:getEntityKind() == "IceGuardianCore" then
                core:damage(150, cause)
                if core:isAlive() then
                    self:resurrect()
                    self:resurrect()
                end
            end
        end
    end
}
