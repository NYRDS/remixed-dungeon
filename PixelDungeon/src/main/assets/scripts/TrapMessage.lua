--
-- User: mike
-- Date: 04.11.2017
-- Time: 22:26
-- This file is part of Remixed Pixel Dungeon.
--
GameScene = luajava.bindClass("com.watabou.pixeldungeon.scenes.GameScene")
local data

function setData(_data)
    data = _data
end

function trap(cell, char)
    wnd = luajava.newInstance("com.watabou.pixeldungeon.windows.WndMessage",data)
    GameScene:show(wnd)
end

