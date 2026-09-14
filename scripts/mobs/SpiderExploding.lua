local RPD = require "scripts/lib/commonClasses"
local mob = require "scripts/lib/mob"

-- java SpiderExploding (batch 12): living bomb - on a hit applies its random
-- plant to the victim and bursts (attackProc fires only on a hit, same as the
-- java attack() override that died on super.attack()==true). Java picked the
-- plant via a random MultiKindMob kind; kind is visually inert, so the roll
-- lives in lua data.
local PLANTS = {
    "Firebloom", "Icecap", "Sorrowmoss", "Dreamweed",
    "Sungrass", "Earthroot", "Fadeleaf", "Moongrace"
}

return mob.init{
    stats = function(self)
        local data = mob.restoreData(self)
        if data.plant then
            return
        end
        data.plant = PLANTS[math.random(#PLANTS)]
    end,
    attackProc = function(self, enemy, dmg)
        if enemy ~= nil then
            local plant = RPD.LevelObjectsFactory:objectByName(mob.restoreData(self).plant)
            if plant ~= nil then
                plant:effect(enemy:getPos(), enemy, self)
            end
            self:die(self)
        end
        return dmg
    end
}
