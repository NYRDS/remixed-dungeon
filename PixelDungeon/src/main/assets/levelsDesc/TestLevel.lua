--
-- User: mike
-- Date: 17.06.2017
-- Time: 0:18
-- This fle is part of Remixed Pixel Dungeon
--

function getJson()
    local filename

    if math.random() > 0.5 then
        filename = "levelsDesc/FortuneShop.json"
    else
        filename = "levelsDesc/LevelPortal.json"
    end

    return loadResource(filename)
end
