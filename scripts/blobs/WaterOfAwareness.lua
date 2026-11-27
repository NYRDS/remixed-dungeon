---
--- The well of awareness: reveals every secret tile on the level for the
--- drinking hero; an unidentified item dropped in gets identified. Was
--- WaterOfAwareness.java.
---
local RPD   = require "scripts/lib/commonClasses"
local well  = require "scripts/lib/wellbase"

return well.init{
    desc = function()
        return {
            mode     = "well",
            tileDesc = "WaterOfAwareness_Info",
        }
    end,

    after = function(self, water)
        if RPD.Dungeon:isCellVisible(water:pos()) then
            RPD.Journal:add(RPD.Journal.Feature.WELL_OF_AWARENESS:desc())
        end
    end,

    affectHero = function(self, water, hero)
        RPD.playSound("snd_drink")
        self:identifyAt(water)

        hero:getBelongings():observe()

        local level = hero:level()
        local map = level.map
        local flags = RPD.TerrainFlags.flags
        for i = 1, map.length do
            local terr = map[i]
            -- java arrays index 1-based from lua; SECRET = 0x08, no bit ops
            -- on LuaJ, so test the flag arithmetically
            if math.floor(flags[terr + 1] / 8) % 2 == 1 then
                level:set(i - 1, RPD.Terrain:discover(terr))
                RPD.GameScene:updateMapPair(i - 1)
                if RPD.Dungeon:isCellVisible(i - 1) then
                    RPD.GameScene:discoverTile(i - 1)
                end
            end
        end

        RPD.affectBuff(hero, "Awareness", 2)
        hero:observe()
        hero:interrupt()
        RPD.glogp(RPD.textById("WaterOfAwareness_Procced"))
        return true
    end,

    affectItem = function(self, water, item)
        if item:isIdentified() then
            return nil
        end
        item:identify()
        RPD.Badges:validateItemLevelAcquired(item)
        self:identifyAt(water)
        return item
    end,
}
