package com.nyrds.pixeldungeon.spiders.levels;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.nyrds.android.util.JsonHelper;
import com.watabou.pixeldungeon.levels.CommonLevel;

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
