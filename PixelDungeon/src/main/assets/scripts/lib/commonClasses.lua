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
    MobFactory = luajava.bindClass("com.nyrds.pixeldungeon.mobs.common.MobFactory"),
    ItemFactory = luajava.bindClass("com.nyrds.pixeldungeon.items.common.ItemFactory"),
    Journal = luajava.bindClass("com.watabou.pixeldungeon.Journal"),
    Chasm = luajava.bindClass("com.watabou.pixeldungeon.levels.features.Chasm"),

    Blobs = {
        Blob = luajava.bindClass("com.watabou.pixeldungeon.actors.blobs.Blob"),
        Fire = luajava.bindClass("com.watabou.pixeldungeon.actors.blobs.Fire"),
        Foliage = luajava.bindClass("com.watabou.pixeldungeon.actors.blobs.Foliage"),
        ConfusionGas = luajava.bindClass("com.watabou.pixeldungeon.actors.blobs.ConfusionGas"),
        LiquidFlame = luajava.bindClass("com.watabou.pixeldungeon.actors.blobs.LiquidFlame"),
        ParalyticGas = luajava.bindClass("com.watabou.pixeldungeon.actors.blobs.ParalyticGas"),
        Darkness = luajava.bindClass("com.watabou.pixeldungeon.actors.blobs.Darkness"),
        Web = luajava.bindClass("com.watabou.pixeldungeon.actors.blobs.Web"),
        ToxicGas = luajava.bindClass("com.watabou.pixeldungeon.actors.blobs.ToxicGas"),
        Regrowth = luajava.bindClass("com.watabou.pixeldungeon.actors.blobs.Regrowth")
    },

    Sfx = {
        CellEmitter = luajava.bindClass("com.watabou.pixeldungeon.effects.CellEmitter"),
        Emitter = luajava.bindClass("com.watabou.noosa.particles.Emitter"),
        FlameParticle = luajava.bindClass("com.watabou.pixeldungeon.effects.particles.FlameParticle"),
        SnowParticle = luajava.bindClass("com.watabou.pixeldungeon.effects.particles.SnowParticle")
    },

    Objects = {
        Ui = {
            WndMessage = "com.watabou.pixeldungeon.windows.WndMessage",
            WndStory = "com.watabou.pixeldungeon.windows.WndStory"
        },
        Actors = {
            ScriptedActor = "com.nyrds.pixeldungeon.mechanics.actors.ScriptedActor"
        },
    },

    new = function(class, ...)
        return luajava.newInstance(class, ...)
    end,

    placeBlob = function (blobClass, cell, amount)
        RPD.GameScene:add( RPD.Blobs.Blob:seed(cell, amount , blobClass ) );
    end
}



return RPD





