--
-- User: mike
-- Date: 17.06.2017
-- Time: 0:18
-- This fle is part of Remixed Pixel Dungeon
--

sys = luajava.bindClass("java.lang.System")
print ("Yea! binding works!", sys:currentTimeMillis() )

Dungeon = luajava.bindClass("com.watabou.pixeldungeon.Dungeon")

print("Your class:",Dungeon.hero:className())

function getJson()
    local filename

    if math.random() > 0.5 then
        filename = "levelsDesc/FortuneShop.json"
    else
        filename = "levelsDesc/LevelPortal.json"
    end

    return loadResource(filename)
end
