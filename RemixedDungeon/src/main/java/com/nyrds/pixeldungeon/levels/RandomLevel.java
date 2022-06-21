package com.nyrds.pixeldungeon.levels;

import androidx.annotation.Keep;

import com.nyrds.Packable;
import com.nyrds.pixeldungeon.items.common.ItemFactory;
import com.nyrds.pixeldungeon.mobs.common.MobFactory;
import com.nyrds.pixeldungeon.utils.DungeonGenerator;
import com.nyrds.platform.EventCollector;
import com.nyrds.util.ModdingMode;
import com.watabou.pixeldungeon.actors.mobs.Mob;
import com.watabou.pixeldungeon.items.Item;
import com.watabou.pixeldungeon.levels.Patch;
import com.watabou.pixeldungeon.levels.RegularLevel;
import com.watabou.pixeldungeon.levels.Room;
import com.watabou.pixeldungeon.levels.Terrain;
import com.watabou.utils.Random;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import clone.org.json.JSONArray;
import clone.org.json.JSONException;
import clone.org.json.JSONObject;
import lombok.SneakyThrows;

public class RandomLevel extends RegularLevel {

	@Packable
	private int mobsSpawned = 0;

	@Keep
	public RandomLevel() {
		super();
	}

	public RandomLevel(String fileName) {
		mDescFile = fileName;
		readDescFile(mDescFile);
	}


	@Override
	protected void decorate() {
	}

	@Override
	public void create(int w, int h) {

		try {
			width = mLevelDesc.optInt("width",w);
			height = mLevelDesc.optInt("height",h);

			initSizeDependentStuff();

			feeling = DungeonGenerator.getLevelFeeling(levelId);

			if (mLevelDesc.has("items")) {
				JSONArray itemsDesc = mLevelDesc.getJSONArray("items");

				for (int i = 0; i < itemsDesc.length(); ++i) {
					JSONObject itemDesc = itemsDesc.optJSONObject(i);
					Item item = ItemFactory.createItemFromDesc(itemDesc);
					addItemToSpawn(item);
				}
			}

		} catch (JSONException e) {
			throw ModdingMode.modException("RandomLevel",e);
		}


		if (noBuild()) return;

		do {
			Arrays.fill(map, feeling == Feeling.CHASM ? Terrain.CHASM
					: Terrain.WALL);
		} while (!build());

		buildFlagMaps();
		cleanWalls();
		createMobs();
		createItems();
		createScript();
	}

	@Override
	@SneakyThrows
	public Mob createMob() {
		try {
			if (mLevelDesc.has("mobs")) {

				JSONArray mobsDesc = mLevelDesc.getJSONArray("mobs");
				if (mobsSpawned < mobsDesc.length()) {
					JSONObject mobDesc = mobsDesc.optJSONObject(mobsSpawned);

					mobsSpawned++;

					String kind = mobDesc.getString("kind");
					Mob mob = MobFactory.mobByName(kind);

					mob.fromJson(mobDesc);
					setMobSpawnPos(mob);

					return mob;
				}
			}
		} catch (JSONException e) {
			throw ModdingMode.modException("invalid mob desc",e);
		}
		return super.createMob();
	}

	@Override
	protected void assignRoomType() {

		JSONArray roomsDesc = mLevelDesc.optJSONArray("rooms");

		List<Room.Type> neededRooms = new ArrayList<>();

		if (roomsDesc != null) {
			for (int i = 0; i < roomsDesc.length(); ++i) {
				try {
					neededRooms.add(Room.Type.valueOf(roomsDesc.getString(i)));
				} catch (JSONException e) {
					EventCollector.logException(e,"bad room desc");
				}
			}
		}

		for (Room r : rooms) {
			if (r.type == Room.Type.NULL && r.connected.size() == 1) {
				if (!neededRooms.isEmpty() && r.width() > 3 && r.height() > 3) {
					r.type = neededRooms.remove(0);
				}
			} else if (Random.Int(2) == 0) {
				assignRoomConnectivity(r);
			}
		}
		assignRemainingRooms();
	}

	@Override
	protected int nTraps() {
		return mLevelDesc.optInt("nTraps", super.nTraps());
	}


	@Override
	public int nMobs() {
		return mLevelDesc.optInt("nMobs", super.nMobs());
	}

	protected boolean[] water() {
		return Patch.generate(this, getFeeling() == Feeling.WATER ? 0.60f : 0.45f, 5);
	}

	protected boolean[] grass() {
		return Patch.generate(this, getFeeling() == Feeling.GRASS ? 0.60f : 0.40f, 4);
	}
}
