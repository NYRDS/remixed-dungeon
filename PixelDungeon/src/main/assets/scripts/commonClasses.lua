--
-- User: mike
-- Date: 07.11.2017
-- Time: 0:13
-- This file is part of Remixed Pixel Dungeon.
--

RPD = {
    GameScene = luajava.bindClass("com.watabou.pixeldungeon.scenes.GameScene"),
    Dungeon = luajava.bindClass("com.watabou.pixeldungeon.Dungeon"),
    Terrain = luajava.bindClass("com.watabou.pixeldungeon.levels.Terrain"),
    Actor = luajava.bindClass("com.watabou.pixeldungeon.actors.Actor"),
    Blobs = {
        Blob      = luajava.bindClass("com.watabou.pixeldungeon.actors.blobs.Blob"),
        Fire      = luajava.bindClass("com.watabou.pixeldungeon.actors.blobs.Fire"),
        Foliage   = luajava.bindClass("com.watabou.pixeldungeon.actors.blobs.Foliage")
    },
    Sfx = {
        CellEmitter   = luajava.bindClass("com.watabou.pixeldungeon.effects.CellEmitter"),
        Emitter       = luajava.bindClass("com.watabou.noosa.particles.Emitter"),
        FlameParticle = luajava.bindClass("com.watabou.pixeldungeon.effects.particles.FlameParticle")
    }
}

return RPD





