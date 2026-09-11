local RPD = require "scripts/lib/commonClasses"
local mob = require "scripts/lib/mob"

return mob.init{
    attackProc = function(self, enemy, dmg)
        -- 1/3 chance to bleed some of the hero's gold on the floor
        if enemy:getEntityKind() == "Hero" and math.random(3) == 1 then
            local gp = math.random(1, math.max(self:hp() - 1, 1))
            RPD.Dungeon.level:drop(RPD.item("Gold", gp), self:getPos())
        end
        return dmg
    end
}
