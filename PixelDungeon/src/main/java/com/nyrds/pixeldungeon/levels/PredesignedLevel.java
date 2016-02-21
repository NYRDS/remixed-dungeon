package com.nyrds.pixeldungeon.levels;

import com.nyrds.android.util.JsonHelper;
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
			throw new RuntimeException(String.format("Malformed level [%s] description", fileName));
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
		} catch (JSONException e) {
			throw new RuntimeException(e);
		}
		buildFlagMaps();
		cleanWalls();
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
	}

	@Override
	protected void createItems() {
	}

	@Override
	protected int nTraps() {
		return 0;
	}

}
