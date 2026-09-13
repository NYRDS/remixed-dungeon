local RPD = require "scripts/lib/commonClasses"
local mob = require "scripts/lib/mob"

-- java Swarm (batch 8): any non-magical hit with damage < hp-2 splits off a
-- clone on a nearby cell; each generation halves the carried potion loot
-- chance and the carcass chance.
-- generation lives in self.data and reaches the clone through makeClone's
-- bundle round-trip (LUA_DATA), so the chain survives saving.
return mob.init{
    stats = function(self)
        -- restore-time re-derivation: a split swarm keeps its scaled carcass
        -- chance across save/load (fresh spawns keep the json 0.2)
        local generation = mob.restoreData(self).generation or 0
        if generation > 0 then
            self:setCarcassChance(0.2 / (generation + 1))
        end
    end,

    defenceProc = function(self, enemy, dmg)
        if dmg > 0 and self:hp() >= dmg + 2 then
            local level = RPD.Dungeon.level
            local cell = level:getEmptyCellNextTo(self:getPos())

            if level:cellValid(cell) then
                local clone = self:split(cell, dmg)
                local data = mob.restoreData(clone)
                data.generation = (data.generation or 0) + 1

                local chance = 0.2 / (data.generation + 1)
                clone:setCarcassChance(chance)
                clone:loot(RPD.item("PotionOfHealing"), chance)
            end
        end
        return dmg
    end
}
