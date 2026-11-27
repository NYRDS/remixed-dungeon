---
--- Foliage: the garden - plain tiles under the light turn to grass, the hero
--- regains shadows when resting among it (was Foliage.java; the terrain walk
--- and the once-per-tick effects ride the script).
---
local RPD  = require "scripts/lib/commonClasses"
local blob = require "scripts/lib/blobbase"

return blob.init{
    desc = function()
        return {
            mode = "persist",
        }
    end,

    affect = function(self, gas, cells)
        local level = RPD.Dungeon.level
        local map = level.map
        local regrowth = false

        for i = 1, cells.length do
            local pos = cells[i]
            if map[pos + 1] == RPD.Terrain.EMBERS or map[pos + 1] == RPD.Terrain.EMPTY then
                map[pos + 1] = RPD.Terrain.GRASS
                regrowth = true
            end
        end

        if regrowth then
            RPD.GameScene:updateMap()
        end
    end,

    after = function(self, gas, cells)
        local hero = RPD.Dungeon.hero
        if hero:isAlive() and hero:visibleEnemies() == 0 and gas.cur[hero:getPos() + 1] > 0 then
            RPD.CharUtils:refreshShadows(hero)
        end

        for i = 1, cells.length do
            if RPD.Dungeon:isCellVisible(cells[i]) then
                RPD.Journal:add(RPD.Journal.Feature.GARDEN:desc())
                return
            end
        end
    end,
}
