package com.nyrds.pixeldungeon.levels;

import com.nyrds.android.util.JsonHelper;
import com.nyrds.android.util.TrackedRuntimeException;
import com.watabou.pixeldungeon.levels.Terrain;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by mike on 26.12.2016.
 * This file is part of Remixed Pixel Dungeon.
 */

public class XTilemapConfiguration {

	private Map<Integer,TileDesc> tilemapConfiguration = new HashMap<>();

	private static Map<String, Integer> terrainMapping = new HashMap<>();

	private static void createTerrainMapping() {
		if(terrainMapping.isEmpty()) {
			for (Field f : Terrain.class.getDeclaredFields()) {
				if (f.isSynthetic()) {
					continue;
				}
				int value;
				try {
					value = f.getInt(null);
				} catch (IllegalAccessException | IllegalArgumentException e) {
					throw new TrackedRuntimeException(e);
				}
				String name = f.getName();

				terrainMapping.put(name, value);
			}
		}
	}

	public static XTilemapConfiguration readConfig(String filename) throws JSONException {
		createTerrainMapping();

		JSONObject terrainDesc = JsonHelper.readJsonFromAsset(filename);
		XTilemapConfiguration ret = new XTilemapConfiguration();

		Iterator<?> keys = terrainDesc.keys();

		while( keys.hasNext() ) {
			String key = (String)keys.next();
			if(terrainMapping.containsKey(key)) {
				int terrain = terrainMapping.get(key);
				TileDesc tileDesc = new TileDesc();

				JSONObject desc = terrainDesc.getJSONObject(key);
				JSONArray baseDesc = desc.getJSONArray("base");

				tileDesc.baseTiles = new ArrayList<>();
				toIntArray(tileDesc.baseTiles, baseDesc);

				JSONArray decoDesc = desc.getJSONArray("deco");

				tileDesc.decoTiles = new ArrayList<>();
				toIntArray(tileDesc.decoTiles, decoDesc);

				ret.tilemapConfiguration.put(terrain,tileDesc);
			}
		}


		return ret;
	}

	private static void toIntArray(ArrayList outArray, JSONArray intArray) throws JSONException {
		for(int i = 0;i<intArray.length();++i) {
			outArray.add(intArray.getInt(i));
		}
	}

	private static class TileDesc {
		ArrayList<Integer> baseTiles;
		ArrayList<Integer> decoTiles;
	}

	public int baseTile(int terrain, int variant) {
		TileDesc desc = tilemapConfiguration.get(terrain);
		if(desc!=null) {
			return desc.baseTiles.get(variant % desc.baseTiles.size());
		}
		return 15;
	}

	public int decoTile(int terrain, int variant) {
		TileDesc desc = tilemapConfiguration.get(terrain);
		if(desc != null) {
			return desc.decoTiles.get(variant % desc.decoTiles.size());
		}
		return 15;
	}
}
