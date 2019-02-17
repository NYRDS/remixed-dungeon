--
-- User: mike
-- Date: 07.11.2017
-- Time: 0:13
-- This file is part of Remixed Pixel Dungeon.
--

local GLog  = luajava.bindClass("com.watabou.pixeldungeon.utils.GLog")


local RemixedDungeon = luajava.bindClass("com.watabou.pixeldungeon.RemixedDungeon")

local Sample = luajava.bindClass("com.watabou.noosa.audio.Sample")
local StringsManager   = luajava.bindClass("com.watabou.noosa.StringsManager")

local Buffs  = {
    Buff         = luajava.bindClass("com.watabou.pixeldungeon.actors.buffs.Buff"),
    Roots        = luajava.bindClass("com.watabou.pixeldungeon.actors.buffs.Roots"),
    Paralysis    = luajava.bindClass("com.watabou.pixeldungeon.actors.buffs.Paralysis"),
    Vertigo      = luajava.bindClass("com.watabou.pixeldungeon.actors.buffs.Vertigo"),
    Invisibility = luajava.bindClass("com.watabou.pixeldungeon.actors.buffs.Invisibility"),
    Levitation   = luajava.bindClass("com.watabou.pixeldungeon.actors.buffs.Levitation"),
    Hunger       = luajava.bindClass("com.watabou.pixeldungeon.actors.buffs.Hunger"),
    Poison       = luajava.bindClass("com.watabou.pixeldungeon.actors.buffs.Poison"),
    Frost        = luajava.bindClass("com.watabou.pixeldungeon.actors.buffs.Frost"),
    Light        = luajava.bindClass("com.watabou.pixeldungeon.actors.buffs.Light"),
    Cripple      = luajava.bindClass("com.watabou.pixeldungeon.actors.buffs.Cripple"),
    Charm        = luajava.bindClass("com.watabou.pixeldungeon.actors.buffs.Charm"),
    Blessed      = luajava.bindClass("com.watabou.pixeldungeon.actors.buffs.Blessed")
}

local Blobs = {
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
}

local PseudoBlobs = {
    Freezing = luajava.bindClass("com.watabou.pixeldungeon.actors.blobs.Freezing")
}

local actions = {
    eat = "Food_ACEat",
    light = "Torch_ACLight",
    apply = "Weightstone_ACApply",
    mine = "Pickaxe_ACMine",
    plant = "Plant_ACPlant",
    reforge = "ShortSword_ACReforge",
    zap = "Wand_ACZap",
    read = "Scroll_ACRead",
    drink = "Drink_ACDrink",
    equip ="EquipableItem_ACEquip"
}

local Objects = {
    Ui = {
        WndMessage    = "com.watabou.pixeldungeon.windows.WndMessage",
        WndStory      = "com.watabou.pixeldungeon.windows.WndStory",
        WndQuest      = "com.watabou.pixeldungeon.windows.WndQuest",
        WndOptionsLua = "com.nyrds.pixeldungeon.windows.WndOptionsLua"
    },

    Actors = {
        ScriptedActor = "com.nyrds.pixeldungeon.mechanics.actors.ScriptedActor"
    },
}

local GameScene = luajava.bindClass("com.watabou.pixeldungeon.scenes.GameScene")
local Dungeon   = luajava.bindClass("com.watabou.pixeldungeon.Dungeon")

local MobAi = luajava.bindClass("com.nyrds.pixeldungeon.ai.MobAi")


local wandOfBlink = luajava.newInstance("com.watabou.pixeldungeon.items.wands.WandOfBlink")
local wandOfTelekinesis = luajava.newInstance("com.watabou.pixeldungeon.items.wands.WandOfTelekinesis")
local wandOfFirebolt = luajava.newInstance("com.watabou.pixeldungeon.items.wands.WandOfFirebolt")

local Wands = {
    wandOfBlink = wandOfBlink,
    wandOfTelekinesis = wandOfTelekinesis,
    wandOfFirebolt = wandOfFirebolt
}

local ItemFactory = luajava.bindClass("com.nyrds.pixeldungeon.items.common.ItemFactory")
local MobFactory  = luajava.bindClass("com.nyrds.pixeldungeon.mobs.common.MobFactory")

local RPD = {
    RemixedDungeon = RemixedDungeon,
    GameScene = GameScene,
    Dungeon = Dungeon,
    SystemTime = luajava.bindClass("com.watabou.utils.SystemTime"),
    Terrain = luajava.bindClass("com.watabou.pixeldungeon.levels.Terrain"),
    Actor = luajava.bindClass("com.watabou.pixeldungeon.actors.Actor"),
    MobFactory = MobFactory,
    ItemFactory = ItemFactory,
    Journal = luajava.bindClass("com.watabou.pixeldungeon.Journal"),
    Chasm = luajava.bindClass("com.watabou.pixeldungeon.levels.features.Chasm"),
    Mob   = luajava.bindClass("com.watabou.pixeldungeon.actors.mobs.Mob"),
    Heap  = luajava.bindClass("com.watabou.pixeldungeon.items.Heap"),

    GLog = GLog,
    MobAi = MobAi,

    Buffs = Buffs,

    Actions = actions,

    Blobs = Blobs,
    PseudoBlobs = PseudoBlobs,

    Sfx = {
        CellEmitter = luajava.bindClass("com.watabou.pixeldungeon.effects.CellEmitter"),
        Emitter = luajava.bindClass("com.watabou.noosa.particles.Emitter"),
        FlameParticle = luajava.bindClass("com.watabou.pixeldungeon.effects.particles.FlameParticle"),
        SnowParticle = luajava.bindClass("com.watabou.pixeldungeon.effects.particles.SnowParticle"),
        ShaftParticle = luajava.bindClass("com.watabou.pixeldungeon.effects.particles.ShaftParticle"),
        Speck = luajava.bindClass("com.watabou.pixeldungeon.effects.Speck"),
        ShadowParticle = luajava.bindClass("com.watabou.pixeldungeon.effects.particles.ShadowParticle"),
        SpellSprite = luajava.bindClass("com.watabou.pixeldungeon.effects.SpellSprite"),
    },

    Objects = Objects,

    Wands = Wands,

    new = function(class, ...)
        return luajava.newInstance(class, ...)
    end,

    affectBuff = function (chr, buffClass, duration)
        Buffs.Buff:affect(chr, buffClass, duration)
    end,

    permanentBuff = function (chr, buffClass)
        Buffs.Buff:permanent(chr, buffClass)
    end,

    removeBuff = function (chr, buffClass)
        Buffs.Buff:detach(chr, buffClass)
    end,

    placePseudoBlob = function (blobClass, cell)
        blobClass:affect(cell)
    end,

    placeBlob = function (blobClass, cell, amount)
        GameScene:add( Blobs.Blob:seed(cell, amount, blobClass ) )
    end,

    playSound = function(sound)
        Sample.INSTANCE:play(sound)
    end,

    textById = function(id)
        return StringsManager:getVar(id)
    end,

    glog = function (text,...)
        GLog:i(text,{...})
    end,

    glogp = function (text,...)
        GLog:p(text,{...})
    end,

    glogn = function (text,...)
        GLog:n(text,{...})
    end,

    getXy = function (chr)
        local pos = chr:getPos()
        return {Dungeon.level:cellX(pos),Dungeon.level:cellY(pos)}
    end,

    setAi = function(mob, state)
        mob:setState(MobAi:getStateByTag(state))
    end,

    blinkTo = function(mob, target)
        wandOfBlink:mobWandUse(mob, target)
    end,

    chooseOption = function(handler, title, text, ...)
        local wnd = luajava.newInstance(Objects.Ui.WndOptionsLua, handler, title, text, {...})
        GameScene:show(wnd)
    end,

    showStoryWindow = function(story_id)
        local wnd = luajava.newInstance(Objects.Ui.WndStory,story_id)
        GameScene:show(wnd)
    end,

    zapEffect = function (from, to, zapEffect)
        GameScene:zapEffect(from, to, zapEffect)
    end,

    item = function(itemClass, quantity)
        quantity = quantity or 1
        local item = ItemFactory:itemByName(itemClass)
        item:quantity(quantity)
        return item
    end
}

return RPD





