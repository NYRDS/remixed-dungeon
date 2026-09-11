local RPD = require "scripts/lib/commonClasses"
local mob = require "scripts/lib/mob"

return mob.init{
    attackProc = function(self, enemy, dmg)
        -- 1/4 chance to summon a Spirit of Pain nearby
        if enemy ~= nil and math.random(4) == 1 then
            local level = RPD.Dungeon.level
            local pos   = self:emptyCellNextTo()
            if level:cellValid(pos) then
                local spirit = RPD.MobFactory:mobByName("SpiritOfPain")
                spirit:setPos(pos)
                level:spawnMob(spirit)
            end
        end
        return dmg
    end
}
