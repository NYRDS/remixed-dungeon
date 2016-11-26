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
package com.watabou.pixeldungeon.levels;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.nyrds.android.util.ModdingMode;
import com.nyrds.android.util.TrackedRuntimeException;
import com.nyrds.pixeldungeon.levels.objects.LevelObject;
import com.nyrds.pixeldungeon.levels.objects.Presser;
import com.nyrds.pixeldungeon.ml.EventCollector;
import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.pixeldungeon.mobs.elementals.AirElemental;
import com.nyrds.pixeldungeon.mobs.elementals.EarthElemental;
import com.nyrds.pixeldungeon.mobs.elementals.WaterElemental;
import com.nyrds.pixeldungeon.utils.DungeonGenerator;
import com.watabou.noosa.Game;
import com.watabou.noosa.Scene;
import com.watabou.noosa.audio.Sample;
import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.Challenges;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.Statistics;
import com.watabou.pixeldungeon.actors.Actor;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.blobs.Alchemy;
import com.watabou.pixeldungeon.actors.blobs.Blob;
import com.watabou.pixeldungeon.actors.blobs.WellWater;
import com.watabou.pixeldungeon.actors.buffs.Awareness;
import com.watabou.pixeldungeon.actors.buffs.Blindness;
import com.watabou.pixeldungeon.actors.buffs.Buff;
import com.watabou.pixeldungeon.actors.buffs.MindVision;
import com.watabou.pixeldungeon.actors.buffs.Shadows;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.actors.hero.HeroClass;
import com.watabou.pixeldungeon.actors.mobs.Bestiary;
import com.watabou.pixeldungeon.actors.mobs.Mob;
import com.watabou.pixeldungeon.effects.particles.FlowParticle;
import com.watabou.pixeldungeon.effects.particles.WindParticle;
import com.watabou.pixeldungeon.items.Generator;
import com.watabou.pixeldungeon.items.Gold;
import com.watabou.pixeldungeon.items.Heap;
import com.watabou.pixeldungeon.items.Item;
import com.watabou.pixeldungeon.items.Stylus;
import com.watabou.pixeldungeon.items.armor.Armor;
import com.watabou.pixeldungeon.items.food.Food;
import com.watabou.pixeldungeon.items.food.PseudoPasty;
import com.watabou.pixeldungeon.items.potions.PotionOfHealing;
import com.watabou.pixeldungeon.items.potions.PotionOfStrength;
import com.watabou.pixeldungeon.items.scrolls.ScrollOfUpgrade;
import com.watabou.pixeldungeon.levels.features.Chasm;
import com.watabou.pixeldungeon.levels.features.Door;
import com.watabou.pixeldungeon.levels.features.HighGrass;
import com.watabou.pixeldungeon.levels.painters.Painter;
import com.watabou.pixeldungeon.levels.traps.AlarmTrap;
import com.watabou.pixeldungeon.levels.traps.FireTrap;
import com.watabou.pixeldungeon.levels.traps.GrippingTrap;
import com.watabou.pixeldungeon.levels.traps.LightningTrap;
import com.watabou.pixeldungeon.levels.traps.ParalyticTrap;
import com.watabou.pixeldungeon.levels.traps.PoisonTrap;
import com.watabou.pixeldungeon.levels.traps.SummoningTrap;
import com.watabou.pixeldungeon.levels.traps.ToxicTrap;
import com.watabou.pixeldungeon.mechanics.ShadowCaster;
import com.watabou.pixeldungeon.plants.Plant;
import com.watabou.pixeldungeon.scenes.GameScene;
import com.watabou.pixeldungeon.utils.GLog;
import com.watabou.pixeldungeon.utils.Utils;
import com.watabou.utils.Bundlable;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;
import com.watabou.utils.SparseArray;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public abstract class Level implements Bundlable {

	public int getExit(Integer index) {
		if (hasExit(index)) {
			return exitMap.get(index);
		} else {
			throw new TrackedRuntimeException("no exit with index: " + index.toString());
		}
	}

	public boolean hasExit(Integer index) {
		return exitMap.containsKey(index);
	}

	public void setExit(int exit, Integer index) {
		exitMap.put(index, exit);
	}

	public boolean isExit(int pos) {
		return exitMap.containsValue(pos);
	}

	public int exitIndex(int pos) {
		for (Integer index : exitMap.keySet()) {
			if (exitMap.get(index) == pos) {
				return index;
			}
		}
		throw new TrackedRuntimeException(new Exception("no exit at this cell"));
	}

	public LevelObject getLevelObject(int pos) {
		return objects.get(pos);
	}

	public void onHeroDescend(int cell) {
	}

	@NonNull
	public String music() {
		String ret = DungeonGenerator.music(levelId);
		if (ret == null) {
			ret = Assets.TUNE;
		}
		return ret;
	}

	public Feeling getFeeling() {
		return feeling;
	}

	public void setFeeling(Feeling feeling) {
		this.feeling = feeling;
	}

	public enum Feeling {
		NONE, CHASM, WATER, GRASS, UNDEFINED
	}

	protected int width  = 32;
	protected int height = 32;

	public static int[] NEIGHBOURS4;
	public static int[] NEIGHBOURS8;
	public static int[] NEIGHBOURS9;

	private static final float TIME_TO_RESPAWN = 50;

	private static final String TXT_HIDDEN_PLATE_CLICKS = Game
			.getVar(R.string.Level_HiddenPlate);

	public int[]     map;
	public boolean[] visited;
	public boolean[] mapped;

	public int viewDistance = Dungeon.isChallenged(Challenges.DARKNESS) ? 3 : 8;

	public boolean[] fieldOfView;

	public boolean[] passable;
	public boolean[] losBlocking;
	public boolean[] flammable;
	public boolean[] secret;
	public boolean[] solid;
	public boolean[] avoid;
	public boolean[] water;
	public boolean[] pit;

	public boolean[] nearWalls;
	public boolean[] allCells;

	public boolean[] discoverable;

	protected Feeling feeling = Feeling.UNDEFINED;

	public int entrance;

	private HashMap<Integer, Integer> exitMap = new HashMap<>();

	public String levelId;

	public  HashSet<Mob>                         mobs    = new HashSet<>();
	public  HashMap<Class<? extends Blob>, Blob> blobs   = new HashMap<>();
	public  SparseArray<Plant>                   plants  = new SparseArray<>();
	private SparseArray<Heap>                    heaps   = new SparseArray<>();
	public  SparseArray<LevelObject>             objects = new SparseArray<>();

	protected ArrayList<Item> itemsToSpawn = new ArrayList<>();

	public int color1 = 0x004400;
	public int color2 = 0x88CC44;

	protected static boolean pitRoomNeeded    = false;
	protected static boolean weakFloorCreated = false;

	private static final String MAP            = "map";
	private static final String VISITED        = "visited";
	private static final String MAPPED         = "mapped";
	private static final String ENTRANCE       = "entrance";
	private static final String EXIT           = "exit";
	private static final String HEAPS          = "heaps";
	private static final String PLANTS         = "plants";
	private static final String MOBS           = "mobs";
	private static final String BLOBS          = "blobs";
	private static final String WIDTH          = "width";
	private static final String HEIGHT         = "height";
	private static final String SECONDARY_EXIT = "secondaryExit";
	private static final String OBJECTS        = "objects";

	public static Level fromBundle(Bundle bundle, String key) {
		return (Level) bundle.get(key);
	}

	public String levelKind() {
		return this.getClass().getSimpleName();
	}

	public Heap getHeap(int pos) {
		Heap heap = heaps.get(pos);
		if (heap != null) {
			if (heap.isEmpty()) {
				EventCollector.logEvent("bug", "level", Utils.format("Empty heap at pos %d", pos));
				return null;
			}
			return heap;
		}
		return null;
	}

	public void removeHeap(int pos) {
		heaps.remove(pos);
	}

	public List<Heap> allHeaps() {
		return heaps.values();
	}

	public int cell(int i, int j) {
		int cell = j * getWidth() + i;
		if (cellValid(cell)) {
			return cell;
		}
		return -1;
	}

	public void create() {
		create(32, 32);
	}

	protected void initSizeDependentStuff() {

		Dungeon.initSizeDependentStuff(getWidth(),getHeight());
		NEIGHBOURS4 = new int[]{-getWidth(), +1, +getWidth(), -1};
		NEIGHBOURS8 = new int[]{+1, -1, +getWidth(), -getWidth(),
				+1 + getWidth(), +1 - getWidth(), -1 + getWidth(),
				-1 - getWidth()};
		NEIGHBOURS9 = new int[]{0, +1, -1, +getWidth(), -getWidth(),
				+1 + getWidth(), +1 - getWidth(), -1 + getWidth(),
				-1 - getWidth()};

		map = new int[getLength()];
		visited = new boolean[getLength()];
		mapped = new boolean[getLength()];

		fieldOfView = new boolean[getLength()];

		passable = new boolean[getLength()];
		losBlocking = new boolean[getLength()];
		flammable = new boolean[getLength()];
		secret = new boolean[getLength()];
		solid = new boolean[getLength()];
		avoid = new boolean[getLength()];
		water = new boolean[getLength()];
		pit = new boolean[getLength()];

		nearWalls = new boolean[getLength()];
		allCells = new boolean[getLength()];

		discoverable = new boolean[getLength()];

		Blob.setWidth(getWidth());
		Blob.setHeight(getHeight());
	}

	public void create(int w, int h) {

		width = w;
		height = h;

		initSizeDependentStuff();

		if (!isBossLevel()) {
			addItemToSpawn(Generator.random(Generator.Category.FOOD));
			if (Dungeon.posNeeded()) {
				addItemToSpawn(new PotionOfStrength());
				Dungeon.potionOfStrength++;
			}
			if (Dungeon.soeNeeded()) {
				addItemToSpawn(new ScrollOfUpgrade());
				Dungeon.scrollsOfUpgrade++;
			}
			if (Dungeon.asNeeded()) {
				addItemToSpawn(new Stylus());
				Dungeon.arcaneStyli++;
			}

			if (Random.Int(5) == 0) {
				addItemToSpawn(Generator.random(Generator.Category.RANGED));
			}

			if (Random.Int(15) == 0) {
				addItemToSpawn(new PseudoPasty());
			}

			if (Random.Int(2) == 0) {
				addItemToSpawn(Generator.random(Generator.Category.BULLETS));
			}

			feeling = DungeonGenerator.getCurrentLevelFeeling(levelId);
			if (feeling == Feeling.UNDEFINED) {
				if (Dungeon.depth > 1) {
					switch (Random.Int(10)) {
						case 0:
							feeling = Feeling.CHASM;
							break;
						case 1:
							feeling = Feeling.WATER;
							break;
						case 2:
							feeling = Feeling.GRASS;
							break;
						default:
							feeling = Feeling.NONE;
					}
				}
			}
		}

		boolean pitNeeded = Dungeon.depth > 1 && weakFloorCreated;

		do {
			Arrays.fill(map, feeling == Feeling.CHASM ? Terrain.CHASM
					: Terrain.WALL);

			pitRoomNeeded = pitNeeded;
			weakFloorCreated = false;

		} while (!build());
		decorate();

		buildFlagMaps();
		cleanWalls();

		createMobs();
		createItems();
	}

	public void reset() {

		for (Mob mob : mobs.toArray(new Mob[mobs.size()])) {
			if (!mob.reset()) {
				mobs.remove(mob);
			}
		}
		createMobs();
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {

		mobs = new HashSet<>();
		heaps = new SparseArray<>();
		blobs = new HashMap<>();
		plants = new SparseArray<>();

		width = bundle.optInt(WIDTH, 32); // old levels compat
		height = bundle.optInt(HEIGHT, 32);

		initSizeDependentStuff();

		map = bundle.getIntArray(MAP);
		visited = bundle.getBooleanArray(VISITED);
		mapped = bundle.getBooleanArray(MAPPED);

		entrance = bundle.getInt(ENTRANCE);

		int exits[] = bundle.getIntArray(EXIT);
		if (exits != null) {
			for (int i = 0; i < exits.length; ++i) {
				setExit(exits[i], i);
			}
		} else {
			setExit(bundle.getInt(EXIT), 0);
			int secondaryExit = bundle.optInt(SECONDARY_EXIT, -1);
			if (cellValid(secondaryExit)) {
				setExit(secondaryExit, 1);
			}
		}

		weakFloorCreated = false;

		for (Heap heap : bundle.getCollection(HEAPS, Heap.class)) {
			heaps.put(heap.pos, heap);
		}

		for (Plant plant : bundle.getCollection(PLANTS, Plant.class)) {
			plants.put(plant.pos, plant);
		}

		for (LevelObject object : bundle.getCollection(OBJECTS, LevelObject.class)) {
			objects.put(object.getPos(), object);
		}

		for (Mob mob : bundle.getCollection(MOBS, Mob.class)) {
			if (mob != null && mob.getPos() != -1) {
				mobs.add(mob);
			}
		}

		for (Blob blob : bundle.getCollection(BLOBS, Blob.class)) {
			blobs.put(blob.getClass(), blob);
		}

		buildFlagMaps();
		cleanWalls();
	}

	public void removePets() {
		HashSet<Mob> nonPets = new HashSet<>();

		for (Mob mob : mobs) {
			if (!mob.isPet()) {
				nonPets.add(mob);
			}
		}

		mobs = nonPets;
	}

	@Override
	public void storeInBundle(Bundle bundle) {
		bundle.put(MAP, map);
		bundle.put(VISITED, visited);
		bundle.put(MAPPED, mapped);
		bundle.put(ENTRANCE, entrance);

		int[] exits = new int[exitMap.size()];

		for (int i = 0; i < exitMap.size(); ++i) {
			exits[i] = exitMap.get(i);
		}

		bundle.put(EXIT, exits);

		bundle.put(HEAPS, heaps.values());
		bundle.put(PLANTS, plants.values());
		bundle.put(OBJECTS, objects.values());
		bundle.put(MOBS, mobs);
		bundle.put(BLOBS, blobs.values());

		bundle.put(WIDTH, width);
		bundle.put(HEIGHT, height);
	}

	public boolean dontPack() {
		return false;
	}

	public int tunnelTile() {
		return feeling == Feeling.CHASM ? Terrain.EMPTY_SP : Terrain.EMPTY;
	}

	public String getTilesTex() {
		String tiles = DungeonGenerator.tiles(levelId);
		if (tiles != null) {
			return tiles;
		}

		if (tilesTexEx() == null) {
			return tilesTex();
		}

		if (ModdingMode.inMod() && !ModdingMode.isResourceExistInMod(tilesTexEx())
				&& ModdingMode.isResourceExistInMod(tilesTex())) {
			return tilesTex();
		}

		return tilesTexEx();
	}

	protected String tilesTex() {
		return Assets.TILES_SEWERS;
	}

	protected String tilesTexEx() {
		return null;
	}

	@NonNull
	public String getWaterTex() {
		String water = DungeonGenerator.water(levelId);
		if (water != null) {
			return water;
		}

		return waterTex();
	}

	protected String waterTex() {
		return null;
	}

	abstract protected boolean build();

	abstract protected void decorate();

	abstract protected void createMobs();

	abstract protected void createItems();

	public void addVisuals(Scene scene) {
		for (int i = 0; i < getLength(); i++) {
			if (pit[i]) {
				scene.add(new WindParticle.Wind(i));
				if (i >= getWidth() && water[i - getWidth()]) {
					scene.add(new FlowParticle.Flow(i - getWidth()));
				}
			}
		}
	}

	public int nMobs() {
		return 0;
	}

	public void spawnMob(Mob mob) {
		spawnMob(mob, 0);
	}

	public void spawnMob(Mob mob, float delay) {
		mobs.add(mob);
		if(GameScene.isSceneReady()) {
			mob.updateSprite();
		}
		Actor.addDelayed(mob, delay);
		Actor.occupyCell(mob);

		if(GameScene.isSceneReady()) {
			if(mob.isPet() || fieldOfView[mob.getPos()] ) {
				mobPress(mob);
			}
		}
	}

	protected Mob createMob() {
		Mob mob = null;

		if (Random.Int(5) == 0) {
			switch (feeling) {
				case WATER:
					mob = new WaterElemental();
					break;
				case CHASM:
					mob = new AirElemental();
					break;
				case GRASS:
					mob = new EarthElemental();
					break;
				case NONE:
					break;
				default:
					break;
			}
		}

		if (mob == null) {
			mob = Bestiary.mutable();
		}

		setMobSpawnPos(mob);

		return mob;
	}

	protected void setMobSpawnPos(Mob mob) {
		if (!mob.isWallWalker()) {
			mob.setPos(randomRespawnCell());
		} else {
			mob.setState(mob.WANDERING);
			mob.setPos(randomSolidCell());
		}
	}

	public Actor respawner() {
		if (isBossLevel()) {
			return null;
		}

		return new Actor() {
			@Override
			protected boolean act() {

				int hostileMobsCount = 0;
				for (Mob mob : mobs) {
					if (!mob.isPet()) {
						hostileMobsCount++;
					}
				}

				if (hostileMobsCount < nMobs()) {

					Mob mob = createMob();
					mob.setState(mob.WANDERING);
					if (Dungeon.hero.isAlive() && mob.getPos() != -1) {
						spawnMob(mob);
						if (Statistics.amuletObtained) {
							mob.beckon(Dungeon.hero.getPos());
						}
					}
				}
				spend(Dungeon.nightMode || Statistics.amuletObtained ? TIME_TO_RESPAWN / 2
						: TIME_TO_RESPAWN);
				return true;
			}
		};
	}

	public int randomSolidCell() {
		int cell;
		do {
			cell = Random.Int(getLength());
		} while (!solid[cell] || Dungeon.visible[cell]
				|| Actor.findChar(cell) != null);
		return cell;
	}

	public int randomRespawnCell() {

		if (isBossLevel()) {
			return -1;
		}

		int cell;
		do {
			cell = Random.Int(getLength());
		} while (!passable[cell] || Dungeon.visible[cell]
				|| Actor.findChar(cell) != null);
		return cell;
	}

	public int randomDestination() {
		int cell;
		do {
			cell = Random.Int(getLength());
		} while (!passable[cell]);
		return cell;
	}

	public void addItemToSpawn(Item item) {
		if (item != null) {
			itemsToSpawn.add(item);
		}
	}

	public Item itemToSpanAsPrize() {
		if (Random.Int(itemsToSpawn.size() + 1) > 0) {
			Item item = Random.element(itemsToSpawn);
			itemsToSpawn.remove(item);
			return item;
		} else {
			return null;
		}
	}

	protected void buildFlagMaps() {

		for (int i = 0; i < getLength(); i++) {
			int flags = TerrainFlags.flags[map[i]];
			passable[i] = (flags & TerrainFlags.PASSABLE) != 0;
			losBlocking[i] = (flags & TerrainFlags.LOS_BLOCKING) != 0;
			flammable[i] = (flags & TerrainFlags.FLAMABLE) != 0;
			secret[i] = (flags & TerrainFlags.SECRET) != 0;
			solid[i] = (flags & TerrainFlags.SOLID) != 0;
			avoid[i] = (flags & TerrainFlags.AVOID) != 0;
			water[i] = (flags & TerrainFlags.LIQUID) != 0;
			pit[i] = (flags & TerrainFlags.PIT) != 0;
		}

		int lastRow = getLength() - getWidth();
		for (int i = 0; i < getWidth(); i++) {
			passable[i] = avoid[i] = false;
			passable[lastRow + i] = avoid[lastRow + i] = false;
		}
		for (int i = getWidth(); i < lastRow; i += getWidth()) {
			passable[i] = avoid[i] = false;
			passable[i + getWidth() - 1] = avoid[i + getWidth() - 1] = false;
		}

		for (int i = getWidth(); i < getLength() - getWidth(); i++) {
			allCells[i] = true;
			nearWalls[i] = false;
			if (passable[i]) {
				int count = 0;
				for (int a : NEIGHBOURS4) {
					int tile = map[i + a];
					if (tile == Terrain.WALL || tile == Terrain.WALL_DECO) {
						count++;
					}
				}
				if (count > 0) {
					nearWalls[i] = true;
				}
			}

			if (water[i]) {
				int t = Terrain.WATER_TILES;
				for (int j = 0; j < NEIGHBOURS4.length; j++) {
					if ((TerrainFlags.flags[map[i + NEIGHBOURS4[j]]] & TerrainFlags.UNSTITCHABLE) != 0) {
						t += 1 << j;
					}
				}
				map[i] = t;
			}

			if (pit[i]) {
				if (!pit[i - getWidth()]) {
					int c = map[i - getWidth()];
					if (c == Terrain.EMPTY_SP || c == Terrain.STATUE_SP) {
						map[i] = Terrain.CHASM_FLOOR_SP;
					} else if (water[i - getWidth()]) {
						map[i] = Terrain.CHASM_WATER;
					} else if ((TerrainFlags.flags[c] & TerrainFlags.UNSTITCHABLE) != 0) {
						map[i] = Terrain.CHASM_WALL;
					} else {
						map[i] = Terrain.CHASM_FLOOR;
					}
				}
			}
		}
	}

	protected void cleanWalls() {
		for (int i = 0; i < getLength(); i++) {

			boolean d = false;

			for (int a : NEIGHBOURS9) {
				int n = i + a;
				if (n >= 0 && n < getLength() && map[n] != Terrain.WALL
						&& map[n] != Terrain.WALL_DECO) {
					d = true;
					break;
				}
			}

			if (d) {
				d = false;

				for (int a : NEIGHBOURS9) {
					int n = i + a;
					if (n >= 0 && n < getLength() && !pit[n]) {
						d = true;
						break;
					}
				}
			}

			discoverable[i] = d;
		}
	}

	public void set(int x, int y, int terrain) {
		int cell = cell(x, y);
		set(cell, terrain);
	}

	public void set(int cell, int terrain) {
		Painter.set(this, cell, terrain);

		int flags = TerrainFlags.flags[terrain];
		passable[cell] = (flags & TerrainFlags.PASSABLE) != 0;
		losBlocking[cell] = (flags & TerrainFlags.LOS_BLOCKING) != 0;
		flammable[cell] = (flags & TerrainFlags.FLAMABLE) != 0;
		secret[cell] = (flags & TerrainFlags.SECRET) != 0;
		solid[cell] = (flags & TerrainFlags.SOLID) != 0;
		avoid[cell] = (flags & TerrainFlags.AVOID) != 0;
		pit[cell] = (flags & TerrainFlags.PIT) != 0;
		water[cell] = terrain == Terrain.WATER
				|| terrain >= Terrain.WATER_TILES;
	}

	public Heap drop(Item item, int cell) {

		if (Dungeon.isChallenged(Challenges.NO_FOOD) && item instanceof Food) {
			item = new Gold(item.price());
		} else if (Dungeon.isChallenged(Challenges.NO_ARMOR)
				&& item instanceof Armor) {
			item = new Gold(item.price());
		} else if (Dungeon.isChallenged(Challenges.NO_HEALING)
				&& item instanceof PotionOfHealing) {
			item = new Gold(item.price());
		}

		if ((map[cell] == Terrain.ALCHEMY) && !(item instanceof Plant.Seed)) {
			int newCell = getEmptyCellNextTo(cell);
			if (cellValid(newCell)) {
				cell = newCell;
			}
		}

		Heap heap = heaps.get(cell);
		if (heap == null) {
			heap = new Heap();
			heap.pos = cell;
			if (map[cell] == Terrain.CHASM || pit[cell]) {
				if (GameScene.isSceneReady()) {
					GameScene.discard(heap);
				}
			} else {
				heaps.put(cell, heap);
				if (GameScene.isSceneReady()) {
					GameScene.add(heap);
				}
			}

		} else if (heap.type == Heap.Type.LOCKED_CHEST
				|| heap.type == Heap.Type.CRYSTAL_CHEST) {

			int n;
			do {
				n = cell + Level.NEIGHBOURS8[Random.Int(8)];
			} while (!Dungeon.level.passable[n] && !Dungeon.level.avoid[n]);
			return drop(item, n);

		}
		heap.drop(item);

		if (Dungeon.level != null) {
			itemPress(cell, item);
		}

		return heap;
	}

	public void levelObjectMoved(LevelObject obj) {
		remove(obj);
		objects.put(obj.getPos(), obj);
	}

	public void addLevelObject(LevelObject obj) {
		objects.put(obj.getPos(), obj);

		if (GameScene.isSceneReady()) {
			GameScene.add(obj);
		}
	}

	public void plant(Plant.Seed seed, int pos) {

		Plant plant = plants.get(pos);
		if (plant != null) {
			plant.wither();
		}

		plant = seed.couch(pos);
		plants.put(pos, plant);
		if (GameScene.isSceneReady()) {
			GameScene.add(plant);
		}
	}

	public void remove(LevelObject levelObject) {
		int index = objects.indexOfValue(levelObject);

		if (index >= 0) {
			objects.remove(objects.keyAt(index));
		}
	}

	public void uproot(int pos) {
		plants.delete(pos);
	}

	public int pitCell() {
		return randomRespawnCell();
	}

	public void press(int cell, Char obj) {
		if (obj instanceof Hero) {
			pressHero(cell, (Hero) obj);
		}

		if (obj instanceof Mob) {
			mobPress((Mob) obj);
		}
	}

	public void pressHero(int cell, Hero hero) {

		if (pit[cell]) {
			Chasm.heroFall(cell, hero);
			return;
		}

		if (TerrainFlags.is(map[cell], TerrainFlags.TRAP)) {
			GLog.i(TXT_HIDDEN_PLATE_CLICKS);
			set(cell, Terrain.discover(map[cell]));
		}

		charPress(cell, hero);
	}

	public boolean objectPress(int cell, LevelObject levelObject) {
		if(map[cell] == Terrain.CHASM || pit[cell]) {
			levelObject.fall();
			return false;
		}
		itemPress(cell, levelObject);
		return true;
	}


	public void itemPress(int cell, Presser presser) {

		if(presser.affectLevelObjects()) {
			LevelObject levelObject = objects.get(cell);
			if (levelObject != null) {

				levelObject.bump();
			}
		}
		charPress(cell, null);
	}

	public void charPress(int cell, @Nullable Char actor) {

		switch (map[cell]) {
			case Terrain.TOXIC_TRAP:
				ToxicTrap.trigger(cell, actor);
				break;

			case Terrain.FIRE_TRAP:
				FireTrap.trigger(cell, actor);
				break;

			case Terrain.PARALYTIC_TRAP:
				ParalyticTrap.trigger(cell, actor);
				break;

			case Terrain.POISON_TRAP:
				PoisonTrap.trigger(cell, actor);
				break;

			case Terrain.ALARM_TRAP:
				AlarmTrap.trigger(cell, actor);
				break;

			case Terrain.LIGHTNING_TRAP:
				LightningTrap.trigger(cell, actor);
				break;

			case Terrain.GRIPPING_TRAP:
				GrippingTrap.trigger(cell, actor);
				break;

			case Terrain.SUMMONING_TRAP:
				SummoningTrap.trigger(cell, actor);
				break;

			case Terrain.DOOR:
				Door.enter(cell);
				break;
		}

		if (!(actor instanceof Mob)) {
			switch (map[cell]) {
				case Terrain.HIGH_GRASS:
					HighGrass.trample(this, cell, actor);
					break;

				case Terrain.WELL:
					WellWater.affectCell(cell);
					break;

				case Terrain.ALCHEMY:
					Alchemy.transmute(cell);
					break;
			}
		}

		if (TerrainFlags.is(map[cell], TerrainFlags.TRAP)) {
			if (Dungeon.visible[cell]) {
				Sample.INSTANCE.play(Assets.SND_TRAP);
			}

			if (actor instanceof Hero) {
				((Hero) actor).interrupt();
			}

			set(cell, Terrain.INACTIVE_TRAP);
			GameScene.updateMap(cell);
		}

		Plant plant = plants.get(cell);
		if (plant != null) {
			plant.activate(actor);
		}
	}

	public void mobPress(Mob mob) {

		int cell = mob.getPos();

		if (pit[cell] && !mob.flying) {
			Chasm.mobFall(mob);
			return;
		}

		charPress(cell, mob);
	}

	private void markFovCellSafe(int p) {
		if (p > 0 && p < fieldOfView.length) {
			fieldOfView[p] = true;
		}
	}

	private void updateFovForObjectAt(int p) {
		for (int a : NEIGHBOURS9) {
			markFovCellSafe(p + a);
		}
	}

	public boolean[] updateFieldOfView(Char c) {

		int cx = c.getPos() % getWidth();
		int cy = c.getPos() / getWidth();

		boolean sighted = c.buff(Blindness.class) == null
				&& c.buff(Shadows.class) == null && c.isAlive();
		if (sighted) {
			ShadowCaster.castShadow(cx, cy, fieldOfView, c.viewDistance);
		} else {
			Arrays.fill(fieldOfView, false);
		}

		int sense = 1;
		if (c.isAlive()) {
			for (Buff b : c.buffs(MindVision.class)) {
				sense = Math.max(((MindVision) b).distance, sense);
			}
		}

		if (!sighted || sense > 1) {

			int ax = Math.max(0, cx - sense);
			int bx = Math.min(cx + sense, getWidth() - 1);
			int ay = Math.max(0, cy - sense);
			int by = Math.min(cy + sense, getHeight() - 1);

			int len = bx - ax + 1;
			int pos = ax + ay * getWidth();
			for (int y = ay; y <= by; y++, pos += getWidth()) {
				Arrays.fill(fieldOfView, pos, pos + len, true);
			}

			for (int i = 0; i < getLength(); i++) {
				fieldOfView[i] &= discoverable[i];
			}
		}

		if (c.isAlive()) {
			if (c.buff(MindVision.class) != null) {
				for (Mob mob : mobs) {
					updateFovForObjectAt(mob.getPos());
				}
			} else if (c == Dungeon.hero
					&& ((Hero) c).heroClass == HeroClass.HUNTRESS) {
				for (Mob mob : mobs) {
					int p = mob.getPos();
					if (distance(c.getPos(), p) == 2) {
						updateFovForObjectAt(p);
					}
				}
			}
			if (c.buff(Awareness.class) != null) {
				for (Heap heap : heaps.values()) {
					updateFovForObjectAt(heap.pos);
				}
			}
		}

		return fieldOfView;
	}

	public int distance(int a, int b) {
		int ax = a % getWidth();
		int ay = a / getWidth();
		int bx = b % getWidth();
		int by = b / getWidth();
		return Math.max(Math.abs(ax - bx), Math.abs(ay - by));
	}

	public boolean adjacent(int a, int b) {
		int diff = Math.abs(a - b);
		return diff == 1 || diff == getWidth() || diff == getWidth() + 1
				|| diff == getWidth() - 1;
	}

	public String tileName(int tile) {

		if (tile >= Terrain.WATER_TILES) {
			return Game.getVar(R.string.Level_TileWater);
		}

		if (tile != Terrain.CHASM && (TerrainFlags.flags[tile] & TerrainFlags.PIT) != 0) {
			return tileName(Terrain.CHASM);
		}

		switch (tile) {
			case Terrain.CHASM:
				return Game.getVar(R.string.Level_TileChasm);
			case Terrain.EMPTY:
			case Terrain.EMPTY_SP:
			case Terrain.EMPTY_DECO:
			case Terrain.SECRET_TOXIC_TRAP:
			case Terrain.SECRET_FIRE_TRAP:
			case Terrain.SECRET_PARALYTIC_TRAP:
			case Terrain.SECRET_POISON_TRAP:
			case Terrain.SECRET_ALARM_TRAP:
			case Terrain.SECRET_LIGHTNING_TRAP:
				return Game.getVar(R.string.Level_TileFloor);
			case Terrain.GRASS:
				return Game.getVar(R.string.Level_TileGrass);
			case Terrain.WATER:
				return Game.getVar(R.string.Level_TileWater);
			case Terrain.WALL:
			case Terrain.WALL_DECO:
			case Terrain.SECRET_DOOR:
				return Game.getVar(R.string.Level_TileWall);
			case Terrain.DOOR:
				return Game.getVar(R.string.Level_TileClosedDoor);
			case Terrain.OPEN_DOOR:
				return Game.getVar(R.string.Level_TileOpenDoor);
			case Terrain.ENTRANCE:
				return Game.getVar(R.string.Level_TileEntrance);
			case Terrain.EXIT:
				return Game.getVar(R.string.Level_TileExit);
			case Terrain.EMBERS:
				return Game.getVar(R.string.Level_TileEmbers);
			case Terrain.LOCKED_DOOR:
				return Game.getVar(R.string.Level_TileLockedDoor);
			case Terrain.PEDESTAL:
				return Game.getVar(R.string.Level_TilePedestal);
			case Terrain.BARRICADE:
				return Game.getVar(R.string.Level_TileBarricade);
			case Terrain.HIGH_GRASS:
				return Game.getVar(R.string.Level_TileHighGrass);
			case Terrain.LOCKED_EXIT:
				return Game.getVar(R.string.Level_TileLockedExit);
			case Terrain.UNLOCKED_EXIT:
				return Game.getVar(R.string.Level_TileUnlockedExit);
			case Terrain.SIGN:
				return Game.getVar(R.string.Level_TileSign);
			case Terrain.WELL:
				return Game.getVar(R.string.Level_TileWell);
			case Terrain.EMPTY_WELL:
				return Game.getVar(R.string.Level_TileEmptyWell);
			case Terrain.STATUE:
			case Terrain.STATUE_SP:
				return Game.getVar(R.string.Level_TileStatue);
			case Terrain.TOXIC_TRAP:
				return Game.getVar(R.string.Level_TileToxicTrap);
			case Terrain.FIRE_TRAP:
				return Game.getVar(R.string.Level_TileFireTrap);
			case Terrain.PARALYTIC_TRAP:
				return Game.getVar(R.string.Level_TileParalyticTrap);
			case Terrain.POISON_TRAP:
				return Game.getVar(R.string.Level_TilePoisonTrap);
			case Terrain.ALARM_TRAP:
				return Game.getVar(R.string.Level_TileAlarmTrap);
			case Terrain.LIGHTNING_TRAP:
				return Game.getVar(R.string.Level_TileLightningTrap);
			case Terrain.GRIPPING_TRAP:
				return Game.getVar(R.string.Level_TileGrippingTrap);
			case Terrain.SUMMONING_TRAP:
				return Game.getVar(R.string.Level_TileSummoningTrap);
			case Terrain.INACTIVE_TRAP:
				return Game.getVar(R.string.Level_TileInactiveTrap);
			case Terrain.BOOKSHELF:
				return Game.getVar(R.string.Level_TileBookshelf);
			case Terrain.ALCHEMY:
				return Game.getVar(R.string.Level_TileAlchemy);
			default:
				return Game.getVar(R.string.Level_TileDefault);
		}
	}

	public String tileDesc(int tile) {

		switch (tile) {
			case Terrain.CHASM:
				return Game.getVar(R.string.Level_TileDescChasm);
			case Terrain.WATER:
				return Game.getVar(R.string.Level_TileDescWater);
			case Terrain.ENTRANCE:
				return Game.getVar(R.string.Level_TileDescEntrance);
			case Terrain.EXIT:
			case Terrain.UNLOCKED_EXIT:
				return Game.getVar(R.string.Level_TileDescExit);
			case Terrain.EMBERS:
				return Game.getVar(R.string.Level_TileDescEmbers);
			case Terrain.HIGH_GRASS:
				return Game.getVar(R.string.Level_TileDescHighGrass);
			case Terrain.LOCKED_DOOR:
				return Game.getVar(R.string.Level_TileDescLockedDoor);
			case Terrain.LOCKED_EXIT:
				return Game.getVar(R.string.Level_TileDescLockedExit);
			case Terrain.BARRICADE:
				return Game.getVar(R.string.Level_TileDescBarricade);
			case Terrain.SIGN:
				return Game.getVar(R.string.Level_TileDescSign);
			case Terrain.TOXIC_TRAP:
			case Terrain.FIRE_TRAP:
			case Terrain.PARALYTIC_TRAP:
			case Terrain.POISON_TRAP:
			case Terrain.ALARM_TRAP:
			case Terrain.LIGHTNING_TRAP:
			case Terrain.GRIPPING_TRAP:
			case Terrain.SUMMONING_TRAP:
				return Game.getVar(R.string.Level_TileDescTrap);
			case Terrain.INACTIVE_TRAP:
				return Game.getVar(R.string.Level_TileDescInactiveTrap);
			case Terrain.STATUE:
			case Terrain.STATUE_SP:
				return Game.getVar(R.string.Level_TileDescStatue);
			case Terrain.ALCHEMY:
				return Game.getVar(R.string.Level_TileDescAlchemy);
			case Terrain.EMPTY_WELL:
				return Game.getVar(R.string.Level_TileDescEmptyWell);
			default:
				if (tile >= Terrain.WATER_TILES) {
					return tileDesc(Terrain.WATER);
				}
				if ((TerrainFlags.flags[tile] & TerrainFlags.PIT) != 0) {
					return tileDesc(Terrain.CHASM);
				}
				return "";
		}
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	public int getLength() {
		return width * height;
	}

	public boolean cellValid(int x, int y) {
		return x > 0 && y > 0 && x < getWidth() - 1 && y < getHeight() - 1;
	}

	public boolean cellValid(int cell) {
		return cell >= 0 && cell < getLength();
	}

	public int getSolidCellNextTo(int cell) {
		ArrayList<Integer> candidates = new ArrayList<>();

		for (int n : Level.NEIGHBOURS8) {
			int p = n + cell;
			if (cellValid(p) && (solid[p]) && Actor.findChar(p) == null) {
				candidates.add(p);
			}
		}

		return oneCellFrom(candidates);
	}

	public int getEmptyCellNextTo(int cell) {
		ArrayList<Integer> candidates = new ArrayList<>();

		for (int n : Level.NEIGHBOURS8) {
			int p = n + cell;
			if (cellValid(p) && (avoid[p] || passable[p]) && Actor.findChar(p) == null) {
				candidates.add(p);
			}
		}

		return oneCellFrom(candidates);
	}

	public boolean isBossLevel() {
		return false;
	}

	public void seal() {
	}

	public void unseal() {
	}

	public int getDistToNearestTerrain(int cell, int terr) {
		int minima = getLength();
		for (int i = 0; i < getLength(); i++) {
			if (map[i] == terr) {
				int delta = distance(cell, i);
				if (delta < minima) {
					minima = delta;
				}
			}
		}
		return minima;
	}

	public int getDistToNearestTerrain(int x, int y, int terr) {
		return getDistToNearestTerrain(cell(x, y), terr);
	}

	public int getNearestTerrain(int x, int y, int terr) {
		int minima = getDistToNearestTerrain(x, y, terr);

		ArrayList<Integer> candidates = new ArrayList<>();

		int cell = cell(x, y);
		for (int i = 0; i < getLength(); i++) {
			if (map[i] == terr) {
				int delta = distance(cell, i);
				if (delta == minima) {
					candidates.add(i);
				}
			}
		}

		return oneCellFrom(candidates);
	}

	private int oneCellFrom(ArrayList<Integer> candidates) {
		if (!candidates.isEmpty()) {
			return Random.element(candidates);
		}

		return -1;
	}

	public int getRandomTerrainCell(int terrainType) {
		ArrayList<Integer> candidates = new ArrayList<>();

		for (int i = 0; i < getLength(); i++) {
			if (map[i] == terrainType) {
				candidates.add(i);
			}
		}

		return oneCellFrom(candidates);
	}

	public int get(int i, int j) {
		int cell = cell(i, j);
		if (cellValid(cell)) {
			return map[cell];
		}
		return -1;
	}

	public int cellX(int cell) {
		return cell % width;
	}

	public int cellY(int cell) {
		return cell / width;
	}

	public void fillAreaWith(Class<? extends Blob> blobClass, int cell, int xs, int ys, int amount) {
		fillAreaWith(blobClass, cellX(cell), cellY(cell), xs, ys, amount);
	}

	public void fillAreaWith(Class<? extends Blob> blobClass, int x, int y, int xs, int ys, int amount) {
		Blob blob = Dungeon.level.blobs.get(blobClass);
		if (blob == null) {
			try {
				blob = blobClass.newInstance();
			} catch (Exception e) {
				throw new TrackedRuntimeException(e);
			}
			GameScene.add(blob);
		}

		for (int i = x; i <= x + xs; i++) {
			for (int j = y; j <= y + ys; j++) {
				if (cellValid(i, j)) {
					blob.seed(i, j, amount);
				}
			}
		}
		blobs.put(blobClass, blob);
	}

	public void clearAreaFrom(Class<? extends Blob> blobClass, int cell, int xs, int ys) {
		clearAreaFrom(blobClass, cellX(cell), cellY(cell), xs, ys);
	}

	public void clearAreaFrom(Class<? extends Blob> blobClass, int x, int y, int xs, int ys) {
		Blob blob = Dungeon.level.blobs.get(blobClass);
		if (blob == null) {
			return;
		}

		for (int i = x; i <= x + xs; i++) {
			for (int j = y; j <= y + ys; j++) {
				if (cellValid(i, j)) {
					blob.clearBlob(cell(i, j));
				}
			}
		}
	}

	public int blobAmountAt(Class<? extends Blob> blobClass, int cell) {
		Blob blob = Dungeon.level.blobs.get(blobClass);
		if (blob == null) {
			return 0;
		}

		return blob.cur[cell];
	}

	public Mob getRandomMob() {
		return Random.element(mobs);
	}
}
