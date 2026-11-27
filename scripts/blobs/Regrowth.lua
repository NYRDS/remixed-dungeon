---
--- Regrowth: grass grows back on the covered plain tiles (was Regrowth.java).
---
local RPD  = require "scripts/lib/commonClasses"
local blob = require "scripts/lib/blobbase"

return blob.init{
    desc = function()
        return {
            mode = "diffusion",
        }
    end,

    affect = function(self, gas, cells)
        local level = RPD.Dungeon.level
        local mapUpdated = false

        for i = 1, cells.length do
            local pos = cells[i]
            if level:isPlainTile(pos) then
                local c = level.map[pos + 1]
                if c == RPD.Terrain.EMPTY or c == RPD.Terrain.EMBERS or c == RPD.Terrain.EMPTY_DECO then
                    level:set(pos, gas.cur[pos + 1] > 9 and RPD.Terrain.HIGH_GRASS or RPD.Terrain.GRASS)
                    mapUpdated = true
                elseif c == RPD.Terrain.GRASS and gas.cur[pos + 1] > 9 then
                    level:set(pos, RPD.Terrain.HIGH_GRASS)
                    mapUpdated = true
                end
            end
        end

        if mapUpdated then
            RPD.GameScene:updateMap()
        end
    end,
}
