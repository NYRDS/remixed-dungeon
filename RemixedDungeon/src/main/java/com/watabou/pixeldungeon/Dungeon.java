package com.watabou.pixeldungeon;


import com.google.common.base.Optional;
import com.nyrds.LuaInterface;
import com.nyrds.lua.LuaEngine;
import com.nyrds.pixeldungeon.ai.MobAi;
import com.nyrds.pixeldungeon.ai.Wandering;
import com.nyrds.pixeldungeon.game.GameLoop;
import com.nyrds.pixeldungeon.game.GamePreferences;
import com.nyrds.pixeldungeon.game.ModQuirks;
import com.nyrds.pixeldungeon.items.Treasury;
import com.nyrds.pixeldungeon.items.common.Library;
import com.nyrds.pixeldungeon.levels.IceCavesLevel;
import com.nyrds.pixeldungeon.levels.NecroLevel;
import com.nyrds.pixeldungeon.mechanics.buffs.BuffFactory;
import com.nyrds.pixeldungeon.mechanics.spells.SpellFactory;
import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.pixeldungeon.mobs.npc.AzuterronNPC;
import com.nyrds.pixeldungeon.mobs.npc.CagedKobold;
import com.nyrds.pixeldungeon.mobs.npc.PlagueDoctorNPC;
import com.nyrds.pixeldungeon.mobs.npc.ScarecrowNPC;
import com.nyrds.pixeldungeon.utils.CharsList;
import com.nyrds.pixeldungeon.utils.DungeonGenerator;
import com.nyrds.pixeldungeon.utils.EntityIdSource;
import com.nyrds.pixeldungeon.utils.ItemsList;
import com.nyrds.pixeldungeon.utils.Position;
import com.nyrds.platform.EventCollector;
import com.nyrds.platform.game.Game;
import com.nyrds.platform.storage.FileSystem;
import com.nyrds.platform.storage.Preferences;
import com.nyrds.platform.util.StringsManager;
import com.nyrds.platform.util.TrackedRuntimeException;
import com.nyrds.util.ModdingMode;
import com.nyrds.util.Util;
import com.watabou.noosa.Scene;
import com.watabou.pixeldungeon.Rankings.gameOver;
import com.watabou.pixeldungeon.actors.Actor;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.actors.hero.HeroClass;
import com.watabou.pixeldungeon.actors.mobs.Mob;
import com.watabou.pixeldungeon.actors.mobs.npcs.Blacksmith;
import com.watabou.pixeldungeon.actors.mobs.npcs.Ghost;
import com.watabou.pixeldungeon.actors.mobs.npcs.Imp;
import com.watabou.pixeldungeon.actors.mobs.npcs.WandMaker;
import com.watabou.pixeldungeon.items.Ankh;
import com.watabou.pixeldungeon.items.potions.Potion;
import com.watabou.pixeldungeon.items.rings.Ring;
import com.watabou.pixeldungeon.items.scrolls.Scroll;
import com.watabou.pixeldungeon.items.wands.Wand;
import com.watabou.pixeldungeon.levels.DeadEndLevel;
import com.watabou.pixeldungeon.levels.Level;
import com.watabou.pixeldungeon.levels.Room;
import com.watabou.pixeldungeon.levels.Terrain;
import com.watabou.pixeldungeon.scenes.GameScene;
import com.watabou.pixeldungeon.ui.QuickSlot;
import com.watabou.pixeldungeon.utils.BArray;
import com.watabou.pixeldungeon.utils.GLog;
import com.watabou.pixeldungeon.utils.Utils;
import com.watabou.pixeldungeon.windows.WndResurrect;
import com.watabou.utils.Bundle;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;
import com.watabou.utils.SystemTime;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.HashSet;

import lombok.SneakyThrows;
import lombok.val;


import static com.nyrds.platform.game.RemixedDungeon.MOVE_TIMEOUTS;

public class Dungeon {

    public static int potionOfStrength;
    public static int scrollsOfUpgrade;
    public static int arcaneStyli;
    public static boolean dewVial; // true if the dew vial can be spawned
    public static int transmutation; // depth number for a well of transmutation

    private static int challenges;
    private static int facilitations;

    @NotNull
    public static Hero hero = CharsList.DUMMY_HERO;

    public static Level level;
    public static String levelId;
    public static String previousLevelId;

    public static int depth;
    private static long lastSaveTimestamp;

    public static String gameId;

    private static boolean realtime;

    private static int moveTimeoutIndex;

    public static HashSet<Integer> chapters;

    // Hero's field of view
    public static boolean[] visible;

    public static boolean nightMode;

    // Current char passability map
    private static boolean[] passable;

    public static HeroClass heroClass;

    private static boolean isometricMode = false;
    public static boolean isometricModeAllowed = false;


    public static void initSizeDependentStuff(int w, int h) {
        int size = w * h;
        Actor.clearActors();

        visible = new boolean[size];
        passable = new boolean[size];

        Arrays.fill(visible, false);

        PathFinder.setMapSize(w, h);
    }

    public static void reset() {
        ModQuirks.reset();
        if (!Scene.sceneMode.equals(Scene.LEVELS_TEST)) {
            LuaEngine.reset();
        }

        DungeonGenerator.reset();
        SpellFactory.reset();

        Treasury.reset();
        Statistics.reset();
        Journal.reset();

        Ghost.Quest.reset();
        WandMaker.Quest.reset();
        Blacksmith.Quest.reset();
        Imp.Quest.reset();
        ScarecrowNPC.Quest.reset();
        AzuterronNPC.Quest.reset();
        CagedKobold.Quest.reset();
        PlagueDoctorNPC.Quest.reset();

        Badges.reset();
        ItemsList.reset();
        CharsList.reset();

        hero = CharsList.DUMMY_HERO;
    }

    public static void init() {
        synchronized (GameLoop.stepLock) {
            GameLoop.loadingOrSaving.incrementAndGet();

            SaveUtils.deleteLevels(heroClass);

            gameId = String.valueOf(SystemTime.now());

            reset();

            Wand.initWoods();
            Ring.initGems();
            Scroll.initLabels();
            Potion.initColors();

            depth = 0;

            potionOfStrength = 0;
            scrollsOfUpgrade = 0;
            arcaneStyli = 0;
            dewVial = true;
            transmutation = Random.IntRange(6, 14);

            chapters = new HashSet<>();

            Room.shuffleTypes();

            hero = new Hero(GameLoop.getDifficulty());

            heroClass.initHero(hero);

            hero.levelId = DungeonGenerator.getEntryLevel();

            realtime = GamePreferences.realtime();
            moveTimeoutIndex = GamePreferences.limitTimeoutIndex(GamePreferences.moveTimeout());

            GameLoop.loadingOrSaving.decrementAndGet();
        }
    }

    @Contract(pure = true)
    public static boolean isChallenged(int mask) {
        return (getChallenges() & mask) != 0;
    }

    @Contract(pure = true)
    public static boolean isFacilitated(int mask) {
        return (facilitations & mask) != 0;
    }

    private static void updateStatistics() {
        if (depth > Statistics.deepestFloor) {
            Statistics.deepestFloor = depth;

            Statistics.completedWithNoKilling = Statistics.qualifiedForNoKilling;
        }
    }

    @NotNull
    public static Level newLevel(@NotNull Position pos) {
        synchronized (GameLoop.stepLock) {
            GameLoop.loadingOrSaving.incrementAndGet();

            updateStatistics();

            if (!DungeonGenerator.isLevelExist(pos.levelId)) {
                pos.levelId = DungeonGenerator.getEntryLevel();
                pos.cellId = -1;
                pos = DungeonGenerator.descend(pos);
            }

            Level level = DungeonGenerator.createLevel(pos);

            Dungeon.hero.setPos(level.entrance);

            Statistics.qualifiedForNoKilling = !DungeonGenerator.getLevelProperty(level.levelId, "isSafe", false);

            GameLoop.loadingOrSaving.decrementAndGet();
            return level;
        }
    }

    public static void resetLevel() {

        initSizeDependentStuff(level.getWidth(), level.getHeight());

        level.reset();
        switchLevel(level, level.entrance, CharsList.emptyMobList);
    }

    public static String tip(Level _level) {
        if (_level instanceof DeadEndLevel) {

            return StringsManager.getVar(R.string.Dungeon_DeadEnd);

        } else {
            String[] tips = StringsManager.getVars(R.array.Dungeon_Tips);
            int index = depth - 1;

            if (index == -1) {
                return "Welcome to test level";
            }

            if (index < tips.length) {
                return tips[index];
            } else {
                return StringsManager.getVar(R.string.Dungeon_NoTips);
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
        final Level level = Dungeon.level;

        return level != null && level.isBossLevel();
    }

    public static void switchLevel(@NotNull final Level level, int pos, Collection<Mob> followers) {
        EventCollector.setSessionData("level", level.levelId);

        isometricModeAllowed = level.isPlainTile(1); //TODO check entire level

        if (isometricModeAllowed) {
            setIsometricMode(Preferences.INSTANCE.getBoolean(Preferences.KEY_USE_ISOMETRIC_TILES, false));
        } else {
            setIsometricMode(false);
        }

        nightMode = new GregorianCalendar().get(Calendar.HOUR_OF_DAY) < 7;

        Actor.init(level);

        Actor respawner = level.respawner();
        if (respawner != null) {
            Actor.add(respawner);
        }

        hero.levelId = level.levelId;
        hero.setViewDistance(level.getViewDistance());

        if (level.cellValid(pos)) {
            hero.setPos(pos);
        } else if (level.cellValid(level.entrance)) {
            hero.setPos(level.entrance);
        } else {
            hero.setPos(level.getRandomTerrainCell(Terrain.EMPTY));
        }

        for (Mob mob : followers) {
            var dup = CharsList.getById(mob.getId());

            if (dup.valid()) {
                GLog.debug("Removing dup: %s, %d", dup.getEntityKind(), dup.getId());
                Actor.remove(dup);
                Actor.freeCell(dup.getPos());
                CharsList.remove(dup.getId());
                level.mobs.remove(dup);
            }

            int cell = level.getEmptyCellNextTo(hero.getPos());
            if (!level.cellValid(cell)) {
                cell = hero.getPos();
            }
            mob.setPos(cell);

            mob.setEnemy(CharsList.DUMMY);
            mob.setState(MobAi.getStateByClass(Wandering.class));
            level.spawnMob(mob);
        }

        previousLevelId = levelId;
        levelId = level.levelId;
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

    private static final String VERSION = "version";
    private static final String HERO = "hero";
    private static final String DEPTH = "depth";
    private static final String LEVEL = "level";
    private static final String POS = "potionsOfStrength";
    private static final String SOU = "scrollsOfEnhancement";
    private static final String AS = "arcaneStyli";
    private static final String DV = "dewVial";
    private static final String WT = "transmutation";
    private static final String CHAPTERS = "chapters";
    private static final String QUESTS = "quests";
    private static final String BADGES = "badges";
    private static final String SCRIPTS_DATA = "scripts_data";
    private static final String GAME_ID = "game_id";
    private static final String MOVE_TIMEOUT = "move_timeout";
    private static final String LAST_USED_ID = "lastUsedId";
    private static final String MOD = "mod";
    private static final String REALTIME = "realtime";
    private static final String CHALLENGES = "challenges";
    private static final String FACILITATIONS = "facilations";


    public static void gameOver() {
        SaveUtils.deleteSaveFromSlot(SaveUtils.getPrevSave(), heroClass);
        SaveUtils.deleteSaveFromSlot(SaveUtils.getAutoSave(), heroClass);
        Dungeon.deleteGame(true);
        heroClass = HeroClass.NONE;
    }

    public static void saveGame(String fileName) throws IOException {
        Bundle bundle = new Bundle();

        bundle.put(GAME_ID, gameId);
        bundle.put(VERSION, Game.version);
        bundle.put(HERO, hero);
        bundle.put(DEPTH, depth);

        bundle.put(POS, potionOfStrength);
        bundle.put(SOU, scrollsOfUpgrade);
        bundle.put(AS, arcaneStyli);
        bundle.put(DV, dewVial);
        bundle.put(WT, transmutation);

        bundle.put(REALTIME, realtime);
        bundle.put(CHALLENGES, getChallenges());
        bundle.put(FACILITATIONS, facilitations);

        int count = 0;
        int[] ids = new int[chapters.size()];
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

        bundle.put(MOVE_TIMEOUT, moveTimeoutIndex);

        bundle.put(SCRIPTS_DATA,
                LuaEngine.require(LuaEngine.SCRIPTS_LIB_STORAGE).get("serializeGameData").call().checkjstring());

        bundle.put(LAST_USED_ID, EntityIdSource.getNextId());
        CharsList.storeInBundle(bundle);
        bundle.put(MOD, ModdingMode.activeMod());

        OutputStream output = FileSystem.getOutputStream(fileName);
        Bundle.write(bundle, output);
        output.close();
    }

    @SneakyThrows
    private static void saveLevel(String saveTo, Level level) {
        Bundle bundle = new Bundle();
        bundle.put(LEVEL, level);

        bundle.put(SCRIPTS_DATA,
                LuaEngine.require(LuaEngine.SCRIPTS_LIB_STORAGE).get("serializeLevelData").call().checkjstring());


        OutputStream output = FileSystem.getOutputStream(saveTo);
        Bundle.write(bundle, output);
        output.close();
    }

    //@AddTrace(name = "Dungeon.saveAllImpl")
    private static void saveAllImpl() {
        float MBytesAvailable = Util.getAvailableInternalMemorySize() / 1024f / 1024f;

        if (MBytesAvailable < 2) {
            EventCollector.logEvent("saveGame", "lowMemory");
            Game.toast("Low memory condition");
        }

        if (level != null && hero.valid() && hero.isAlive()) {
            Level thisLevel = Dungeon.level;

            Actor.fixTime();
            try {
                SaveUtils.copySaveToSlot(SaveUtils.getPrevSave(), heroClass);

                Position current = currentPosition();

                String saveToLevel = getLevelSaveFile(current);
                String saveToGame = SaveUtils.gameFile(hero.getHeroClass());

                saveGame(saveToGame);
                saveLevel(saveToLevel, thisLevel);

                Library.saveLibrary();

                SaveUtils.copySaveToSlot(SaveUtils.getAutoSave(), heroClass);

                GamesInProgress.set(hero.getHeroClass(), depth, hero.lvl());
            } catch (IOException e) {
                Game.toast(StringsManager.getVar(R.string.Dungeon_saveIoError) + "\n" + e.getLocalizedMessage());
                EventCollector.logException(new Exception("cannot write save", e));
            }
        } else if (WndResurrect.instance != null) {

            WndResurrect.instance.hide();
            Hero.reallyDie(hero, WndResurrect.causeOfDeath);
        } else {
            EventCollector.logException(new Exception(Utils.format("spurious save: %s %s", String.valueOf(level), String.valueOf(hero))));
        }
    }

    @NotNull
    private static String getLevelSaveFile(Position current) {
        return SaveUtils.depthFileForSave(hero.getHeroClass(),
                DungeonGenerator.getLevelDepth(current.levelId),
                DungeonGenerator.getLevelKind(current.levelId),
                current.levelId);
    }

    public static void save(boolean force) {

        if (!force && SystemTime.now() - lastSaveTimestamp < 1000) {
            return;
        }

        if(! Dungeon.hero.isAlive()) {
            return;
        }

        GameLoop.loadingOrSaving.incrementAndGet();
        synchronized (GameLoop.stepLock) {
            saveAllImpl();
        }
        GameLoop.loadingOrSaving.decrementAndGet();

        lastSaveTimestamp = SystemTime.now();
    }

    //@AddTrace(name = "Dungeon.loadGame")
    public static void loadGame() throws IOException {
        loadGame(SaveUtils.gameFile(heroClass), true);
    }

    public static void loadGameForRankings(String fileName) throws IOException {
        loadGame(fileName, false);
    }

    private static void loadGameFromBundle(Bundle bundle, boolean fullLoad) {

        String saveMod = bundle.optString(MOD, ModdingMode.REMIXED);
        String activeMod = ModdingMode.activeMod();

        if (fullLoad && !saveMod.equals(activeMod)) {
            EventCollector.logException(new Exception(
                    Utils.format("loading save from another mod (save: %s, active: %s)", saveMod, activeMod)));
        }

        if(fullLoad) {
            reset();
        }

        Dungeon.gameId = bundle.optString(GAME_ID, Utils.UNKNOWN);
        EntityIdSource.setLastUsedId(bundle.optInt(LAST_USED_ID, 1));
        CharsList.restoreFromBundle(bundle);

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

        realtime = bundle.getBoolean(REALTIME);
        setChallenges(bundle.optInt(CHALLENGES, 0));
        setFacilitations(bundle.optInt(FACILITATIONS, 0));

        if (fullLoad) {
            chapters = new HashSet<>();
            int[] ids = bundle.getIntArray(CHAPTERS);
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

        String version = bundle.getString(VERSION);

        hero = (Hero) bundle.get(HERO);
        if (hero == null) {
            throw new TrackedRuntimeException("no hero in bundle");
        }

        depth = bundle.getInt(DEPTH);

        Statistics.restoreFromBundle(bundle);
        Journal.restoreFromBundle(bundle);
        Logbook.restoreFromBundle(bundle);
        LuaEngine.require(LuaEngine.SCRIPTS_LIB_STORAGE).get("deserializeGameData").call(bundle.getString(SCRIPTS_DATA));

        moveTimeoutIndex = GamePreferences.limitTimeoutIndex(bundle.optInt(MOVE_TIMEOUT, Integer.MAX_VALUE));
    }

    private static void loadGame(String fileName, boolean fullLoad) throws IOException {
        synchronized (GameLoop.stepLock) {
            try {
                GameLoop.loadingOrSaving.incrementAndGet();

                val bundle = gameBundle(fileName);
                if (bundle.isPresent()) {
                    loadGameFromBundle(bundle.get(), fullLoad);
                }

            } finally {
                GameLoop.loadingOrSaving.decrementAndGet();
            }
        }
    }

    @NotNull
    @SneakyThrows
    public static Level loadLevel(Position next) {
        synchronized (GameLoop.stepLock) {
            try {
                GameLoop.loadingOrSaving.incrementAndGet();

                String levelId = next.levelId;

                if (Dungeon.level != null) {
                    CharsList.remove(Dungeon.hero.getId());
                    for (var mob : Dungeon.level.mobs) {
                        CharsList.remove(mob.getId());
                    }
                    Dungeon.level = null;
                }

                DungeonGenerator.loadingLevel(next);

                String loadFrom = SaveUtils.depthFileForLoad(heroClass,
                        DungeonGenerator.getLevelDepth(levelId),
                        DungeonGenerator.getLevelKind(levelId),
                        levelId);

                GLog.toFile("loading level: %s", loadFrom);


                if (DungeonGenerator.isStatic(levelId)) {
                    return newLevel(next);
                }

                if (!FileSystem.getFile(loadFrom).exists()) {
                    return newLevel(next);
                }

                try (InputStream input = new FileInputStream(FileSystem.getFile(loadFrom))) {
                    Bundle bundle = Bundle.read(input);

                    Level level = (Level) bundle.get("level");
                    LuaEngine.require(LuaEngine.SCRIPTS_LIB_STORAGE).get("deserializeLevelData").call(bundle.getString(SCRIPTS_DATA));

                    if (level == null) {
                        level = newLevel(next);
                    }

                    level.levelId = next.levelId;
                    initSizeDependentStuff(level.getWidth(), level.getHeight());
                    return level;

                }
            } finally {
                GameLoop.loadingOrSaving.decrementAndGet();
            }
        }
    }

    public static void deleteGame(boolean deleteLevels) {
        GLog.toFile("deleteGame");
        SaveUtils.deleteGameFile(heroClass);

        if (deleteLevels) {
            SaveUtils.deleteLevels(heroClass);
        }

        GamesInProgress.delete(heroClass);
    }

    public static Optional<Bundle> gameBundle(String fileName) throws IOException {

        var saveFile = FileSystem.getFile(fileName);
        if (saveFile.exists()) {
            try (InputStream input = new FileInputStream(saveFile)) {
                return Optional.of(Bundle.read(input));
            }
        }
        return Optional.absent();
    }

    public static void preview(GamesInProgress.Info info, Bundle bundle) {
        info.depth = bundle.getInt(DEPTH);
        if (info.depth == -1) {
            info.depth = bundle.getInt("maxDepth"); // FIXME
        }
        Hero.preview(info, bundle.getBundle(HERO));
    }

    public static void fail(String desc) {
        if (hero.getBelongings().getItem(Ankh.class) == null) {
            Rankings.INSTANCE.submit(Rankings.gameOver.LOSE, desc);
        }
    }

    public static void win(String desc, gameOver kind) {
        if (getChallenges() != 0) {
            Badges.validateChampion();
        }
        Rankings.INSTANCE.submit(kind, desc);
    }


    public static void observe() {
        GameScene.observeRequest();
    }

    public static void observeImpl() {
        level.updateFieldOfView(hero.getControlTarget());
        System.arraycopy(level.fieldOfView, 0, visible, 0, visible.length);

        BArray.or(level.mapped, visible, level.mapped);

        GameScene.afterObserve();
    }

    private static void markActorsAsUnpassableIgnoreFov() {
        for (Char actor : Actor.chars.values()) {
            int pos = actor.getPos();
            passable[pos] = false;
        }
    }

    private static void markObjects(Char ch) {

        boolean ignoreDanger = ignoreDanger(ch);

        for (val objectLayer : level.objects.values()) {
            for (val object : objectLayer.values()) {
                int pos = object.getPos();

                if (!level.cellValid(pos)) {
                    EventCollector.logException("Invalid object " + object.getEntityKind());
                    level.remove(object);
                    continue;
                }

                if (object.nonPassable(ch)) {
                    passable[pos] = false;
                }

                if (!ignoreDanger && object.avoid()) {
                    passable[pos] = false;
                }

            }
        }
    }

    private static void markActorsAsUnpassable(Char ch, boolean[] visible) {
        int chPos = ch.getPos();
        for (Char actor : Actor.chars.values()) {
            int pos = actor.getPos();

            if (!level.cellValid(pos)) {
                GLog.debug("WTF?");
                return;
            }

            if (visible[pos] || level.adjacent(pos, chPos)) {
                if (actor instanceof Mob) {
                    passable[pos] = passable[pos] && !level.avoid[pos] && actor.getOwnerId() == ch.getId();
                }
            }
        }
    }


    public static int findPath(@NotNull Hero ch, int to, boolean[] pass, boolean[] visible) {

        int from = ch.getPos();

        if (level.adjacent(from, to)) {
            if (!(pass[to] || level.avoid[to])) {
                return Level.INVALID_CELL;
            }

            Char chr = Actor.findChar(to);

            if (chr instanceof Mob) {
                Mob mob = (Mob) chr;
                if (mob.isPet()) {
                    return to;
                }
            }
            return chr == null ? to : Level.INVALID_CELL;
        }

        if (ignoreDanger(ch)) {
            BArray.or(pass, level.avoid, passable);
        } else {
            BArray.and_not(pass, level.avoid, passable);
        }

        markActorsAsUnpassable(ch, visible);

        markObjects(ch);

        return PathFinder.getStep(from, to, passable);
    }

    public static boolean ignoreDanger(@NotNull Char ch) {
        return ch.isFlying() || ch.buffLevel(BuffFactory.AMOK) > 0;
    }


    public static int findPath(@NotNull Char ch, int to, boolean[] pass) {

        int from = ch.getPos();

        if (level.adjacent(from, to)) {

            if (!(pass[to] || level.avoid[to])) {
                return Level.INVALID_CELL;
            }

            return Actor.findChar(to) == null ? to : Level.INVALID_CELL;
        }

        if (ch.isFlying() || ch.buffLevel(BuffFactory.AMOK) > 0) {
            BArray.or(pass, level.avoid, passable);
        } else {
            BArray.and_not(pass, level.avoid, passable);
        }

        markActorsAsUnpassableIgnoreFov();

        markObjects(ch);

        return PathFinder.getStep(from, to, passable);

    }

    public static int flee(Char ch, int from, boolean[] pass) {

        int cur = ch.getPos();

        if (ch.isFlying()) {
            BArray.or(pass, level.avoid, passable);
        } else {
            System.arraycopy(pass, 0, passable, 0, level.getLength());
        }

        markActorsAsUnpassableIgnoreFov();
        markObjects(ch);

        passable[cur] = true;

        return PathFinder.getStepBack(cur, from, passable);
    }

    public static Position currentPosition() {
        return new Position(hero.levelId, hero.getPos());
    }


    public static boolean isLoading() {
        return hero.invalid() || level == null || GameLoop.loadingOrSaving.get() > 0;
    }

    public static boolean realtime() {
        return realtime;
    }

    public static double moveTimeout() {
        return MOVE_TIMEOUTS[moveTimeoutIndex];
    }

    public static void saveCurrentLevel() {
        saveLevel(getLevelSaveFile(currentPosition()), Dungeon.level);
    }

    public static int getChallenges() {
        return challenges;
    }


    public static void setFacilitation(int mask) {
        facilitations = facilitations | mask;
        setFacilitations(facilitations);
    }

    public static void resetFacilitation(int mask) {
        facilitations = facilitations & ~mask;
        setFacilitations(facilitations);
    }

    public static boolean setChallenge(int mask) {
        if (ModdingMode.inMod()) {
            if (mask == Challenges.NO_TOWN) {
                return false;
            }
        }

        challenges = challenges | mask;
        setChallenges(challenges);
        return true;
    }

    public static void resetChallenge(int mask) {
        challenges = challenges & ~mask;
        setChallenges(challenges);
    }

    public static int getFacilitations() {
        return facilitations;
    }

    public static void setChallenges(int challenges) {
        EventCollector.setSessionData(CHALLENGES, String.valueOf(challenges));
        Dungeon.challenges = challenges;
    }

    public static void setFacilitations(int facilitations) {
        EventCollector.setSessionData(FACILITATIONS, String.valueOf(facilitations));
        Dungeon.facilitations = facilitations;
    }

    public static boolean isPathVisible(int from, int to) {
        return isCellVisible(from) || isCellVisible(to);
    }

    @LuaInterface
    public static boolean isCellVisible(int cell) {
        if (level == null) {
            return false;
        }

        if (!level.cellValid(cell)) {
            EventCollector.logException(Utils.format("visibility check on %d", cell));
            return false;
        }
        return visible[cell];
    }

    public static boolean isNorthWallVisible(int cell) {
        if (!Dungeon.isIsometricMode()) {
            return isCellVisible(cell);
        }

        if (!isCellVisible(cell)) {
            return false;
        }

        int next_cell = cell + level.getWidth();
        if (!level.cellValid(next_cell)) {
            return true;
        }
        return isCellVisible(next_cell);
    }


    public static void onHeroLeaveLevel() {
        if (level != null) {
            level.unseal();
        }
    }

    public static void setIsometricMode(boolean isometricMode) {
        EventCollector.setSessionData("isometricMode", String.valueOf(isometricMode));
        Dungeon.isometricMode = isometricMode;
    }

    public static boolean isIsometricMode() {
        return isometricMode;
    }
}
