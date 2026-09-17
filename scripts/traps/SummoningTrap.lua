local RPD = require "scripts/lib/commonClasses"
local trap = require "scripts/lib/trap"

-- java SummoningTrap: 3-5 random level mobs around the trap cell.
-- Accepted deltas vs java: mobs spawn immediately on a free neighbour cell
-- (java re-validated canSpawnAt and delayed each spawn by 0.1s).
return trap.init(
    function (cell, char, data)
        if RPD.Dungeon:bossLevel() then
            return
        end

        local level = RPD.Dungeon.level

        local nMobs = 3
        if math.random(2) == 1 then
            nMobs = nMobs + 1
            if math.random(2) == 1 then
                nMobs = nMobs + 1
            end
        end

        for i = 1, nMobs do
            local spawnCell = level:getEmptyCellNextTo(cell)
            if level:cellValid(spawnCell) then
                RPD.MobSpawner:spawnRandomMob(level, spawnCell, -1)
            end
        end
    end
)
