--
-- User: mike
-- Date: 17.06.2017
-- Time: 0:18
-- This fle is part of Remixed Pixel Dungeon
--

sys = luajava.bindClass('java.lang.System')

function getJson()
    local ModdingMode = luajava.bindClass('com.nyrds.android.util.ModdingMode')
    local filename
    if math.random() > 0.5 then
        filename = "levelsDesc/FortuneShop.json"
    else
        filename = "levelsDesc/Town.json"
    end

    return ModdingMode.getResource(filename)
end
