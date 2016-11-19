package com.nyrds.pixeldungeon.levels;

import com.nyrds.android.util.TrackedRuntimeException;
import com.nyrds.pixeldungeon.items.common.ItemFactory;
import com.nyrds.pixeldungeon.utils.DungeonGenerator;
import com.watabou.pixeldungeon.items.Item;
import com.watabou.pixeldungeon.levels.Patch;
import com.watabou.pixeldungeon.levels.RegularLevel;
import com.watabou.pixeldungeon.levels.Room;
import com.watabou.pixeldungeon.levels.Terrain;
import com.watabou.utils.Random;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RandomLevel extends RegularLevel {

	//for restoreFromBundle
	public RandomLevel() {
		super();
	}

	public RandomLevel(String fileName) {
		mDescFile = fileName;
		readDescFile(mDescFile);
	}

	@Override
	public String tilesTex() {
		return mLevelDesc.optString("tiles", "tiles0.png");
	}

	public String tilesTexEx() {
		return mLevelDesc.optString("tiles_x", null);
	}

	public String waterTex() {
		return mLevelDesc.optString("water", "water0.png");
	}

	@Override
	protected void decorate() {

	}

	@Override
	public void create(int w, int h) {
		try {
			width = mLevelDesc.getInt("width");
			height = mLevelDesc.getInt("height");

			initSizeDependentStuff();

			feeling = DungeonGenerator.getCurrentLevelFeeling(levelId);

			if (mLevelDesc.has("items")) {
				JSONArray itemsDesc = mLevelDesc.getJSONArray("items");

				for (int i = 0; i < itemsDesc.length(); ++i) {
					JSONObject itemDesc = itemsDesc.optJSONObject(i);
					Item item = ItemFactory.createItemFromDesc(itemDesc);
					addItemToSpawn(item);
				}
			}

		} catch (JSONException e) {
			throw new TrackedRuntimeException(e);
		} catch (InstantiationException e) {
			throw new TrackedRuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new TrackedRuntimeException(e);
		}

		do {
			Arrays.fill(map, feeling == Feeling.CHASM ? Terrain.CHASM
					: Terrain.WALL);
		} while (!build());

		buildFlagMaps();
		cleanWalls();
		createMobs();
		createItems();
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
					e.printStackTrace();
				}
			}
		}

		for (Room r : rooms) {
			if (r.type == Room.Type.NULL && r.connected.size() == 1) {

				if (neededRooms.size() > 0 && r.width() > 3 && r.height() > 3) {
					{

						int n = neededRooms.size();
						r.type = neededRooms.get(Math.min(Random.Int(n), Random.Int(n)));
						if (r.type == Room.Type.WEAK_FLOOR) {
							weakFloorCreated = true;
						}
					}

					Room.useType(r.type);
					neededRooms.remove(r.type);


				} else if (Random.Int(2) == 0) {
					assignRoomConnectivity(r);
				}
			}
		}

		assignRemainingRooms();
	}

	@Override
	protected int nTraps() {
		return mLevelDesc.optInt("nTraps", 0);
	}

	protected boolean[] water() {
		return Patch.generate(this, getFeeling() == Feeling.WATER ? 0.60f : 0.45f, 5);
	}

	protected boolean[] grass() {
		return Patch.generate(this, getFeeling() == Feeling.GRASS ? 0.60f : 0.40f, 4);
	}
}
