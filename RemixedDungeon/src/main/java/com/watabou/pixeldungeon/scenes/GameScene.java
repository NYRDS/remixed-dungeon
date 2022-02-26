/*
 * Pixel Dungeon
 * Copyright (C) 2012-2014  Oleg Dolya
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */
package com.watabou.pixeldungeon.scenes;

import com.nyrds.LuaInterface;
import com.nyrds.pixeldungeon.effects.CustomClipEffect;
import com.nyrds.pixeldungeon.effects.EffectsFactory;
import com.nyrds.pixeldungeon.effects.ParticleEffect;
import com.nyrds.pixeldungeon.effects.ZapEffect;
import com.nyrds.pixeldungeon.game.GameLoop;
import com.nyrds.pixeldungeon.game.GamePreferences;
import com.nyrds.pixeldungeon.levels.LevelTools;
import com.nyrds.pixeldungeon.levels.TestLevel;
import com.nyrds.pixeldungeon.levels.objects.LevelObject;
import com.nyrds.pixeldungeon.levels.objects.sprites.LevelObjectSprite;
import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.pixeldungeon.utils.DungeonGenerator;
import com.nyrds.pixeldungeon.windows.WndHeroSpells;
import com.nyrds.platform.EventCollector;
import com.nyrds.platform.audio.Music;
import com.nyrds.platform.audio.Sample;
import com.nyrds.platform.game.Game;
import com.nyrds.platform.util.StringsManager;
import com.nyrds.platform.util.TrackedRuntimeException;
import com.nyrds.util.ModError;
import com.nyrds.util.ModdingMode;
import com.nyrds.util.Util;
import com.nyrds.util.WeakOptional;
import com.watabou.noosa.Camera;
import com.watabou.noosa.Gizmo;
import com.watabou.noosa.Group;
import com.watabou.noosa.Image;
import com.watabou.noosa.SkinnedBlock;
import com.watabou.noosa.Text;
import com.watabou.noosa.Visual;
import com.watabou.noosa.particles.DummyEmitter;
import com.watabou.noosa.particles.Emitter;
import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.Badges;
import com.watabou.pixeldungeon.ClassicDungeonTilemap;
import com.watabou.pixeldungeon.CustomLayerTilemap;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.DungeonTilemap;
import com.watabou.pixeldungeon.FogOfWar;
import com.watabou.pixeldungeon.Statistics;
import com.watabou.pixeldungeon.XyzDungeonTilemap;
import com.watabou.pixeldungeon.actors.Actor;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.blobs.Blob;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.actors.mobs.Mob;
import com.watabou.pixeldungeon.effects.BannerSprites;
import com.watabou.pixeldungeon.effects.BlobEmitter;
import com.watabou.pixeldungeon.effects.EmoIcon;
import com.watabou.pixeldungeon.effects.Flare;
import com.watabou.pixeldungeon.effects.FloatingText;
import com.watabou.pixeldungeon.effects.Ripple;
import com.watabou.pixeldungeon.effects.SpellSprite;
import com.watabou.pixeldungeon.effects.SystemFloatingText;
import com.watabou.pixeldungeon.items.Heap;
import com.watabou.pixeldungeon.items.Item;
import com.watabou.pixeldungeon.items.bags.Bag;
import com.watabou.pixeldungeon.items.wands.WandOfBlink;
import com.watabou.pixeldungeon.levels.Level;
import com.watabou.pixeldungeon.levels.RegularLevel;
import com.watabou.pixeldungeon.levels.TerrainFlags;
import com.watabou.pixeldungeon.levels.features.Chasm;
import com.watabou.pixeldungeon.sprites.CharSprite;
import com.watabou.pixeldungeon.sprites.DiscardedItemSprite;
import com.watabou.pixeldungeon.sprites.ItemSprite;
import com.watabou.pixeldungeon.ui.AttackIndicator;
import com.watabou.pixeldungeon.ui.Banner;
import com.watabou.pixeldungeon.ui.BusyIndicator;
import com.watabou.pixeldungeon.ui.GameLog;
import com.watabou.pixeldungeon.ui.HealthIndicator;
import com.watabou.pixeldungeon.ui.Icons;
import com.watabou.pixeldungeon.ui.QuickSlot;
import com.watabou.pixeldungeon.ui.ResumeIndicator;
import com.watabou.pixeldungeon.ui.StatusPane;
import com.watabou.pixeldungeon.ui.Toast;
import com.watabou.pixeldungeon.ui.Toolbar;
import com.watabou.pixeldungeon.ui.Window;
import com.watabou.pixeldungeon.utils.GLog;
import com.watabou.pixeldungeon.utils.Utils;
import com.watabou.pixeldungeon.windows.WndBag;
import com.watabou.pixeldungeon.windows.WndBag.Mode;
import com.watabou.pixeldungeon.windows.WndGame;
import com.watabou.pixeldungeon.windows.WndTitledMessage;
import com.watabou.utils.Random;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;

import lombok.var;

public class GameScene extends PixelScene {

    private static final float MAX_BRIGHTNESS = 1.22f;

    private static volatile GameScene scene;

    private SkinnedBlock water;

    private DungeonTilemap logicTiles;

    private DungeonTilemap baseTiles;

    @Nullable
    private DungeonTilemap doorTiles;

    @Nullable
    private DungeonTilemap roofTiles;

    private FogOfWar fog;

    private static CellSelector cellSelector;

    private Group ripples;
    private Group bottomEffects;
    private Group objects;
    private Group objectEffects;
    private Group heaps;
    private Group mobs;
    private Group emitters;
    private Group effects;
    private Group gases;
    private Group spells;
    private Group statuses;
    private Group emoicons;


    //ui elements
    private Toolbar         toolbar;
    private StatusPane      statusPane;
    private Toast           prompt;
    private AttackIndicator attack;
    private ResumeIndicator resume;
    private GameLog         log;
    private BusyIndicator   busy;

    private volatile boolean sceneCreated = false;
    private          float   waterSx      = 0, waterSy = -5;
    private boolean objectSortingRequested;


    public void updateUiCamera() {
        statusPane.setSize(uiCamera.width, 0);
        toolbar.setRect(0, uiCamera.height - toolbar.height(), uiCamera.width, toolbar.height());
        attack.setPos(uiCamera.width - attack.width(), toolbar.top() - attack.height());
        resume.setPos(uiCamera.width - resume.width(), attack.top() - resume.height());
        log.setRect(0, toolbar.top(), attack.left(), 0);
        busy.setX(1);
        busy.setY(statusPane.bottom() + 2);
    }


    static public void playLevelMusic() {
        if(Dungeon.level == null) {
            EventCollector.logException("attempt to play music on null level");
            return;
        }
        Music.INSTANCE.play(Dungeon.level.music(), true);
        Music.INSTANCE.volume(1f);
    }

    @Override
    public void create() {
        Level level = Dungeon.level;

        if(level==null) {
            throw new TrackedRuntimeException("Trying to create GameScene when level is nil!");
        }

        Hero hero = Dungeon.hero;

        if(hero==null) {
            throw new TrackedRuntimeException("Trying to create GameScene when hero is nil!");
        }

        createGameScene(level, hero);
    }

    public void createGameScene(@NotNull Level level, @NotNull Hero hero) {
        playLevelMusic();

        LevelTools.upgradeMap(level);
        GamePreferences.lastClass(hero.getHeroClass().classIndex());

        super.create();

        Camera.main.zoom((float) (defaultZoom + GamePreferences.zoom()));

        scene = this;

        Group terrain = new Group();
        add(terrain);

        water = new SkinnedBlock(level.getWidth() * DungeonTilemap.SIZE,
                level.getHeight() * DungeonTilemap.SIZE, level.getWaterTex());

        waterSx = DungeonGenerator.getLevelProperty(level.levelId, "waterSx", waterSx);
        waterSy = DungeonGenerator.getLevelProperty(level.levelId, "waterSy", waterSy);

        terrain.add(water);

        ripples = new Group();
        terrain.add(ripples);

        String logicTilesAtlas = level.getProperty("tiles_logic", null);

        if(logicTilesAtlas != null) {
			logicTiles = new ClassicDungeonTilemap(level,logicTilesAtlas);
			terrain.add(logicTiles);
		}

        if (!level.customTiles()) {
            baseTiles = DungeonTilemap.factory(level);

            if(baseTiles instanceof XyzDungeonTilemap) {
                doorTiles = ((XyzDungeonTilemap)baseTiles).doorTilemap();
                roofTiles = ((XyzDungeonTilemap)baseTiles).roofTilemap();
            }

        } else {
            CustomLayerTilemap tiles = new CustomLayerTilemap(level, Level.LayerId.Base);
            tiles.addLayer(Level.LayerId.Deco);
            tiles.addLayer(Level.LayerId.Deco2);
            baseTiles = tiles;

            tiles = new CustomLayerTilemap(level, Level.LayerId.Roof_Base);
            tiles.addLayer(Level.LayerId.Roof_Deco);
            tiles.setTransparent(true);
            roofTiles = tiles;
        }
        terrain.add(baseTiles);

        bottomEffects = new Group();
        add(bottomEffects);

        objects = new Group();
        add(objects);

        objectEffects = new Group();
        add(objectEffects);

        level.addVisuals(this);

        heaps = new Group();
        add(heaps);

        if(doorTiles!=null) {
            add(doorTiles);
        }

        for (Heap heap : level.allHeaps()) {
            addHeapSprite(heap);
        }

        emitters = new Group();
        effects = new Group();
        emoicons = new Group();

        mobs = new Group();
        add(mobs);

        // hack to save bugged saves...
        boolean buggedSave = false;
        HashSet<Mob> filteredMobs = new HashSet<>();
        for (Mob mob : level.mobs) {
            if (level.cellValid(mob.getPos())) {
                filteredMobs.add(mob);
            } else {
                buggedSave = true;
            }
        }

        if (buggedSave) {
            EventCollector.logException("bugged save mob.pos==-1");
        }

        level.mobs = filteredMobs;

        for (Mob mob : level.mobs) {
            if (Statistics.amuletObtained) {
                if(!mob.friendly(hero)) {
                    mob.beckon(hero.getPos());
                }
            }
            mob.regenSprite();
        }

        add(emitters);
        add(effects);

        gases = new Group();
        add(gases);

        for (Blob blob : level.blobs.values()) {
            blob.emitter = null;
            addBlobSprite(blob);
        }

        fog = new FogOfWar(level.getWidth(), level.getHeight());

        if (level.noFogOfWar()) {
            level.reveal();
        }

        spells = new Group();
        add(spells);

        if(roofTiles!=null) {
            add(roofTiles);
        }

        fog.updateVisibility(Dungeon.visible, level.visited, level.mapped, false);
        add(fog);

        statuses = new Group();
        add(statuses);

        add(emoicons);

        brightness(GamePreferences.brightness());

        add(new HealthIndicator());

        add(cellSelector = new CellSelector(baseTiles));

        statusPane = new StatusPane(hero, level);
        statusPane.camera = uiCamera;
        statusPane.setSize(uiCamera.width, 0);
        add(statusPane);

        toolbar = new Toolbar(hero);
        toolbar.camera = uiCamera;
        toolbar.setRect(0, uiCamera.height - toolbar.height(), uiCamera.width, toolbar.height());
        add(toolbar);

        attack = new AttackIndicator();
        attack.camera = uiCamera;
        attack.setPos(uiCamera.width - attack.width(), toolbar.top() - attack.height());
        add(attack);

        resume = new ResumeIndicator();
        resume.camera = uiCamera;
        resume.setPos(uiCamera.width - resume.width(), attack.top() - resume.height());
        add(resume);

        log = new GameLog();
        log.camera = uiCamera;
        log.setRect(0, toolbar.top(), attack.left(), 0);
        add(log);

        if (Dungeon.depth < Statistics.deepestFloor) {
            GLog.i(StringsManager.getVar(R.string.GameScene_WelcomeBack), Dungeon.depth);
        } else {
            GLog.i(StringsManager.getVar(R.string.GameScene_Welcome), Dungeon.depth);
            Sample.INSTANCE.play(Assets.SND_DESCEND);
        }
        switch (level.getFeeling()) {
            case CHASM:
                GLog.w(StringsManager.getVar(R.string.GameScene_Chasm));
                break;
            case WATER:
                GLog.w(StringsManager.getVar(R.string.GameScene_Water));
                break;
            case GRASS:
                GLog.w(StringsManager.getVar(R.string.GameScene_Grass));
                break;
            default:
        }

        if (level instanceof RegularLevel
                && ((RegularLevel) level).secretDoors > Random.IntRange(3, 4)) {
            GLog.w(StringsManager.getVar(R.string.GameScene_Secrets));
        }
        if (Dungeon.nightMode && !Dungeon.bossLevel()) {
            GLog.w(StringsManager.getVar(R.string.GameScene_NightMode));
        }

        busy = new BusyIndicator();
        busy.camera = uiCamera;
        busy.setX(1);
        busy.setY(statusPane.bottom() + 18);
        add(busy);

        sceneCreated = true;

        InterlevelScene.Mode appearMode = InterlevelScene.mode;
        InterlevelScene.mode = InterlevelScene.Mode.CONTINUE;

        hero.regenSprite();
        //it is a chance for another level change right here if level entrance is at CHASM for example
        switch (appearMode) {
            case RESURRECT:
                WandOfBlink.appear(hero, level.entrance);
                new Flare(8, 32).color(0xFFFF66, true).show(hero.getHeroSprite(), 2f);
                break;
            case RETURN:
                WandOfBlink.appear(hero, hero.getPos());
                break;
            case FALL:
                Chasm.heroLand();
                break;
            case DESCEND:

                DungeonGenerator.showStory(level);

                if (hero.isAlive() && !level.isSafe() && Dungeon.depth != 22 && Dungeon.depth != 1) {
                    Badges.validateNoKilling();
                }
                break;
            default:
        }

        Camera.main.target = hero.getHeroSprite();

        level.activateScripts();
        LevelTools.upgradeMap(level); // Epic level gen compatibility

        for (var lo: level.getAllLevelObjects()) {
            GLog.debug("creating lo: %s", lo.getEntityKind());

            if(lo.getEntityKind().contains("ShadowTile")) {
                GLog.debug("creating ShadowTile");
            }

            lo.lo_sprite.clear();
            addLevelObjectSprite(lo);
            lo.addedToScene();
        }

        fadeIn();

        Dungeon.observe();
        hero.updateSprite();
        hero.readyAndIdle();
        QuickSlot.refresh(hero);

        doSelfTest();

        final double moveTimeout = Dungeon.moveTimeout();
        final boolean realtime = Dungeon.realtime();

        if(realtime || moveTimeout < Double.POSITIVE_INFINITY) {

            String msg = "";
            if(realtime) {
                msg += StringsManager.getVar(R.string.WrnExperimental_realtime);
            } else {
                if (moveTimeout < Double.POSITIVE_INFINITY) {
                    msg += Utils.format(R.string.WrnExperimental_moveTimeout, (int)moveTimeout);
                }
            }

            msg += "\n\n";
            msg += StringsManager.getVar(R.string.WrnExperimental_hint);

            add(new WndTitledMessage(Icons.get(Icons.ALERT),
                    StringsManager.getVar(R.string.WrnExperimental_title),
                    msg
            ));
        }
    }

    private void doSelfTest() {
        final Level level = Dungeon.level;
        if(Util.isDebug()) {
            for (int i = 0; i< level.map.length; ++i) {
                level.tileDescByCell(i);
                level.tileNameByCell(i);
            }

            GLog.debug(Dungeon.hero.immunities().toString());
            //GLog.toFile(StringsManager.missingStrings.toString());

            if(!(level instanceof TestLevel) && !ModdingMode.inMod()) {
                for (var lo : level.getAllLevelObjects()) {
                    int pos = lo.getPos();
                    if (!TerrainFlags.is(level.map[pos],TerrainFlags.PASSABLE)) {
                        throw new ModError(Utils.format("%s on a non-passable cell %d (%d) . level %s", lo.getEntityKind(), pos, level.map[pos], level.levelId));
                    }

                    if (level.pit[pos]) {
                        throw new ModError(Utils.format("%s on a pit cell %d. level %s", lo.getEntityKind(), pos, level.levelId));
                    }
                }
            }
        }

        if(level instanceof TestLevel) {
            TestLevel testLevel = (TestLevel) level;
            testLevel.runEquipTest();
            testLevel.runMobsTest();
            testLevel.reset();
        }
    }

    public void destroy() {
        sceneCreated = false;
        scene = null;
        super.destroy();
    }

    @Override
    public synchronized void pause() {
        if(!Game.softPaused) {
            final Hero hero = Dungeon.hero;
            if(hero != null && hero.isAlive()) {
                Dungeon.save(false);
            }
        }
    }

    private static transient boolean observeRequested = false;

    public static void observeRequest() {
        observeRequested = true;
    }

    @Override
    public synchronized void update() {
        if (!sceneCreated) {
            return;
        }

        final Hero hero = Dungeon.hero;

        if (hero == null || hero.invalid()) {
            return;
        }

        if (Dungeon.isLoading()) {
            return;
        }

        if(!Dungeon.level.cellValid(hero.getPos())){
            return;
        }

        if(objectSortingRequested) {
            objects.sort();
        }
        super.update();

        water.offset(waterSx * GameLoop.elapsed, waterSy * GameLoop.elapsed);

        Actor.process(GameLoop.elapsed);

        if (hero.isReady() && !hero.paralysed) {
            log.newLine();
        }

        if(observeRequested) {
            observeRequested = false;
            Dungeon.observeImpl();
        }
    }

    @Override
    protected void onBackPressed() {
        if (!cancel()) {
            add(new WndGame());
        }
    }

    @Override
    protected void onMenuPressed() {
        if (Dungeon.hero.isReady()) {
            selectItem(Dungeon.hero, null, Mode.ALL, null);
        }
    }

    public void brightness(boolean value) {

        float levelLimit = Math.min(Dungeon.level.getProperty("maxBrightness", MAX_BRIGHTNESS),
                DungeonGenerator.getLevelProperty(Dungeon.level.levelId,"maxBrightness", MAX_BRIGHTNESS));


        float brightnessValue =  value ? Math.min(MAX_BRIGHTNESS, levelLimit)  : 1.0f;

        water.brightness(brightnessValue);
        baseTiles.brightness(brightnessValue);

        if (logicTiles != null) {
            logicTiles.brightness(brightnessValue);
        }

        if(roofTiles != null) {
            roofTiles.brightness(brightnessValue);
        }

        if (value) {
            fog.am = +2f;
            fog.aa = -1f;
        } else {
            fog.am = +1f;
            fog.aa = 0f;
        }
    }

    private void addLevelObjectSprite(@NotNull LevelObject obj) {
        obj.lo_sprite = WeakOptional.of( (LevelObjectSprite)objects.recycle(LevelObjectSprite.class) );
        obj.lo_sprite.ifPresent (sprite -> sprite.reset(obj));
        obj.addedToScene();
        objectSortingRequested = true;
    }

    private void addHeapSprite(@NotNull Heap heap) {
        ItemSprite sprite = heap.sprite = (ItemSprite) heaps.recycle(ItemSprite.class);
        sprite.revive();
        sprite.link(heap);
        sprite.setIsometricShift(true);
        heaps.add(sprite);
    }

    private void addDiscardedSprite(@NotNull Heap heap) {
        heap.sprite = (DiscardedItemSprite) heaps.recycle(DiscardedItemSprite.class);
        heap.sprite.setIsometricShift(true);
        heap.sprite.revive();
        heap.sprite.link(heap);
        heap.sprite.setIsometricShift(true);
        heaps.add(heap.sprite);
    }

    private static void addBlobSprite(final Blob gas) {
        if (isSceneReady())
            if (gas.emitter == null) {
                scene.gases.add(new BlobEmitter(gas));
            }
    }

    public void prompt(String text, Image icon) {

        if (prompt != null) {
            prompt.killAndErase();
            prompt = null;
        }

        if (text != null) {
            prompt = new Toast(text, icon) {
                @Override
                protected void onClose() {
                    cancel();
                }
            };
            prompt.camera = uiCamera;
            prompt.setPos((uiCamera.width - prompt.width()) / 2, uiCamera.height - 60);
            add(prompt);
        }
    }

    private void showBanner(Banner banner) {
        banner.camera = uiCamera;
        banner.setX(align(uiCamera, (uiCamera.width - banner.width) / 2));
        banner.setY(align(uiCamera, (uiCamera.height - banner.height) / 3));
        add(banner);
    }

    // -------------------------------------------------------

    public static void add(Blob gas) {
        if (isSceneReady()) {
            Actor.add(gas);
            addBlobSprite(gas);
        }
    }

    public static void add(LevelObject obj) {
        if (isSceneReady()) {
            scene.addLevelObjectSprite(obj);
        }
    }

    public static void add(Heap heap) {
        if (isSceneReady()) {
            scene.addHeapSprite(heap);
        }
    }

    public static void discard(Heap heap) {
        if (isSceneReady()) {
            scene.addDiscardedSprite(heap);
        }
    }

    public static boolean mayCreateSprites() {
        return scene != null;
    }

    public static boolean isSceneReady() {
        return scene != null && !Game.isPaused() && !Dungeon.isLoading();
    }

    public static void add(EmoIcon icon) {
        if (isSceneReady()) {
            scene.emoicons.add(icon);
        }
    }

    public static void effect(Visual effect) {
        if (isSceneReady()) {
            scene.effects.add(effect);
        }
    }

    public static void zapEffect(int from, int to, String zapEffect) {
        if (isSceneReady()) {
            ZapEffect.zap(scene.effects, from, to, zapEffect);
        }
    }


    @LuaInterface
    public static Group particleEffect(String effectName, int cell) {
        if (isSceneReady()) {
            Group effect = ParticleEffect.addToCell(effectName, cell);
            effect.setIsometricShift(Dungeon.isIsometricMode());
            scene.add(effect);
            return effect;
        }
        return new Group();
    }

    @LuaInterface
    public static CustomClipEffect clipEffect(int cell, int layer, String effectName) {

        CustomClipEffect effect = EffectsFactory.getEffectByName(effectName);
        effect.place(cell);
        if (isSceneReady()) {
            switch (layer) {
                case 0:
                    scene.bottomEffects.add(effect);
                    break;
                case 1:
                    scene.effects.add(effect);
                    break;
                case 2:
                    scene.objectEffects.add(effect);
                    break;
                default:
                    GLog.n("Bad layer %d for %s", layer, effectName);
            }
        }
        effect.setIsometricShift(true);
        effect.playAnimOnce();
        return effect;
    }

    public static Ripple ripple(int pos) {
        Ripple ripple = (Ripple) scene.ripples.recycle(Ripple.class);
        ripple.reset(pos);
        return ripple;
    }

    public static SpellSprite spellSprite() {
        return (SpellSprite) scene.spells.recycle(SpellSprite.class);
    }

    @NotNull
    public static Emitter emitter() {
        if (isSceneReady()) {
            Emitter emitter = (Emitter) scene.emitters.recycle(Emitter.class);
            emitter.revive();
            return emitter;
        } else {
            return new DummyEmitter();
        }
    }

    public static Text status() {
        if (ModdingMode.getClassicTextRenderingMode()) {
            return (FloatingText) scene.statuses.recycle(FloatingText.class);
        } else {
            return (SystemFloatingText) scene.statuses.recycle(SystemFloatingText.class);
        }
    }

    public static void pickUp(Item item) {
        if(isSceneReady()) {
            scene.toolbar.pickup(item);
        }
    }

    public static void updateMap() {
        if (isSceneReady()) {
            scene.baseTiles.updateAll();
        }
    }

    public static void updateMap(int cell) {
        if (isSceneReady()) {
            final Level level = Dungeon.level;
            if(level.cellValid(cell)) {
                scene.baseTiles.updateCell(cell, level);
            } else {
                EventCollector.logException(Utils.format("Attempt to update invalid %d on %s", cell, level.levelId));
            }
        }
    }

    public static void updateMapPair(int cell) {
        if (isSceneReady()) {
            final Level level = Dungeon.level;
            scene.baseTiles.updateCell(cell, level);
            final int cellN = cell - level.getWidth();
            if(level.cellValid(cellN)) {
                scene.baseTiles.updateCell(cellN, level);
            }
        }
    }

    public static void discoverTile(int pos) {
        if (isSceneReady()) {
            scene.baseTiles.discover(pos);
        }
    }

    public static void show(Window wnd) {
        cancelCellSelector();
        if (isSceneReady() && scene.sceneCreated) {
            scene.add(wnd);
        }
    }

    public static void afterObserve() {
        if (isSceneReady() && scene.sceneCreated) {

            final Level level = Dungeon.level;

            GameScene.updateMap();
            scene.baseTiles.updateFow(scene.fog);

            for (Mob mob : level.mobs) {
                mob.getSprite().setVisible(Dungeon.visible[mob.getPos()]);
            }
        }
    }

    public static void flash(int color) {
        if (isSceneReady()) {
            scene.fadeIn(0xFF000000 | color, true);
        }
    }

    public static void gameOver() {
        if (isSceneReady()) {

            Banner gameOver = new Banner(BannerSprites.get(BannerSprites.Type.GAME_OVER));
            gameOver.show(0x000000, 1f);
            scene.showBanner(gameOver);

            Sample.INSTANCE.play(Assets.SND_DEATH);
        }
    }

    public static void bossSlain() {
        if (isSceneReady() && Dungeon.hero.isAlive()) {
            Banner bossSlain = new Banner(BannerSprites.get(BannerSprites.Type.BOSS_SLAIN));
            bossSlain.show(0xFFFFFF, 0.3f, 5f);
            scene.showBanner(bossSlain);

            Sample.INSTANCE.play(Assets.SND_BOSS);
        }
    }

    @LuaInterface
    public static void handleCell(int cell) {
        if(isSceneReady()) {
            cellSelector.select(cell);
        }
    }

    public static void selectCell(CellSelector.Listener listener, Char selector) {

        if(listener == cellSelector.listener && selector == cellSelector.selector) {
            return;
        }

        if(isSceneReady()) {

            if (cellSelector != null && cellSelector.listener != null && cellSelector.listener != defaultCellListener) {
                cellSelector.listener.onSelect( null, cellSelector.selector);
            }

            cellSelector.listener = listener;
            cellSelector.selector = selector;

            scene.prompt(listener.prompt(), listener.icon());
            script.runOptional("selectCell");
        }
    }

    public static boolean cancelCellSelector() {
        if (cellSelector != null) {
            cellSelector.cancel();
            return true;
        } else {
            return false;
        }
    }

    public static WndBag selectItemFromBag(WndBag.Listener listener, Bag bag, Mode mode, String title) {
        cancelCellSelector();
        WndBag wnd = new WndBag(Dungeon.hero.getBelongings(), bag, listener, mode, title);
        scene.add(wnd);
        return wnd;
    }

    public static WndBag selectItem(Char selector, WndBag.Listener listener, Mode mode, String title) {
        cancelCellSelector();

        WndBag wnd = WndBag.lastBag(selector, listener, mode, title);
        scene.add(wnd);

        return wnd;
    }

    public static WndHeroSpells selectSpell(WndHeroSpells.Listener listener) {
        cancelCellSelector();

        WndHeroSpells wnd = new WndHeroSpells(listener);
        scene.add(wnd);

        return wnd;
    }

    static public boolean cancel() {
        Hero hero = Dungeon.hero;

        if(hero!=null) {
            hero.next();
            if (hero.curAction != null || hero.restoreHealth) {

                hero.curAction = null;
                hero.restoreHealth = false;
                return true;
            } else {
                return cancelCellSelector();
            }
        }
        return false;
    }

    public static void ready() {
        selectCell(defaultCellListener, Dungeon.hero);
        QuickSlot.cancel();
    }

    private static final CellSelector.Listener defaultCellListener = new DefaultCellListener();

    public void updateToolbar(boolean reset) {
        if (toolbar != null) {

            if(reset) {
                toolbar.destroy();

                toolbar = new Toolbar(Dungeon.hero);
                toolbar.camera = uiCamera;
            }

            toolbar.setRect(0, uiCamera.height - toolbar.height(), uiCamera.width, toolbar.height());
            add(toolbar);

            if(attack != null) {
                attack.camera = uiCamera;
                attack.setPos(uiCamera.width - attack.width(), toolbar.top() - attack.height());
                attack.update();
            }
            if(resume != null) {
                resume.camera = uiCamera;
                resume.setPos(uiCamera.width - resume.width(), attack.top() - resume.height());
                resume.update();
            }
        }
    }

    @Override
    public void resume() {
        super.resume();
        InterlevelScene.Do(InterlevelScene.Mode.CONTINUE);
    }

    public static void addMobSpriteDirect(CharSprite sprite) {
        if (isSceneReady()) {
            scene.mobs.add(sprite);
        }
    }

    public static void addToMobLayer(Gizmo gizmo) {
        if (isSceneReady()) {
            scene.mobs.add(gizmo);
        }
    }

    public static Image getTile(int cell) {
        Image ret;

        if(scene.roofTiles!=null) {
            ret = scene.roofTiles.tile(cell);

            if (ret != null) {
                return ret;
            }
        }

        ret = scene.baseTiles.tile(cell);
        if (ret != null) {
            return ret;
        }

        return scene.logicTiles.tile(cell);
    }

    static public boolean defaultCellSelector() {
        if(isSceneReady()) {
            return scene.cellSelector.defaultListner();
        }
        return true;
    }

    @LuaInterface
    static DungeonTilemap getBaseTiles() {
        if(isSceneReady()) {
            return scene.baseTiles;
        }
        throw new IllegalStateException("Scene not ready");
    }

}
