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

import android.support.annotation.NonNull;

import com.nyrds.android.util.FileSystem;
import com.nyrds.android.util.Scrambler;
import com.nyrds.pixeldungeon.ml.EventCollector;
import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.pixeldungeon.mobs.npc.AzuterronNPC;
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
import com.watabou.pixeldungeon.utils.BArray;
import com.watabou.pixeldungeon.utils.GLog;
import com.watabou.pixeldungeon.windows.WndResurrect;
import com.watabou.utils.Bundle;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;

public class Dungeon {

	public static int     potionOfStrength;
	public static int     scrollsOfUpgrade;
	public static int     arcaneStyli;
	public static boolean dewVial; // true if the dew vial can be spawned
	public static int     transmutation; // depth number for a well of transmutation

	public static int challenges;

	public static Hero  hero;
	public static Level level;

	public static int depth;
	private static int scrambledGold;
	private static boolean loading = false;


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

		Room.shuffleTypes();

		hero = new Hero(difficulty);

		Badges.reset();

		heroClass.initHero(hero);

		hero.levelKind = DungeonGenerator.getEntryLevelKind();
		hero.levelId = DungeonGenerator.getEntryLevel();

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
		GLog.toFile("creating level: %s %s %d", pos.levelId, pos.levelKind, pos.levelDepth);
		Level level = DungeonGenerator.createLevel(pos);

		Statistics.qualifiedForNoKilling = !level.isBossLevel();

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

	public static boolean shopOnLevel() {
		if (hero.levelKind.equals("NecroLevel")){
			return false;
		} else{
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
		hero.levelKind = level.levelKind();
		hero.levelId = levelId;

		if (!level.cellValid(hero.getPos())) {
			hero.setPos(level.entrance);
		}

		Light light = hero.buff(Light.class);
		hero.viewDistance = light == null ? level.viewDistance : Math.max(Light.DISTANCE, level.viewDistance);

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

	private static final String VERSION    = "version";
	private static final String CHALLENGES = "challenges";
	private static final String HERO       = "hero";
	private static final String GOLD       = "gold";
	private static final String DEPTH      = "depth";
	private static final String LEVEL      = "level";
	private static final String POS        = "potionsOfStrength";
	private static final String SOU        = "scrollsOfEnhancement";
	private static final String AS         = "arcaneStyli";
	private static final String DV         = "dewVial";
	private static final String WT         = "transmutation";
	private static final String CHAPTERS   = "chapters";
	private static final String QUESTS     = "quests";
	private static final String BADGES     = "badges";

	public static void gameOver() {
		Dungeon.deleteGame(true);
	}

	public static void saveGame(String fileName) throws IOException {
		Bundle bundle = new Bundle();

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
		bundle.put(QUESTS, quests);

		Room.storeRoomsInBundle(bundle);

		Statistics.storeInBundle(bundle);
		Journal.storeInBundle(bundle);

		Scroll.save(bundle);
		Potion.save(bundle);
		Wand.save(bundle);
		Ring.save(bundle);

		Bundle badges = new Bundle();
		Badges.saveLocal(badges);
		bundle.put(BADGES, badges);

		GLog.toFile("saving game: %s", fileName);

		OutputStream output = new FileOutputStream(FileSystem.getInteralStorageFile(fileName));
		Bundle.write(bundle, output);
		output.close();
	}

	public static void saveLevel() throws IOException {
		Bundle bundle = new Bundle();
		bundle.put(LEVEL, level);

		Position current = currentPosition();

		String saveTo = SaveUtils.depthFileForSave(hero.heroClass, current.levelDepth, current.levelKind,
				current.levelId);

		GLog.toFile("saving level: %s", saveTo);

		OutputStream output = new FileOutputStream(FileSystem.getInteralStorageFile(saveTo));
		Bundle.write(bundle, output);
		output.close();
	}

	public static void saveAll() throws IOException {
		float MBytesAvaliable = Game.getAvailableInternalMemorySize() / 1024f / 1024f;

		if (MBytesAvaliable < 2) {
			Game.toast("Low memory condition");
			GLog.toFile("Low memory!!!");
		}

		GLog.toFile("Saving: %5.2f MBytes available", MBytesAvaliable);

		if (hero.isAlive()) {

			Actor.fixTime();
			saveGame(SaveUtils.gameFile(hero.heroClass));
			saveLevel();

			GamesInProgress.set(hero.heroClass, depth, hero.lvl());

		} else if (WndResurrect.instance != null) {

			WndResurrect.instance.hide();
			Hero.reallyDie(WndResurrect.causeOfDeath);
		}
	}

	public static void loadGame() throws IOException {
		GLog.toFile("load Game");
		loadGame(SaveUtils.gameFile(heroClass), true);
	}

	public static void loadGameForRankings(String fileName) throws IOException {
		loadGame(fileName, false);
	}

	public static void loadGameFromBundle(Bundle bundle, boolean fullLoad) {
		Dungeon.challenges = bundle.getInt(CHALLENGES);

		Dungeon.level = null;
		Dungeon.depth = -1;

		Scroll.restore(bundle);
		Potion.restore(bundle);
		Wand.restore(bundle);
		Ring.restore(bundle);

		potionOfStrength = bundle.getInt(POS);
		scrollsOfUpgrade = bundle.getInt(SOU);
		arcaneStyli = bundle.getInt(AS);
		dewVial = bundle.getBoolean(DV);
		transmutation = bundle.getInt(WT);

		if (fullLoad) {
			chapters = new HashSet<>();
			int ids[] = bundle.getIntArray(CHAPTERS);
			if (ids != null) {
				for (int id : ids) {
					chapters.add(id);
				}
			}

			Bundle quests = bundle.getBundle(QUESTS);
			if (!quests.isNull()) {
				Ghost.Quest.restoreFromBundle(quests);
				WandMaker.Quest.restoreFromBundle(quests);
				Blacksmith.Quest.restoreFromBundle(quests);
				Imp.Quest.restoreFromBundle(quests);
				AzuterronNPC.Quest.restoreFromBundle(quests);
				ScarecrowNPC.Quest.restoreFromBundle(quests);
			} else {
				Ghost.Quest.reset();
				WandMaker.Quest.reset();
				Blacksmith.Quest.reset();
				Imp.Quest.reset();
				AzuterronNPC.Quest.reset();
				ScarecrowNPC.Quest.reset();
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
	}

	public static void loadGame(String fileName, boolean fullLoad) throws IOException {
		GLog.toFile("load Game %s", fileName);

		Bundle bundle = gameBundle(fileName);

		loadGameFromBundle(bundle, fullLoad);
	}

	public static Level loadLevel(Position next) throws IOException {
		loading = true;

		DungeonGenerator.loadingLevel(next);

		String loadFrom = SaveUtils.depthFileForLoad(heroClass, next.levelDepth, next.levelKind, next.levelId);

		GLog.toFile("loading level: %s", loadFrom);

		InputStream input;

		if (FileSystem.getFile(loadFrom).exists()) {
			input = new FileInputStream(FileSystem.getFile(loadFrom));
			Dungeon.level = null;
		} else {
			GLog.toFile("File %s not found!", loadFrom);
			return newLevel(next);
		}

		Bundle bundle = Bundle.read(input);

		input.close();

		if (bundle == null) {
			EventCollector.logEvent("Dungeon.loadLevel","read fail");
			return newLevel(next);
		}

		Level level = Level.fromBundle(bundle, "level");

		if(level == null) {
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

	private static void markActorsAsUnpassable(boolean[] visible) {
		for (Actor actor : Actor.all()) {
			if (actor instanceof Char) {
				int pos = ((Char) actor).getPos();
				if (visible[pos]) {
					passable[pos] = false;
				}
			}
		}
	}

	public static int findPath(Char ch, int from, int to, boolean pass[], boolean[] visible) {

		if (level.adjacent(from, to)) {
			return Actor.findChar(to) == null && (pass[to] || level.avoid[to]) ? to : -1;
		}

		if (ch.flying || ch.buff(Amok.class) != null) {
			BArray.or(pass, level.avoid, passable);
		} else {
			System.arraycopy(pass, 0, passable, 0, level.getLength());
		}

		if (visible != null) {
			markActorsAsUnpassable(visible);
		} else {
			markActorsAsUnpassableIgnoreFov();
		}

		return PathFinder.getStep(from, to, passable);

	}

	public static int flee(Char ch, int cur, int from, boolean pass[], boolean[] visible) {

		if (ch.flying) {
			BArray.or(pass, level.avoid, passable);
		} else {
			System.arraycopy(pass, 0, passable, 0, level.getLength());
		}

		if (visible != null) {
			markActorsAsUnpassable(visible);
		} else {
			markActorsAsUnpassableIgnoreFov();
		}

		passable[cur] = true;

		return PathFinder.getStepBack(cur, from, passable);
	}

	public static void challengeAllMobs(Char ch, String sound) {

		if (Dungeon.level == null) {
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
		return new Position(hero.levelKind, hero.levelId, depth, hero.getPos());
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
}
