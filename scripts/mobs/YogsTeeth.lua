local RPD = require "scripts/lib/commonClasses"
local mob = require "scripts/lib/mob"

-- java YogsTeeth: being hurt draws every mob to it; each hit rolls three
-- independent procs - life drain, bleeding as strong as the hit, and a
-- devoured (double) hit.
local function beckonAll(self)
    local mobs = RPD.Dungeon.level:getMobs()
    for i = 1, #mobs do
        mobs[i]:beckon(self:getPos())
    end
end

return mob.init{
    damage = function(self, dmg, src)
        beckonAll(self)
    end,
    attackProc = function(self, enemy, dmg)
        if enemy ~= nil then
            if math.random(3) == 1 then
                self:heal(dmg, self)
            end
            if math.random(3) == 1 then
                local bleeding = RPD.Buffs.Buff:affect(enemy, "Bleeding")
                bleeding:level(dmg)
            end
            if math.random(3) == 1 then
                RPD.Devour:hit(enemy)
                RPD.playSound("snd_bite")
                return dmg * 2
            end
        end
        return dmg
    end
}
