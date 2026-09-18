---
--- Icecap: freezes the plant cell and all adjacent non-LOS-blocking cells
--- (was plants/Icecap.java; java used a PathFinder distance map of limit 1,
--- which is the source cell + 8 neighbours, so the offsets are replicated
--- here including the raw +/-1 row-wrap quirk at level edges)
---
local RPD = require "scripts/lib/commonClasses"
local plant = require "scripts/lib/plant"

return plant.init{
    effect = function(self, plantObject, pos, presser, activator)
        local level = RPD.Dungeon.level
        local width = level:getWidth()
        local offsets = { 0, -1, 1, -width, width, -width - 1, -width + 1, width - 1, width + 1 }

        for i = 1, #offsets do
            local cell = pos + offsets[i]
            if cell >= 0 and cell < level:getLength() then
                if offsets[i] == 0 or not level.losBlocking[cell + 1] then
                    RPD.PseudoBlobs.Freezing:affect(cell)
                end
            end
        end
    end
}
