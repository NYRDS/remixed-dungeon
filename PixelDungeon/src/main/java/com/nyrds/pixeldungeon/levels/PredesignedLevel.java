package com.nyrds.pixeldungeon.levels;

import com.nyrds.android.util.JsonHelper;
import com.nyrds.android.util.TrackedRuntimeException;
import com.nyrds.pixeldungeon.items.common.ItemFactory;
import com.nyrds.pixeldungeon.mobs.common.MobFactory;
import com.watabou.pixeldungeon.actors.mobs.Mob;
import com.watabou.pixeldungeon.items.Item;
import com.watabou.pixeldungeon.levels.CommonLevel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class PredesignedLevel extends CommonLevel {

	JSONObject mLevelDesc;

	public PredesignedLevel(String fileName) {
		color1 = 0x48763c;
		color2 = 0x59994a;

		mLevelDesc = JsonHelper.readFile(fileName);

		if (mLevelDesc == null) {
			throw new TrackedRuntimeException(String.format("Malformed level [%s] description", fileName));
		}

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
	public void create(int w, int h) {
		try {
			width = mLevelDesc.getInt("width");
			height = mLevelDesc.getInt("height");

			initSizeDependentStuff();

			JSONArray map = mLevelDesc.getJSONArray("map");

			for (int i = 0; i < map.length(); i++) {
				set(i, map.getInt(i));
			}

			JSONArray entranceDesc = mLevelDesc.getJSONArray("entrance");

			entrance = cell(entranceDesc.getInt(0), entranceDesc.getInt(1));

			JSONArray exitDesc = mLevelDesc.getJSONArray("exit");
			exit = cell(exitDesc.getInt(0), exitDesc.getInt(1));

		} catch (JSONException e) {
			throw new TrackedRuntimeException(e);
		}
		buildFlagMaps();
		cleanWalls();
		createMobs();
		createItems();
	}

	@Override
	protected boolean build() {
		return true;
	}

	@Override
	protected void decorate() {
	}

	@Override
	protected void createMobs() {
		try {
			if (mLevelDesc.has("mobs")) {
				JSONArray mobsDesc = mLevelDesc.getJSONArray("mobs");

				for(int i=0;i<mobsDesc.length();++i) {
					JSONObject mobDesc = mobsDesc.optJSONObject(i);
					int x = mobDesc.getInt("x");
					int y = mobDesc.getInt("y");

					if(cellValid(x,y)) {
						String kind = mobDesc.getString("kind");
						Mob mob = MobFactory.mobClassByName(kind).newInstance();
						mob.setPos(cell(x,y));
						spawnMob(mob);
					}
				}
			}
		} catch (JSONException e) {
			throw new TrackedRuntimeException("bad mob description", e);
		} catch (InstantiationException e) {
			throw new TrackedRuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new TrackedRuntimeException(e);
		}
	}

	@Override
	protected void createItems() {
		try {
			if (mLevelDesc.has("items")) {
				JSONArray itemsDesc = mLevelDesc.getJSONArray("items");

				for(int i=0;i<itemsDesc.length();++i) {
					JSONObject itemDesc = itemsDesc.optJSONObject(i);
					int x = itemDesc.getInt("x");
					int y = itemDesc.getInt("y");

					if(cellValid(x,y)) {
						String kind = itemDesc.getString("kind");
						Item item = ItemFactory.itemsClassByName(kind).newInstance();
						if(itemDesc.has("quantity")) {
							item.quantity(itemDesc.getInt("quantity"));
						}
						drop(item, cell(x, y));
					}
				}
			}
		} catch (JSONException e) {
			throw new TrackedRuntimeException("bad items description", e);
		} catch (InstantiationException e) {
			throw new TrackedRuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new TrackedRuntimeException(e);
		}
	}

	@Override
	protected int nTraps() {
		return 0;
	}

}
