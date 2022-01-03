--
-- User: mike
-- Date: 07.11.2017
-- Time: 0:13
-- This file is part of Remixed Pixel Dungeon.
--

local json = require("scripts/lib/json")

local GLog  = luajava.bindClass("com.watabou.pixeldungeon.utils.GLog")

local RemixedDungeon = luajava.bindClass("com.nyrds.platform.game.RemixedDungeon")

local GameLoop = luajava.bindClass("com.nyrds.pixeldungeon.game.GameLoop")

local DungeonGenerator = luajava.bindClass("com.nyrds.pixeldungeon.utils.DungeonGenerator")
local PathFinder       = luajava.bindClass("com.watabou.utils.PathFinder")

local Sample           = luajava.bindClass("com.nyrds.platform.audio.Sample")
local Music            = luajava.bindClass("com.nyrds.platform.audio.Music")
local StringsManager   = luajava.bindClass("com.nyrds.platform.util.StringsManager")
local CharUtils        = luajava.bindClass("com.watabou.pixeldungeon.actors.CharUtils")

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
    Blessed      = luajava.bindClass("com.watabou.pixeldungeon.actors.buffs.Blessed"),
    MindVision   = luajava.bindClass("com.watabou.pixeldungeon.actors.buffs.MindVision"),
    Necrotism    = luajava.bindClass("com.nyrds.pixeldungeon.mechanics.buffs.Necrotism"),
    RageBuff     = luajava.bindClass("com.nyrds.pixeldungeon.mechanics.buffs.RageBuff"),
    Terror       = luajava.bindClass("com.watabou.pixeldungeon.actors.buffs.Terror"),
    Amok         = luajava.bindClass("com.watabou.pixeldungeon.actors.buffs.Amok"),
    Awareness    = luajava.bindClass("com.watabou.pixeldungeon.actors.buffs.Awareness"),
    Barkskin     = luajava.bindClass("com.watabou.pixeldungeon.actors.buffs.Barkskin"),
    Sleep        = luajava.bindClass("com.watabou.pixeldungeon.actors.buffs.Sleep"),
    Slow         = luajava.bindClass("com.watabou.pixeldungeon.actors.buffs.Slow"),
    Blindness    = luajava.bindClass("com.watabou.pixeldungeon.actors.buffs.Blindness")
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
    Regrowth = luajava.bindClass("com.watabou.pixeldungeon.actors.blobs.Regrowth"),
    WaterOfHealth = luajava.bindClass("com.watabou.pixeldungeon.actors.blobs.WaterOfHealth"),
    WaterOfTransmutation = luajava.bindClass("com.watabou.pixeldungeon.actors.blobs.WaterOfTransmutation"),
    WaterOfAwareness = luajava.bindClass("com.watabou.pixeldungeon.actors.blobs.WaterOfAwareness"),
    Alchemy = luajava.bindClass("com.watabou.pixeldungeon.actors.blobs.Alchemy")
}

local PseudoBlobs = {
    Freezing = luajava.bindClass("com.watabou.pixeldungeon.actors.blobs.Freezing")
}

local Ballistica = luajava.bindClass("com.watabou.pixeldungeon.mechanics.Ballistica")
local Challenges = luajava.bindClass("com.watabou.pixeldungeon.Challenges")

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
    equip ="EquipableItem_ACEquip",
    throw ="Item_ACThrow",
    drop ="Item_ACDrop",
    ch_steal = "CharAction_Steal",
    ch_taunt = "CharAction_Taunt",
    ch_push = "CharAction_Push",
    ch_order = "CharAction_Order",
    ch_hit = "CharAction_Hit"
}

local Bundle           = "com.watabou.utils.Bundle"

local Objects = {
    Ui = {
        WndMessage     = "com.watabou.pixeldungeon.windows.WndMessage",
        WndStory       = "com.watabou.pixeldungeon.windows.WndStory",
        WndQuest       = "com.watabou.pixeldungeon.windows.WndQuest",
        WndOptionsLua  = "com.nyrds.pixeldungeon.windows.WndOptionsLua",
        WndShopOptions = "com.nyrds.pixeldungeon.windows.WndShopOptions",
        WndChooseWay   = "com.watabou.pixeldungeon.windows.WndChooseWay"
    },

    Actors = {
        ScriptedActor = "com.nyrds.pixeldungeon.mechanics.actors.ScriptedActor"
    },
}

local GameScene = luajava.bindClass("com.watabou.pixeldungeon.scenes.GameScene")
local Dungeon   = luajava.bindClass("com.watabou.pixeldungeon.Dungeon")
local Camera    = luajava.bindClass("com.watabou.noosa.Camera")

local MobAi = luajava.bindClass("com.nyrds.pixeldungeon.ai.MobAi")

local Position = "com.nyrds.pixeldungeon.utils.Position"

local CharsList = luajava.newInstance("com.nyrds.pixeldungeon.utils.CharsList")


local wandOfBlink = luajava.newInstance("com.watabou.pixeldungeon.items.wands.WandOfBlink")
local wandOfTelekinesis = luajava.newInstance("com.watabou.pixeldungeon.items.wands.WandOfTelekinesis")
local wandOfFirebolt = luajava.newInstance("com.watabou.pixeldungeon.items.wands.WandOfFirebolt")

local Wands = {
    wandOfBlink = wandOfBlink,
    wandOfTelekinesis = wandOfTelekinesis,
    wandOfFirebolt = wandOfFirebolt
}

local ItemFactory     = luajava.bindClass("com.nyrds.pixeldungeon.items.common.ItemFactory")
local MobFactory      = luajava.bindClass("com.nyrds.pixeldungeon.mobs.common.MobFactory")
local EffectsFactory  = luajava.bindClass("com.nyrds.pixeldungeon.effects.EffectsFactory")
local LevelObjectsFactory  = luajava.bindClass("com.nyrds.pixeldungeon.levels.objects.LevelObjectsFactory")
local SpellFactory = luajava.bindClass("com.nyrds.pixeldungeon.mechanics.spells.SpellFactory")

local Effects = luajava.bindClass("com.watabou.pixeldungeon.effects.Effects")

local Tweeners = {
    PosTweener  = luajava.bindClass("com.watabou.noosa.tweeners.PosTweener"),
    JumpTweener = luajava.bindClass("com.watabou.noosa.tweeners.JumpTweener")
}

local Sfx = {
    CellEmitter = luajava.bindClass("com.watabou.pixeldungeon.effects.CellEmitter"),
    BlobEmitter = luajava.bindClass("com.watabou.pixeldungeon.effects.BlobEmitter"),
    Emitter = luajava.bindClass("com.watabou.noosa.particles.Emitter"),
    FlameParticle = luajava.bindClass("com.watabou.pixeldungeon.effects.particles.FlameParticle"),
    SnowParticle = luajava.bindClass("com.watabou.pixeldungeon.effects.particles.SnowParticle"),
    ShaftParticle = luajava.bindClass("com.watabou.pixeldungeon.effects.particles.ShaftParticle"),
    Speck = luajava.bindClass("com.watabou.pixeldungeon.effects.Speck"),
    ShadowParticle = luajava.bindClass("com.watabou.pixeldungeon.effects.particles.ShadowParticle"),
    SpellSprite = luajava.bindClass("com.watabou.pixeldungeon.effects.SpellSprite"),
    DarknessParticle = luajava.bindClass("com.watabou.pixeldungeon.effects.particles.DarknessParticle"),
    EarthParticle = luajava.bindClass("com.watabou.pixeldungeon.effects.particles.EarthParticle"),
    EnergyParticle = luajava.bindClass("com.watabou.pixeldungeon.effects.particles.EnergyParticle"),
    FlowParticle = luajava.bindClass("com.watabou.pixeldungeon.effects.particles.FlowParticle"),
    LeafParticle = luajava.bindClass("com.watabou.pixeldungeon.effects.particles.LeafParticle"),
    PoisonParticle = luajava.bindClass("com.watabou.pixeldungeon.effects.particles.PoisonParticle"),
    PurpleParticle = luajava.bindClass("com.watabou.pixeldungeon.effects.particles.PurpleParticle"),
    SparkParticle = luajava.bindClass("com.watabou.pixeldungeon.effects.particles.SparkParticle"),
    WebParticle = luajava.bindClass("com.watabou.pixeldungeon.effects.particles.WebParticle"),
    WindParticle = luajava.bindClass("com.watabou.pixeldungeon.effects.particles.WindParticle"),
    WoolParticle = luajava.bindClass("com.watabou.pixeldungeon.effects.particles.WoolParticle"),
    ElmoParticle = luajava.bindClass("com.watabou.pixeldungeon.effects.particles.ElmoParticle"),
    MagicMissile = luajava.bindClass("com.watabou.pixeldungeon.effects.MagicMissile"),
    SpellSprite = luajava.bindClass("com.watabou.pixeldungeon.effects.SpellSprite"),
    DeathStroke= luajava.bindClass("com.nyrds.pixeldungeon.effects.DeathStroke"),
    Wound = luajava.bindClass("com.watabou.pixeldungeon.effects.Wound"),
    Flare = luajava.bindClass("com.watabou.pixeldungeon.effects.Flare")
}

local Badges = luajava.bindClass("com.watabou.pixeldungeon.Badges")
local ItemUtils = luajava.bindClass("com.nyrds.pixeldungeon.items.ItemUtils")

local RPD = {
    RemixedDungeon = RemixedDungeon,
    GameLoop = GameLoop,
    GameScene = GameScene,
    Dungeon = Dungeon,
    DungeonGenerator = DungeonGenerator,
    PathFinder = PathFinder,
    Badges = Badges,
    Effects = Effects,
    ItemUtils = ItemUtils,
    DungeonTilemap = luajava.bindClass("com.watabou.pixeldungeon.DungeonTilemap"),

    CharsList = CharsList,
    CharUtils = CharUtils,
    Utils = luajava.bindClass("com.nyrds.lua.LuaUtils"),

    System = {
        Input = luajava.bindClass("com.nyrds.platform.app.Input")
    },

    Slots = {
        none         = "NONE",
        weapon       = "WEAPON",
        armor        = "ARMOR",
        leftHand     = "LEFT_HAND",
        artifact     = "ARTIFACT",
        leftArtifact = "LEFT_ARTIFACT"
    },

    SystemTime = luajava.bindClass("com.watabou.utils.SystemTime"),
    Terrain = luajava.bindClass("com.watabou.pixeldungeon.levels.Terrain"),
    Actor = luajava.bindClass("com.watabou.pixeldungeon.actors.Actor"),
    MobFactory = MobFactory,
    ItemFactory = ItemFactory,
    EffectsFactory = EffectsFactory,
    SpellFactory = SpellFactory,
    Journal = luajava.bindClass("com.watabou.pixeldungeon.Journal"),
    Chasm = luajava.bindClass("com.watabou.pixeldungeon.levels.features.Chasm"),
    Mob   = luajava.bindClass("com.watabou.pixeldungeon.actors.mobs.Mob"),
    Heap  = luajava.bindClass("com.watabou.pixeldungeon.items.Heap"),

    Ballistica = Ballistica,
    Challenges = Challenges,

    GLog = GLog,
    MobAi = MobAi,

    Buffs = Buffs,
    BuffIndicator = luajava.bindClass("com.watabou.pixeldungeon.ui.BuffIndicator"),

    Actions = actions,

    Blobs = Blobs,
    PseudoBlobs = PseudoBlobs,

    Tweeners = Tweeners,

    Sfx = Sfx,

    Objects = Objects,

    Wands = Wands,

    new = function(class, ...)
        return luajava.newInstance(class, ...)
    end,

    affectBuff = function (chr, buffClass, duration)
        return Buffs.Buff:affect(chr, buffClass, duration)
    end,

    permanentBuff = function (chr, buffClass)
        return Buffs.Buff:permanent(chr, buffClass)
    end,

    removeBuff = function (chr, buffClass)
        Buffs.Buff:detach(chr, buffClass)
    end,

    placePseudoBlob = function (blobClass, cell)
        blobClass:affect(cell)
    end,

    ---@param cell number
    ---@param amount number
    placeBlob = function (blobClass, cell, amount, level)
        level = level or Dungeon.level

        if not Dungeon.level then
            Blobs.Blob:seed(level, cell, amount, blobClass )
        else
            GameScene:add( Blobs.Blob:seed(level, cell, amount, blobClass ) )
        end
    end,

    playSound = function(sound)
        Sample.INSTANCE:play(sound)
    end,

    playMusic = function(music, looped)
        Music.INSTANCE:play(music, looped)
    end,

    stopMusic = function()
        Music.INSTANCE:stop()
    end,

    textById = function(id)
        return StringsManager:getVar(id)
    end,

    glog = function (text,...)
        GLog:i(tostring(text),{...})
    end,

    glogp = function (text,...)
        GLog:p(tostring(text),{...})
    end,

    glogn = function (text,...)
        GLog:n(tostring(text),{...})
    end,

    debug = function (text,...)
        GLog:toFile(tostring(text),{...})
        --GLog:n(tostring(text),{...})
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

    teleportTo = function(levelId, x, y)
        local position = luajava.newInstance(Position,levelId, x, y)
        Dungeon.hero:teleportTo(position)
    end,

    ---@param handler function
    chooseOption = function(handler, title, text, ...)
        assert(type(handler)=='function', "chooseOption handler must be a function")

        local wnd = luajava.newInstance(Objects.Ui.WndOptionsLua, handler, title, text, {...})
        GameScene:show(wnd)
    end,

    showStoryWindow = function(story_id)
        local wnd = luajava.newInstance(Objects.Ui.WndStory,story_id)
        GameScene:show(wnd)
    end,

    showTradeWindow = function(shopkeeper,client)
        local wnd = luajava.newInstance(Objects.Ui.WndShopOptions, shopkeeper, client )
        GameScene:show(wnd)
    end,

    showBuyWindow = function(shopkeeper,client)
        local wnd = luajava.newInstance(Objects.Ui.WndShopOptions, shopkeeper, client )
        wnd: showBuyWnd()
    end,

    showSellWindow = function(shopkeeper,client)
        local wnd = luajava.newInstance(Objects.Ui.WndShopOptions, shopkeeper, client )
        wnd: showSellWnd()
    end,


    zapEffect = function (from, to, zapEffect)
        GameScene:zapEffect(from, to, zapEffect)
    end,

    topEffect = function(cell,effectName)
        return GameScene:clipEffect(cell,1,effectName)
    end,

    objectEffect = function(cell,effectName)
        return GameScene:clipEffect(cell,2,effectName)
    end,

    bottomEffect = function(cell,effectName)
        return GameScene:clipEffect(cell,0,effectName)
    end,

    speckEffectFactory = function (particleType, evolutionType)
        return Sfx.Speck:factory(particleType, evolutionType)
    end,

    pourSpeck = function (emitter, factory, interval)
        emitter:pour(factory, interval)
        emitter.autoKill = false
    end,

    attachMoveTweener = function(img,dx,dy,time)
        Tweeners.PosTweener:attachTo(img,dx,dy,time)
    end,

    attachJumpTweener = function(chr, target, height, time)
        Tweeners.JumpTweener:attachTo(chr:getSprite(), target, height, time)
    end,

    item = function(itemClass, quantity)
        quantity = quantity or 1
        local item = ItemFactory:itemByName(itemClass)
        item:quantity(quantity)
        return item
    end,

    createItem = function(itemClass, itemDesc)
        local item = ItemFactory:createItem(itemClass, json.encode(itemDesc or {_=""}))
        assert(item, "can't create item "..itemClass)
        return item
    end,

    spawnMob = function(mobClass, cell, mobDesc)
        local mob = MobFactory:createMob(mobClass, json.encode(mobDesc or {_=""}))
        mob:setPos(cell)
        assert(mob, "can't spawn mob "..mobClass)
        Dungeon.level:spawnMob(mob)
        return mob
    end,

    levelObject = function(objectClass, cell)
        local object = LevelObjectsFactory:objectByName(objectClass)
        assert(object, "can't create object "..objectClass)
        object:setPos(cell)
        Dungeon.level:addLevelObject(object)
        return object
    end,

    createLevelObject = function(desc, cell)
        local level = Dungeon.level

        if not level:cellValid(cell) then
            local error_msg = "Trying to create %s on invalid cell %d"
            GLog:toFile(error_msg,{desc, cell})
            GLog:n(error_msg,{desc, cell})

            return nil
        end

        desc.x = Dungeon.level:cellX(cell)
        desc.y = Dungeon.level:cellY(cell)

        local object = LevelObjectsFactory:createObject(Dungeon.level, json.encode(desc))

        Dungeon.level:addLevelObject(object)
        return object
    end,

    forCellsAround = function(cell, action)
        local level = Dungeon.level

        local x = level:cellX(cell)
        local y = level:cellY(cell)

        for i = x - 1, x + 1 do
            for j = y - 1, y + 1 do
                if (i~=x or j~=y) and level:cellValid(i,j) then
                    action(level:cell(i,j))
                end
            end
        end
    end,

    forEachCellOnRay = function(from,to,magic,hitChars,hitObjects, action )
        GLog:i(tostring(from) .." " ..tostring(to).." "..tostring(magic).." "..tostring(hitChars).." "..tostring(hitObjects).." "..tostring(action),{""})
        local tgt = Ballistica:cast(from, to, magic, hitChars, hitObjects)

        for i=2, Ballistica.trace.length do
            local cell = Ballistica.trace[i]
            action(cell)
            if cell == tgt then
                break
            end
        end

        return tgt
    end,


    shakeCamera = function(time, power)
        Camera.main:shake(time, power)
    end,

    checkBadge = function(badgeName)
        return Badges:isUnlocked(Badges.Badge:valueOf(badgeName))
    end,

    packEntity = function(entity)
        local bundle = luajava.newInstance(Bundle)
        bundle:putEntity("entity",entity)
        return bundle:serialize()
    end,

    unpackEntity = function(str)
        return luajava.newInstance(Bundle,str):get("entity")
    end,

    toLua = function(entity)
        local bundle = luajava.newInstance(Bundle)
        bundle:putEntity("entity",entity)
        return json.decode(bundle:serialize())["entity"]
    end,

    fromLua = function(entityDesc)
        local jsonDesc = json.encode({entity = entityDesc})
        return luajava.newInstance(Bundle, jsonDesc):get("entity")
    end,

    format = function(fmt, ...)
        local args, order = {...}, {}

        fmt = StringsManager:maybeId(fmt)

        fmt = fmt:gsub('%%(%d+)%$', function(i)
            table.insert(order, args[tonumber(i)])
            return '%'
        end)


        if #order > 0 then
            return string.format(fmt, table.unpack(order))
        else
            return string.format(fmt, table.unpack(args))
        end
    end
}

RPD.creteItem = RPD.createItem -- for old Epic

return RPD





