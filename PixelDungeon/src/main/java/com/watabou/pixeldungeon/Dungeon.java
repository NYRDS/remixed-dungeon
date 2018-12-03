/*
t * Pixel Dungeon
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
package com.watabou.pixeldungeon;

import com.nyrds.android.lua.LuaEngine;
import com.nyrds.android.util.FileSystem;
import com.nyrds.android.util.Scrambler;
import com.nyrds.android.util.TrackedRuntimeException;
import com.nyrds.android.util.Util;
import com.nyrds.pixeldungeon.items.common.Library;
import com.nyrds.pixeldungeon.levels.IceCavesLevel;
import com.nyrds.pixeldungeon.levels.NecroLevel;
import com.nyrds.pixeldungeon.levels.objects.LevelObject;
import com.nyrds.pixeldungeon.ml.EventCollector;
import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.pixeldungeon.mobs.npc.AzuterronNPC;
import com.nyrds.pixeldungeon.mobs.npc.CagedKobold;
import com.nyrds.pixeldungeon.mobs.npc.PlagueDoctorNPC;
import com.nyrds.pixeldungeon.mobs.npc.ScarecrowNPC;
import com.nyrds.pixeldungeon.utils.DungeonGenerator;
import com.nyrds.pixeldungeon.utils.Position;
import com.watabou.noosa.Game;
import com.watabou.noosa.audio.Sample;
import com.watabou.pixeldungeon.Rankings.gameOver;
import com.watabou.pixeldungeon.actors.Actor;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.buffs.Amok;
import com.watabou.pixeldungeon.actors.buffs.Invisibility;
import com.watabou.pixeldungeon.actors.buffs.Light;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.actors.hero.HeroClass;
import com.watabou.pixeldungeon.actors.mobs.Mimic;
import com.watabou.pixeldungeon.actors.mobs.Mob;
import com.watabou.pixeldungeon.actors.mobs.npcs.Blacksmith;
import com.watabou.pixeldungeon.actors.mobs.npcs.Ghost;
import com.watabou.pixeldungeon.actors.mobs.npcs.Imp;
import com.watabou.pixeldungeon.actors.mobs.npcs.WandMaker;
import com.watabou.pixeldungeon.effects.Speck;
import com.watabou.pixeldungeon.items.Ankh;
import com.watabou.pixeldungeon.items.Heap;
import com.watabou.pixeldungeon.items.potions.Potion;
import com.watabou.pixeldungeon.items.rings.Ring;
import com.watabou.pixeldungeon.items.scrolls.Scroll;
import com.watabou.pixeldungeon.items.wands.Wand;
import com.watabou.pixeldungeon.levels.DeadEndLevel;
import com.watabou.pixeldungeon.levels.Level;
import com.watabou.pixeldungeon.levels.Room;
import com.watabou.pixeldungeon.scenes.GameScene;
import com.watabou.pixeldungeon.ui.QuickSlot;
import com.watabou.pixeldungeon.utils.BArray;
import com.watabou.pixeldungeon.utils.GLog;
import com.watabou.pixeldungeon.utils.Utils;
import com.watabou.pixeldungeon.windows.WndResurrect;
import com.watabou.utils.Bundle;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;
import com.watabou.utils.SparseArray;
import com.watabou.utils.SystemTime;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;

import androidx.annotation.NonNull;

import static com.watabou.pixeldungeon.PixelDungeon.MOVE_TIMEOUTS;

public class Dungeon {

    public static int     potionOfStrength;
    public static int     scrollsOfUpgrade;
    public static int     arcaneStyli;
    public static boolean dewVial; // true if the dew vial can be spawned
    public static int     transmutation; // depth number for a well of transmutation

    public static int challenges;

    public static Hero  hero;
    public static Level level;

    public static  int depth;
    private static int scrambledGold;
    private static boolean loading = false;
    private static long lastSaveTimestamp;

    public static  String  gameId;
    private static boolean realtime;
    private static int     moveTimeoutIndex;

    public static HashSet<Integer> chapters;

    // Hero's field of view
    public static boolean[] visible;

    public static boolean nightMode;

    private static boolean[] passable;

    public static HeroClass heroClass;

    public static void initSizeDependentStuff(int w, int h) {
        int size = w * h;
        Actor.clear();
        visible = new boolean[size];
        passable = new boolean[size];

        Arrays.fill(visible, false);

        PathFinder.setMapSize(w, h);
    }

    public static void init() {
        gameId = String.valueOf(SystemTime.now());

        challenges = PixelDungeon.challenges();

        Scroll.initLabels();
        Potion.initColors();
        Wand.initWoods();
        Ring.initGems();

        Statistics.reset();
        Journal.reset();

        depth = 0;
        gold(0);

        potionOfStrength = 0;
        scrollsOfUpgrade = 0;
        arcaneStyli = 0;
        dewVial = true;
        transmutation = Random.IntRange(6, 14);

        chapters = new HashSet<>();

        Ghost.Quest.reset();
        WandMaker.Quest.reset();
        Blacksmith.Quest.reset();
        Imp.Quest.reset();
        ScarecrowNPC.Quest.reset();
        AzuterronNPC.Quest.reset();
        CagedKobold.Quest.reset();
        PlagueDoctorNPC.Quest.reset();

        Room.shuffleTypes();

        hero = new Hero(difficulty);

        Badges.reset();

        heroClass.initHero(hero);

        hero.levelId = DungeonGenerator.getEntryLevel();

        realtime = PixelDungeon.realtime();
        moveTimeoutIndex = PixelDungeon.limitTimeoutIndex(PixelDungeon.moveTimeout());

        SaveUtils.deleteLevels(heroClass);
    }

    public static boolean isChallenged(int mask) {
        return (challenges & mask) != 0;
    }

    private static void updateStatistics() {
        if (depth > Statistics.deepestFloor) {
            Statistics.deepestFloor = depth;

            Statistics.completedWithNoKilling = Statistics.qualifiedForNoKilling;
        }
    }

    @NonNull
    public static Level newLevel(Position pos) {

        Dungeon.level = null;
        updateStatistics();
        Level level = DungeonGenerator.createLevel(pos);

        Statistics.qualifiedForNoKilling = !DungeonGenerator.getLevelProperty(level.levelId, "isSafe", false);

        return level;
    }

    public static void resetLevel() {

        initSizeDependentStuff(level.getWidth(), level.getHeight());

        level.reset();
        switchLevel(level, level.entrance, hero.levelId);
    }

    public static String tip() {
        return tip(level);
    }

    public static String tip(Level _level) {
        if (_level instanceof DeadEndLevel) {

            return Game.getVar(R.string.Dungeon_DeadEnd);

        } else {
            String[] tips = Game.getVars(R.array.Dungeon_Tips);
            int index = depth - 1;

            if (index == -1) {
                return "Welcome to test level";
            }

            if (index < tips.length) {
                return tips[index];
            } else {
                return Game.getVar(R.string.Dungeon_NoTips);
            }
        }
    }

    public static boolean shopOnLevel(Level level) {
        if (level instanceof NecroLevel || level instanceof IceCavesLevel) {
            return false;
        } else {
            return depth == 6 || depth == 11 || depth == 16 || depth == 27;
        }

    }

    public static boolean bossLevel() {
        return Dungeon.level != null && Dungeon.level.isBossLevel();
    }

    @SuppressWarnings("deprecation")
    public static void switchLevel(final Level level, int pos, String levelId) {

        nightMode = new Date().getHours() < 7;

        Actor.init(level);

        Actor respawner = level.respawner();
        if (respawner != null) {
            Actor.add(level.respawner());
        }

        hero.setPos(pos);
        hero.levelId = levelId;

        if (!level.cellValid(hero.getPos())) {
            hero.setPos(level.entrance);
        }

        hero.viewDistance = hero.hasBuff(Light.class) ? Math.max(Level.MIN_VIEW_DISTANCE + 1, level.getViewDistance()) : level.getViewDistance();

        Dungeon.level = level;
    }

    public static boolean posNeeded() {
        int[] quota = {4, 2, 9, 4, 14, 6, 19, 8, 24, 9};
        return chance(quota, potionOfStrength);
    }

    public static boolean soeNeeded() {
        int[] quota = {5, 3, 10, 6, 15, 9, 20, 12, 25, 13};
        return chance(quota, scrollsOfUpgrade);
    }

    private static boolean chance(int[] quota, int number) {

        for (int i = 0; i < quota.length; i += 2) {
            int qDepth = quota[i];
            if (depth <= qDepth) {
                int qNumber = quota[i + 1];
                return Random.Float() < (float) (qNumber - number) / (qDepth - depth + 1);
            }
        }

        return false;
    }

    public static boolean asNeeded() {
        return Random.Int(12 * (1 + arcaneStyli)) < depth;
    }

    private static final String VERSION      = "version";
    private static final String CHALLENGES   = "challenges";
    private static final String HERO         = "hero";
    private static final String GOLD         = "gold";
    private static final String DEPTH        = "depth";
    private static final String LEVEL        = "level";
    private static final String POS          = "potionsOfStrength";
    private static final String SOU          = "scrollsOfEnhancement";
    private static final String AS           = "arcaneStyli";
    private static final String DV           = "dewVial";
    private static final String WT           = "transmutation";
    private static final String CHAPTERS     = "chapters";
    private static final String QUESTS       = "quests";
    private static final String BADGES       = "badges";
    private static final String SCRIPTS_DATA = "scripts_data";
    private static final String GAME_ID      = "game_id";
    private static final String REALTIME     = "realtime";
    private static final String MOVE_TIMEOUT = "move_timeout";

    public static void gameOver() {
        Dungeon.deleteGame(true);
    }

    public static void saveGame(String fileName) throws IOException {
        Bundle bundle = new Bundle();

        bundle.put(GAME_ID, gameId);
        bundle.put(VERSION, Game.version);
        bundle.put(CHALLENGES, challenges);
        bundle.put(HERO, hero);
        bundle.put(GOLD, gold());
        bundle.put(DEPTH, depth);

        bundle.put(POS, potionOfStrength);
        bundle.put(SOU, scrollsOfUpgrade);
        bundle.put(AS, arcaneStyli);
        bundle.put(DV, dewVial);
        bundle.put(WT, transmutation);

        int count = 0;
        int ids[] = new int[chapters.size()];
        for (Integer id : chapters) {
            ids[count++] = id;
        }
        bundle.put(CHAPTERS, ids);

        Bundle quests = new Bundle();
        Ghost.Quest.storeInBundle(quests);
        WandMaker.Quest.storeInBundle(quests);
        Blacksmith.Quest.storeInBundle(quests);
        Imp.Quest.storeInBundle(quests);
        AzuterronNPC.Quest.storeInBundle(quests);
        ScarecrowNPC.Quest.storeInBundle(quests);
        CagedKobold.Quest.storeInBundle(quests);
        PlagueDoctorNPC.Quest.storeInBundle(quests);
        bundle.put(QUESTS, quests);

        Room.storeRoomsInBundle(bundle);

        Statistics.storeInBundle(bundle);
        Journal.storeInBundle(bundle);
        Logbook.storeInBundle(bundle);

        Scroll.save(bundle);
        Potion.save(bundle);
        Wand.save(bundle);
        Ring.save(bundle);
        QuickSlot.save(bundle);

        Bundle badges = new Bundle();
        Badges.saveLocal(badges);
        bundle.put(BADGES, badges);

        bundle.put(REALTIME, realtime);
        bundle.put(MOVE_TIMEOUT, moveTimeoutIndex);

        bundle.put(SCRIPTS_DATA,
                LuaEngine.getEngine().require(LuaEngine.SCRIPTS_LIB_STORAGE).get("serializeGameData").call().checkjstring());

        OutputStream output = FileSystem.getOutputStream(fileName);
        Bundle.write(bundle, output);
        output.close();
    }

    public static void saveLevel(String saveTo) throws IOException {
        level.removePets();

        Bundle bundle = new Bundle();
        bundle.put(LEVEL, level);

        bundle.put(SCRIPTS_DATA,
                LuaEngine.getEngine().require(LuaEngine.SCRIPTS_LIB_STORAGE).get("serializeLevelData").call().checkjstring());


        OutputStream output = FileSystem.getOutputStream(saveTo);
        Bundle.write(bundle, output);
        output.close();
    }

    private static void saveAllImpl() {
        float MBytesAvailable = Util.getAvailableInternalMemorySize() / 1024f / 1024f;

        if (MBytesAvailable < 2) {
            EventCollector.logEvent("saveGame", "lowMemory");
            Game.toast("Low memory condition");
        }

        if (hero!= null && hero.isAlive()) {

            Actor.fixTime();
            try {
                Position current = currentPosition();
                String saveToLevel = getLevelSaveFile(current);

                String saveToGame = SaveUtils.gameFile(hero.heroClass);

                saveGame("tmp.game");
                saveLevel("tmp.level");

                FileSystem.getInternalStorageFile(saveToGame).delete();
                FileSystem.getInternalStorageFile(saveToLevel).delete();

                FileSystem.getInternalStorageFile("tmp.game").renameTo(FileSystem.getInternalStorageFile(saveToGame));
                FileSystem.getInternalStorageFile("tmp.level").renameTo(FileSystem.getInternalStorageFile(saveToLevel));

            } catch (IOException e) {
                throw new TrackedRuntimeException("cannot write save", e);
            }

            GamesInProgress.set(hero.heroClass, depth, hero.lvl());

        } else if (WndResurrect.instance != null) {

            WndResurrect.instance.hide();
            Hero.reallyDie(WndResurrect.causeOfDeath);
        }

        Badges.saveGlobal();
        Library.saveLibrary();
    }

    @NonNull
    private static String getLevelSaveFile(Position current) {
        return SaveUtils.depthFileForSave(hero.heroClass,
                DungeonGenerator.getLevelDepth(current.levelId),
                DungeonGenerator.getLevelKind(current.levelId),
                current.levelId);
    }

    public synchronized static void save() {

        if (SystemTime.now() - lastSaveTimestamp < 1000) {
            return;
        }

        lastSaveTimestamp = SystemTime.now();

        try {
            EventCollector.startTrace("saveGame");
            saveAllImpl();
            EventCollector.stopTrace("saveGame", "saveGame", Dungeon.level.levelId, Game.version);
        } catch (Exception e) {
            EventCollector.logException(e);
            throw new TrackedRuntimeException(e);
        }
    }


    public static void loadGame() throws IOException {
        loadGame(SaveUtils.gameFile(heroClass), true);
    }

    public static void loadGameForRankings(String fileName) throws IOException {
        loadGame(fileName, false);
    }

    private static void loadGameFromBundle(Bundle bundle, boolean fullLoad) {

        Dungeon.gameId = bundle.optString(GAME_ID, Utils.UNKNOWN);
        Dungeon.challenges = bundle.getInt(CHALLENGES);

        Dungeon.level = null;
        Dungeon.depth = -1;

        Scroll.restore(bundle);
        Potion.restore(bundle);
        Wand.restore(bundle);
        Ring.restore(bundle);
        QuickSlot.restore(bundle);

        potionOfStrength = bundle.getInt(POS);
        scrollsOfUpgrade = bundle.getInt(SOU);
        arcaneStyli = bundle.getInt(AS);
        dewVial = bundle.getBoolean(DV);
        transmutation = bundle.getInt(WT);

        if (fullLoad) {
            chapters = new HashSet<>();
            int ids[] = bundle.getIntArray(CHAPTERS);
            for (int id : ids) {
                chapters.add(id);
            }

            Bundle quests = bundle.getBundle(QUESTS);
            if (!quests.isNull()) {
                Ghost.Quest.restoreFromBundle(quests);
                WandMaker.Quest.restoreFromBundle(quests);
                Blacksmith.Quest.restoreFromBundle(quests);
                Imp.Quest.restoreFromBundle(quests);
                AzuterronNPC.Quest.restoreFromBundle(quests);
                ScarecrowNPC.Quest.restoreFromBundle(quests);
                CagedKobold.Quest.restoreFromBundle(quests);
                PlagueDoctorNPC.Quest.restoreFromBundle(quests);
            } else {
                Ghost.Quest.reset();
                WandMaker.Quest.reset();
                Blacksmith.Quest.reset();
                Imp.Quest.reset();
                AzuterronNPC.Quest.reset();
                ScarecrowNPC.Quest.reset();
                CagedKobold.Quest.reset();
                PlagueDoctorNPC.Quest.reset();
            }

            Room.restoreRoomsFromBundle(bundle);
        }

        Bundle badges = bundle.getBundle(BADGES);
        if (!badges.isNull()) {
            Badges.loadLocal(badges);
        } else {
            Badges.reset();
        }

        @SuppressWarnings("unused")
        String version = bundle.getString(VERSION);

        hero = (Hero) bundle.get(HERO);

        gold(bundle.getInt(GOLD));
        depth = bundle.getInt(DEPTH);

        Statistics.restoreFromBundle(bundle);
        Journal.restoreFromBundle(bundle);
        Logbook.restoreFromBundle(bundle);
        LuaEngine.getEngine().require(LuaEngine.SCRIPTS_LIB_STORAGE).get("deserializeGameData").call(bundle.getString(SCRIPTS_DATA));

        realtime = bundle.optBoolean(REALTIME, false);
        moveTimeoutIndex = PixelDungeon.limitTimeoutIndex(bundle.optInt(MOVE_TIMEOUT, Integer.MAX_VALUE));
    }

    private static void loadGame(String fileName, boolean fullLoad) throws IOException {
        Bundle bundle = gameBundle(fileName);

        loadGameFromBundle(bundle, fullLoad);
    }

    public static Level loadLevel(Position next) throws IOException {
        loading = true;

        DungeonGenerator.loadingLevel(next);

        String loadFrom = SaveUtils.depthFileForLoad(heroClass,
                DungeonGenerator.getLevelDepth(next.levelId),
                DungeonGenerator.getLevelKind(next.levelId),
                next.levelId);

        GLog.toFile("loading level: %s", loadFrom);

        InputStream input;

        if (!DungeonGenerator.isStatic(next.levelId) && FileSystem.getFile(loadFrom).exists()) {
            input = new FileInputStream(FileSystem.getFile(loadFrom));
            Dungeon.level = null;
        } else {
            GLog.toFile("File %s not found!", loadFrom);
            return newLevel(next);
        }

        Bundle bundle = Bundle.read(input);

        input.close();

        if (bundle == null) {
            EventCollector.logEvent("Dungeon.loadLevel", "read fail");
            return newLevel(next);
        }

        Level level = Level.fromBundle(bundle, "level");
        LuaEngine.getEngine().require(LuaEngine.SCRIPTS_LIB_STORAGE).get("deserializeLevelData").call(bundle.getString(SCRIPTS_DATA));

        if (level == null) {
            level = newLevel(next);
        }

        level.levelId = next.levelId;
        initSizeDependentStuff(level.getWidth(), level.getHeight());

        loading = false;

        return level;
    }

    public static void deleteGame(boolean deleteLevels) {
        GLog.toFile("deleteGame");
        SaveUtils.deleteGameFile(heroClass);

        if (deleteLevels) {
            SaveUtils.deleteLevels(heroClass);
        }

        GamesInProgress.delete(heroClass);
    }

    public static Bundle gameBundle(String fileName) throws IOException {

        InputStream input = new FileInputStream(FileSystem.getFile(fileName));

        Bundle bundle = Bundle.read(input);
        input.close();

        return bundle;
    }

    public static void preview(GamesInProgress.Info info, Bundle bundle) {
        info.depth = bundle.getInt(DEPTH);
        if (info.depth == -1) {
            info.depth = bundle.getInt("maxDepth"); // FIXME
        }
        Hero.preview(info, bundle.getBundle(HERO));
    }

    public static void fail(String desc) {
        if (hero.belongings.getItem(Ankh.class) == null) {
            Rankings.INSTANCE.submit(Rankings.gameOver.LOSE, desc);
        }
    }

    public static void win(String desc, gameOver kind) {

        if (challenges != 0) {
            Badges.validateChampion();
        }

        Rankings.INSTANCE.submit(kind, desc);
    }

    public static void observe() {

        if (level == null) {
            return;
        }

        level.updateFieldOfView(hero);
        System.arraycopy(level.fieldOfView, 0, visible, 0, visible.length);

        BArray.or(level.visited, visible, level.visited);

        if (GameScene.isSceneReady()) {
            GameScene.afterObserve();
        }
    }

    private static void markActorsAsUnpassableIgnoreFov() {
        for (Actor actor : Actor.all()) {
            if (actor instanceof Char) {
                int pos = ((Char) actor).getPos();
                passable[pos] = false;
            }
        }
    }

    private static void markObjects() {
        for (int i = 0; i < level.objects.size(); i++) {
            SparseArray<LevelObject> objectLayer = level.objects.valueAt(i);
            for (int j = 0; j < objectLayer.size(); j++) {
                LevelObject object = objectLayer.valueAt(j);
                if (object.nonPassable()) {
                    passable[object.getPos()] = false;
                }
            }
        }
    }

    private static void markActorsAsUnpassable(Hero ch, boolean[] visible) {
        for (Actor actor : Actor.all()) {
            if (actor instanceof Char) {
                int pos = ((Char) actor).getPos();
                if (visible[pos]) {
                    if (actor instanceof Mob) {
                        passable[pos] = passable[pos] && ((Mob) actor).isPet();
                    }
                }
            }
        }
    }


    public static int findPath(Hero ch, int from, int to, boolean pass[], boolean[] visible) {

        if (level.adjacent(from, to)) {
            if (!(pass[to] || level.avoid[to])) {
                return -1;
            }

            Char chr = Actor.findChar(to);

            if (chr instanceof Mob) {
                Mob mob = (Mob) chr;
                if (mob.isPet()) {
                    return to;
                }
            }
            return chr == null ? to : -1;
        }

        if (ch.isFlying() || ch.hasBuff(Amok.class)) {
            BArray.or(pass, level.avoid, passable);
        } else {
            System.arraycopy(pass, 0, passable, 0, level.getLength());
        }

        markActorsAsUnpassable(ch, visible);

        markObjects();

        return PathFinder.getStep(from, to, passable);
    }


    public static int findPath(Char ch, int from, int to, boolean pass[]) {

        if (level.adjacent(from, to)) {

            if (!(pass[to] || level.avoid[to])) {
                return -1;
            }

            Char chr = Actor.findChar(to);

            return chr == null ? to : -1;
        }

        if (ch.isFlying() || ch.hasBuff(Amok.class)) {
            BArray.or(pass, level.avoid, passable);
        } else {
            System.arraycopy(pass, 0, passable, 0, level.getLength());
        }

        markActorsAsUnpassableIgnoreFov();

        markObjects();

        return PathFinder.getStep(from, to, passable);

    }

    public static int flee(Char ch, int cur, int from, boolean pass[]) {

        if (ch.isFlying()) {
            BArray.or(pass, level.avoid, passable);
        } else {
            System.arraycopy(pass, 0, passable, 0, level.getLength());
        }

        markActorsAsUnpassableIgnoreFov();
        markObjects();

        passable[cur] = true;

        return PathFinder.getStepBack(cur, from, passable);
    }

    public static void challengeAllMobs(Char ch, String sound) {

        if (!GameScene.isSceneReady()) {
            return;
        }

        for (Mob mob : Dungeon.level.mobs) {
            mob.beckon(ch.getPos());
        }

        for (Heap heap : Dungeon.level.allHeaps()) {
            if (heap.type == Heap.Type.MIMIC) {
                Mimic m = Mimic.spawnAt(heap.pos, heap.items);
                if (m != null) {
                    m.beckon(ch.getPos());
                    heap.destroy();
                }
            }
        }

        ch.getSprite().centerEmitter().start(Speck.factory(Speck.SCREAM), 0.3f, 3);

        Sample.INSTANCE.play(sound);
        if (ch instanceof Hero) {
            Invisibility.dispel((Hero) ch);
        }
    }

    public static Position currentPosition() {
        return new Position(hero.levelId, hero.getPos());
    }

    private static int difficulty;

    public static void setDifficulty(int _difficulty) {
        difficulty = _difficulty;
        PixelDungeon.setDifficulty(difficulty);
    }

    public static int gold() {
        return Scrambler.descramble(scrambledGold);
    }

    public static void gold(int value) {
        scrambledGold = Scrambler.scramble(value);
    }

    public static boolean isLoading() {
        return loading;
    }

    public static boolean realtime() {
        return realtime;
    }

    public static double moveTimeout() {
        return MOVE_TIMEOUTS[moveTimeoutIndex];
    }

    public static void saveCurrentLevel() {
        try {
            saveLevel(getLevelSaveFile(currentPosition()));
        } catch (IOException e) {
            throw new TrackedRuntimeException(e);
        }
    }
}
