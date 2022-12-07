package com.nyrds.retrodungeon.levels;

import com.nyrds.android.util.JsonHelper;
import com.watabou.pixeldungeon.levels.Terrain;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by mike on 26.12.2016.
 * This file is part of Remixed Pixel Dungeon.
 */

public class CustomTilemapConfiguration {

	Map<Terrain,ArrayList<tileProperties>> tilemapConfiguration = new HashMap<>();

	static CustomTilemapConfiguration readConfig(String filename) {

		JSONObject config = JsonHelper.readJsonFromAsset(filename);

		CustomTilemapConfiguration ret = new CustomTilemapConfiguration();
		//ret.tilemapConfiguration.put(Terrain.)
		return ret;
	}

	public class tileProperties {
		public int terrainType;
		public int moreProps;
	}
}
