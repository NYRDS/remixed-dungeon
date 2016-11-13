package com.nyrds.pixeldungeon.levels;

import com.nyrds.android.util.TrackedRuntimeException;
import com.nyrds.pixeldungeon.items.common.ItemFactory;
import com.nyrds.pixeldungeon.levels.objects.LevelObjectsFactory;
import com.watabou.pixeldungeon.items.Item;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class RandomLevel extends CustomLevel {

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
	public void create(int w, int h) {
		try {
			width = mLevelDesc.getInt("width");
			height = mLevelDesc.getInt("height");

			initSizeDependentStuff();

			if (mLevelDesc.has("items")) {
				JSONArray itemsDesc = mLevelDesc.getJSONArray("items");

				for (int i = 0; i < itemsDesc.length(); ++i) {
					JSONObject itemDesc = itemsDesc.optJSONObject(i);
					Item item = ItemFactory.createItemFromDesc(itemDesc);
					addItemToSpawn(item);
				}
			}

			placeObjects();

			setupLinks();

		} catch (JSONException e) {
			throw new TrackedRuntimeException(e);
		} catch (InstantiationException e) {
			throw new TrackedRuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new TrackedRuntimeException(e);
		}

		buildFlagMaps();
		cleanWalls();
		createMobs();
		createItems();
	}

	private void placeObjects() throws JSONException {
		if(mLevelDesc.has("objects")) {
			JSONArray objects = mLevelDesc.getJSONArray("objects");

			for (int i = 0; i < objects.length(); i++) {
				JSONObject object = objects.getJSONObject(i);
				addLevelObject(LevelObjectsFactory.createObject(this, object));
			}
		}
	}

	private void setupLinks() throws JSONException {
		JSONArray entranceDesc = mLevelDesc.getJSONArray("entrance");

		entrance = cell(entranceDesc.getInt(0), entranceDesc.getInt(1));

		if(mLevelDesc.has("exit")) {
			JSONArray exitDesc = mLevelDesc.getJSONArray("exit");
			setExit(cell(exitDesc.getInt(0), exitDesc.getInt(1)), 0);
			return;
		}

		if(mLevelDesc.has("multiexit")){
			JSONArray multiExitDesc = mLevelDesc.getJSONArray("multiexit");
			for(int i=0;i<multiExitDesc.length();++i) {
				JSONArray exitDesc = multiExitDesc.getJSONArray(i);
				setExit(cell(exitDesc.getInt(0), exitDesc.getInt(1)), i);
			}
		}
	}

	@Override
	protected boolean build() {
		return true;
	}

	@Override
	protected void decorate() {
	}

	@Override
	protected void createItems() {

	}

	@Override
	protected int nTraps() {
		return mLevelDesc.optInt("nTraps",0);
	}
}
