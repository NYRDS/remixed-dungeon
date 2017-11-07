--
-- User: mike
-- Date: 07.11.2017
-- Time: 0:13
-- This file is part of Remixed Pixel Dungeon.
--

RPD = {
    GameScene = luajava.bindClass("com.watabou.pixeldungeon.scenes.GameScene"),
    Dungeon = luajava.bindClass("com.watabou.pixeldungeon.Dungeon"),
    Actor = luajava.bindClass("com.watabou.pixeldungeon.actors.Actor"),
    Blobs = {
        Blob      = luajava.bindClass("com.watabou.pixeldungeon.actors.blobs.Blob"),
        Fire      = luajava.bindClass("com.watabou.pixeldungeon.actors.blobs.Fire"),
        Foliage   = luajava.bindClass("com.watabou.pixeldungeon.actors.blobs.Foliage")
    }
}

return RPD





