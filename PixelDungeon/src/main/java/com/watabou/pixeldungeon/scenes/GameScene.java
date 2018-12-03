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

import com.nyrds.android.util.ModdingMode;
import com.nyrds.android.util.TrackedRuntimeException;
import com.nyrds.pixeldungeon.levels.objects.LevelObject;
import com.nyrds.pixeldungeon.levels.objects.sprites.LevelObjectSprite;
import com.nyrds.pixeldungeon.ml.EventCollector;
import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.pixeldungeon.utils.DungeonGenerator;
import com.nyrds.pixeldungeon.windows.WndHeroSpells;
import com.watabou.noosa.Camera;
import com.watabou.noosa.Game;
import com.watabou.noosa.Group;
import com.watabou.noosa.Image;
import com.watabou.noosa.SkinnedBlock;
import com.watabou.noosa.Text;
import com.watabou.noosa.Visual;
import com.watabou.noosa.audio.Music;
import com.watabou.noosa.audio.Sample;
import com.watabou.noosa.particles.Emitter;
import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.Badges;
import com.watabou.pixeldungeon.ClassicDungeonTilemap;
import com.watabou.pixeldungeon.CustomLayerTilemap;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.DungeonTilemap;
import com.watabou.pixeldungeon.FogOfWar;
import com.watabou.pixeldungeon.PixelDungeon;
import com.watabou.pixeldungeon.Statistics;
import com.watabou.pixeldungeon.actors.Actor;
import com.watabou.pixeldungeon.actors.blobs.Blob;
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
import com.watabou.pixeldungeon.levels.features.Chasm;
import com.watabou.pixeldungeon.plants.Plant;
import com.watabou.pixeldungeon.sprites.CharSprite;
import com.watabou.pixeldungeon.sprites.DiscardedItemSprite;
import com.watabou.pixeldungeon.sprites.ItemSprite;
import com.watabou.pixeldungeon.sprites.PlantSprite;
import com.watabou.pixeldungeon.ui.AttackIndicator;
import com.watabou.pixeldungeon.ui.Banner;
import com.watabou.pixeldungeon.ui.BusyIndicator;
import com.watabou.pixeldungeon.ui.GameLog;
import com.watabou.pixeldungeon.ui.HealthIndicator;
import com.watabou.pixeldungeon.ui.QuickSlot;
import com.watabou.pixeldungeon.ui.ResumeIndicator;
import com.watabou.pixeldungeon.ui.StatusPane;
import com.watabou.pixeldungeon.ui.Toast;
import com.watabou.pixeldungeon.ui.Toolbar;
import com.watabou.pixeldungeon.ui.Window;
import com.watabou.pixeldungeon.utils.GLog;
import com.watabou.pixeldungeon.windows.WndBag;
import com.watabou.pixeldungeon.windows.WndBag.Mode;
import com.watabou.pixeldungeon.windows.WndGame;
import com.watabou.utils.Random;
import com.watabou.utils.SparseArray;

import java.util.HashSet;

public class GameScene extends PixelScene {

    private static final float MAX_BRIGHTNESS = 1.22f;

    private static volatile GameScene scene;

    private SkinnedBlock water;

    private DungeonTilemap logicTiles;
    private DungeonTilemap baseTiles;
    private DungeonTilemap roofTiles;

    private FogOfWar fog;

    private static CellSelector cellSelector;

    private Group ripples;
    private Group plants;
    private Group heaps;
    private Group mobs;
    private Group emitters;
    private Group effects;
    private Group gases;
    private Group spells;
    private Group statuses;
    private Group emoicons;

    private Group objects;

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

    public void updateUiCamera() {
        statusPane.setSize(uiCamera.width, 0);
        toolbar.setRect(0, uiCamera.height - toolbar.height(), uiCamera.width, toolbar.height());
        attack.setPos(uiCamera.width - attack.width(), toolbar.top() - attack.height());
        resume.setPos(uiCamera.width - resume.width(), attack.top() - resume.height());
        log.setRect(0, toolbar.top(), attack.left(), 0);
        busy.x = 1;
        busy.y = statusPane.bottom() + 2;
    }


    static public void playLevelMusic() {
        Music.INSTANCE.play(Dungeon.level.music(), true);
        Music.INSTANCE.volume(1f);
    }

    @Override
    public void create() {
        playLevelMusic();

        Level level = Dungeon.level;

        PixelDungeon.lastClass(Dungeon.hero.heroClass.ordinal());

        super.create();

        Camera.main.zoom((float) (defaultZoom + PixelDungeon.zoom()));

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
            baseTiles = DungeonTilemap.factory(level, level.getTilesetForLayer(Level.LayerId.Base));
        } else {
            CustomLayerTilemap tiles = new CustomLayerTilemap(level, Level.LayerId.Base);
            tiles.addLayer(Level.LayerId.Deco);
            tiles.addLayer(Level.LayerId.Deco2);
            baseTiles = tiles;

            tiles = new CustomLayerTilemap(level,Level.LayerId.Roof_Base);
            tiles.addLayer(Level.LayerId.Roof_Deco);
            tiles.setTransparent(true);
            roofTiles = tiles;
        }
        terrain.add(baseTiles);


        objects = new Group();
        add(objects);

        for (int i = 0; i < level.objects.size(); i++) {
            SparseArray<LevelObject> objectLayer = level.objects.valueAt(i);
            for (int j = 0; j < objectLayer.size(); j++) {
                addLevelObjectSprite(objectLayer.valueAt(j));
            }
        }

        level.addVisuals(this);

        plants = new Group();
        add(plants);

        for (int i = 0; i < level.plants.size(); i++) {
            addPlantSprite(level.plants.valueAt(i));
        }

        heaps = new Group();
        add(heaps);

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
                mob.beckon(Dungeon.hero.getPos());
            }
        }

        Dungeon.hero.updateSprite();

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

        if(roofTiles!=null) {
            add(roofTiles);
        }

        fog.updateVisibility(Dungeon.visible, level.visited, level.mapped);
        add(fog);

        brightness(PixelDungeon.brightness());

        spells = new Group();
        add(spells);

        statuses = new Group();
        add(statuses);

        add(emoicons);

        add(new HealthIndicator());

        add(cellSelector = new CellSelector(baseTiles));

        Dungeon.hero.updateLook();

        statusPane = new StatusPane(Dungeon.hero, level);
        statusPane.camera = uiCamera;
        statusPane.setSize(uiCamera.width, 0);
        add(statusPane);

        toolbar = new Toolbar(Dungeon.hero, uiCamera.width);
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
            GLog.i(Game.getVar(R.string.GameScene_WelcomeBack), Dungeon.depth);
        } else {
            GLog.i(Game.getVar(R.string.GameScene_Welcome), Dungeon.depth);
            Sample.INSTANCE.play(Assets.SND_DESCEND);
        }
        switch (level.getFeeling()) {
            case CHASM:
                GLog.w(Game.getVar(R.string.GameScene_Chasm));
                break;
            case WATER:
                GLog.w(Game.getVar(R.string.GameScene_Water));
                break;
            case GRASS:
                GLog.w(Game.getVar(R.string.GameScene_Grass));
                break;
            default:
        }

        if (level instanceof RegularLevel
                && ((RegularLevel) level).secretDoors > Random.IntRange(3, 4)) {
            GLog.w(Game.getVar(R.string.GameScene_Secrets));
        }
        if (Dungeon.nightMode && !Dungeon.bossLevel()) {
            GLog.w(Game.getVar(R.string.GameScene_NightMode));
        }

        busy = new BusyIndicator();
        busy.camera = uiCamera;
        busy.x = 1;
        busy.y = statusPane.bottom() + 18;
        add(busy);

        sceneCreated = true;

        InterlevelScene.Mode appearMode = InterlevelScene.mode;
        InterlevelScene.mode = InterlevelScene.Mode.CONTINUE;

        //it is a chance for another level change right here if level entrance is at CHASM for example
        switch (appearMode) {
            case RESURRECT:
                Dungeon.hero.regenSprite();
                WandOfBlink.appear(Dungeon.hero, level.entrance);
                new Flare(8, 32).color(0xFFFF66, true).show(Dungeon.hero.getHeroSprite(), 2f);
                break;
            case RETURN:
                WandOfBlink.appear(Dungeon.hero, Dungeon.hero.getPos());
                break;
            case FALL:
                Chasm.heroLand();
                break;
            case DESCEND:

                DungeonGenerator.showStory(level);

                if (Dungeon.hero.isAlive() && !level.isSafe() && Dungeon.depth != 22 && Dungeon.depth != 1) {
                    Badges.validateNoKilling();
                }
                break;
            default:
        }

        Camera.main.target = Dungeon.hero.getHeroSprite();

        level.activateScripts();

        fadeIn();

        Dungeon.observe();
    }

    public void destroy() {
        scene = null;
        Badges.saveGlobal();

        super.destroy();
    }

    @Override
    public synchronized void pause() {
        if(!Game.softPaused) {
            Dungeon.save();
        }
    }

    @Override
    public synchronized void update() {
        if (!sceneCreated) {
            return;
        }

        if (Dungeon.hero == null) {
            return;
        }

        if (Dungeon.level == null) {
            return;
        }

        super.update();

        water.offset(waterSx * Game.elapsed, waterSy * Game.elapsed);

        Actor.process(Game.elapsed);

        if (Dungeon.hero.isReady() && !Dungeon.hero.paralysed) {
            log.newLine();
        }

        if (!Dungeon.realtime()) {
            cellSelector.enabled = Dungeon.hero.isReady();
        } else {
            cellSelector.enabled = Dungeon.hero.isAlive();
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
            selectItem(null, WndBag.Mode.ALL, null);
        }
    }

    public void brightness(boolean value) {

        float levelLimit = Math.min(Dungeon.level.getPropertyFloat("maxBrightness", MAX_BRIGHTNESS),
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

    private void addLevelObjectSprite(LevelObject obj) {
        (obj.sprite = (LevelObjectSprite) objects.recycle(LevelObjectSprite.class)).reset(obj);
    }

    private void addHeapSprite(Heap heap) {
        ItemSprite sprite = heap.sprite = (ItemSprite) heaps.recycle(ItemSprite.class);
        sprite.revive();
        sprite.link(heap);
        heaps.add(sprite);
    }

    private void addDiscardedSprite(Heap heap) {
        heap.sprite = (DiscardedItemSprite) heaps.recycle(DiscardedItemSprite.class);
        heap.sprite.revive();
        heap.sprite.link(heap);
        heaps.add(heap.sprite);
    }

    private void addPlantSprite(Plant plant) {
        (plant.sprite = (PlantSprite) plants.recycle(PlantSprite.class)).reset(plant);
    }

    private static void addBlobSprite(final Blob gas) {
        if (isSceneReady())
            if (gas.emitter == null) {
                scene.gases.add(new BlobEmitter(gas));
            }
    }

    public void prompt(String text) {

        if (prompt != null) {
            prompt.killAndErase();
            prompt = null;
        }

        if (text != null) {
            prompt = new Toast(text) {
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
        banner.x = align(uiCamera, (uiCamera.width - banner.width) / 2);
        banner.y = align(uiCamera, (uiCamera.height - banner.height) / 3);
        add(banner);
    }

    // -------------------------------------------------------

    public static void add(Plant plant) {
        if (scene != null && Dungeon.level != null) {
            scene.addPlantSprite(plant);
        } else {
            EventCollector.logException();
        }
    }

    public static void add(Blob gas) {
        if (scene != null && Dungeon.level != null) {
            Actor.add(gas);
            addBlobSprite(gas);
        } else {
            EventCollector.logException();
        }
    }

    public static void add(LevelObject obj) {
        if (isSceneReady()) {
            scene.addLevelObjectSprite(obj);
        } else {
            throw new TrackedRuntimeException(obj.toString());
        }
    }

    public static void add(Heap heap) {
        if (isSceneReady()) {
            scene.addHeapSprite(heap);
        } else {
            EventCollector.logException();
        }
    }

    public static void discard(Heap heap) {
        if (isSceneReady()) {
            scene.addDiscardedSprite(heap);
        } else {
            EventCollector.logException();
        }
    }

    public static boolean isSceneReady() {
        return scene != null && Dungeon.level != null;
    }

    public static void add(EmoIcon icon) {
        scene.emoicons.add(icon);
    }

    public static void effect(Visual effect) {
        scene.effects.add(effect);
    }

    public static Ripple ripple(int pos) {
        Ripple ripple = (Ripple) scene.ripples.recycle(Ripple.class);
        ripple.reset(pos);
        return ripple;
    }

    public static SpellSprite spellSprite() {
        return (SpellSprite) scene.spells.recycle(SpellSprite.class);
    }

    public static Emitter emitter() {
        if (scene != null) {
            Emitter emitter = (Emitter) scene.emitters.recycle(Emitter.class);
            emitter.revive();
            return emitter;
        } else {
            return null;
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
        scene.toolbar.pickup(item);
    }

    public static void updateMap() {
        if (isSceneReady()) {
            scene.baseTiles.updateAll();
        } else {
            EventCollector.logException();
        }
    }

    public static void updateMap(int cell) {
        if (isSceneReady()) {
            scene.baseTiles.updateCell(cell, Dungeon.level);
        } else {
            EventCollector.logException();
        }
    }

    public static void discoverTile(int pos) {
        if (isSceneReady()) {
            scene.baseTiles.discover(pos);
        } else {
            EventCollector.logException();
        }
    }

    public static void show(Window wnd) {
        cancelCellSelector();
        scene.add(wnd);
    }

    public static void afterObserve() {
        if (scene != null && scene.sceneCreated) {

            scene.fog.updateVisibility(Dungeon.visible, Dungeon.level.visited, Dungeon.level.mapped);

            for (Mob mob : Dungeon.level.mobs) {
                mob.getSprite().setVisible(Dungeon.visible[mob.getPos()]);
            }
        } else {
            EventCollector.logException();
        }
    }

    public static void flash(int color) {
        scene.fadeIn(0xFF000000 | color, true);
    }

    public static void gameOver() {
        Banner gameOver = new Banner(BannerSprites.get(BannerSprites.Type.GAME_OVER));
        gameOver.show(0x000000, 1f);
        scene.showBanner(gameOver);

        Sample.INSTANCE.play(Assets.SND_DEATH);
    }

    public static void bossSlain() {
        if (Dungeon.hero.isAlive()) {
            Banner bossSlain = new Banner(BannerSprites.get(BannerSprites.Type.BOSS_SLAIN));
            bossSlain.show(0xFFFFFF, 0.3f, 5f);
            scene.showBanner(bossSlain);

            Sample.INSTANCE.play(Assets.SND_BOSS);
        }
    }

    public static void handleCell(int cell) {
        cellSelector.select(cell);
    }

    public static void selectCell(CellSelector.Listener listener) {
        cellSelector.listener = listener;
        scene.prompt(listener.prompt());
    }

    private static boolean cancelCellSelector() {
        if (cellSelector != null && cellSelector.listener != null && cellSelector.listener != defaultCellListener) {
            cellSelector.cancel();
            return true;
        } else {
            return false;
        }
    }

    public static void selectItemFromBag(WndBag.Listener listener, Bag bag, Mode mode, String title) {
        cancelCellSelector();
        scene.add(new WndBag(bag, listener, mode, title));
    }

    public static WndBag selectItem(WndBag.Listener listener, WndBag.Mode mode, String title) {
        cancelCellSelector();

        WndBag wnd = mode == Mode.SEED ? WndBag.seedPouch(listener, mode, title)
                : WndBag.lastBag(listener, mode, title);
        scene.add(wnd);

        return wnd;
    }

    public static WndHeroSpells selectSpell(WndHeroSpells.Listener listener) {
        cancelCellSelector();

        WndHeroSpells wnd = new WndHeroSpells(listener);
        scene.add(wnd);

        return wnd;
    }

    static boolean cancel() {
        if (Dungeon.hero != null && (Dungeon.hero.curAction != null || Dungeon.hero.restoreHealth)) {

            Dungeon.hero.curAction = null;
            Dungeon.hero.restoreHealth = false;
            return true;

        } else {

            return cancelCellSelector();

        }
    }

    public static void ready() {
        selectCell(defaultCellListener);
        QuickSlot.cancel();
    }

    private static final CellSelector.Listener defaultCellListener = new CellSelector.Listener() {
        @Override
        public void onSelect(Integer cell) {
            if (Dungeon.hero.handle(cell)) {
                Dungeon.hero.next();
            }
        }

        @Override
        public String prompt() {
            return null;
        }
    };

    public void updateToolbar(boolean reset) {
        if (toolbar != null) {

            if(reset) {
                toolbar.destroy();

                toolbar = new Toolbar(Dungeon.hero,uiCamera.width);
                toolbar.camera = uiCamera;
            }

            toolbar.setRect(0, uiCamera.height - toolbar.height(), uiCamera.width, toolbar.height());
            add(toolbar);

            if(attack != null) {
                attack.setPos(uiCamera.width - attack.width(), toolbar.top() - attack.height());
                attack.update();
            }
            if(resume != null) {
                resume.setPos(uiCamera.width - resume.width(), attack.top() - resume.height());
                resume.update();
            }
        } else {
            EventCollector.logException();
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

    public static Image getTile(int cell) {
        Image ret = scene.baseTiles.tile(cell);
        if (ret == null) {
            ret = scene.logicTiles.tile(cell);
        }
        return ret;
    }
}
