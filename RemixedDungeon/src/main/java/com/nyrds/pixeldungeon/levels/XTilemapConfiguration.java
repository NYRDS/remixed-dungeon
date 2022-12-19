package com.nyrds.pixeldungeon.levels;

import com.nyrds.util.JsonHelper;
import com.watabou.pixeldungeon.levels.Level;
import com.watabou.pixeldungeon.levels.Terrain;
import com.watabou.pixeldungeon.utils.GLog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;



/**
 * Created by mike on 26.12.2016.
 * This file is part of Remixed Pixel Dungeon.
 */

public class XTilemapConfiguration {

	private final Map<Integer,TileDesc> tilemapConfiguration = new HashMap<>();

	private static final Map<String, Integer> terrainMapping = new HashMap<>();

	private static void createTerrainMapping() {
		if(terrainMapping.isEmpty()) {
			for (Field f : Terrain.class.getDeclaredFields()) {
				if (f.isSynthetic()) {
					continue;
				}
				int value;
				try {
					value = f.getInt(null);
				} catch (IllegalAccessException | IllegalArgumentException ignored) {
					continue;
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

		var keys = terrainDesc.keys();

		while( keys.hasNext() ) {
			String key = keys.next();
			if(terrainMapping.containsKey(key)) {
				int terrain = terrainMapping.get(key);
				ret.tilemapConfiguration.put(terrain,createTileDescFromKey(terrainDesc, key));
			}
		}

		for(int secretTrap: Terrain.SECRET_TRAPS) {
			ret.tilemapConfiguration.put(secretTrap,createTileDescFromKey(terrainDesc, "SECRET_TRAP"));
		}

		TileDesc waterTileDesc = createTileDescFromKey(terrainDesc, "WATER_TILES");
		for(int waterBorder = Terrain.WATER_TILES;waterBorder<=Terrain.WATER;++waterBorder) {
			TileDesc borderPieceDesc = new TileDesc();

			borderPieceDesc.baseTiles.addAll(waterTileDesc.baseTiles);
			borderPieceDesc.decoTiles.addAll(waterTileDesc.decoTiles);

			for(int i = 0;i< waterTileDesc.baseTiles.size();++i) {
				borderPieceDesc.baseTiles.set(i, borderPieceDesc.baseTiles.get(i) + waterBorder - Terrain.WATER_TILES);
			}
			ret.tilemapConfiguration.put(waterBorder, borderPieceDesc);
		}

		for (Integer tileType:terrainMapping.values()) {
			if(!ret.tilemapConfiguration.containsKey(tileType)) {
				GLog.w("description for tile id %d is missing",tileType);
			}
		}

		return ret;
	}

	private static TileDesc createTileDescFromKey(JSONObject terrainDesc, String key) throws JSONException {
		TileDesc tileDesc = new TileDesc();

		JSONObject desc = terrainDesc.getJSONObject(key);
		JSONArray baseDesc = desc.getJSONArray("base");
		toIntArray(tileDesc.baseTiles, baseDesc);

		JSONArray decoDesc = desc.getJSONArray("deco");
		toIntArray(tileDesc.decoTiles, decoDesc);
		return  tileDesc;
	}

	private static void toIntArray(ArrayList<Integer> outArray, JSONArray intArray) throws JSONException {
		for(int i = 0;i<intArray.length();++i) {
			outArray.add(intArray.getInt(i));
		}
	}

	private static class TileDesc {
		ArrayList<Integer> baseTiles = new ArrayList<>();
		ArrayList<Integer> decoTiles = new ArrayList<>();
	}

	public int baseTile(Level level, int cell) {
		TileDesc desc = tilemapConfiguration.get(level.map[cell]);
		return desc.baseTiles.get(cell % desc.baseTiles.size());
	}

	public int decoTile(Level level, int cell) {
		TileDesc desc = tilemapConfiguration.get(level.map[cell]);
		return desc.decoTiles.get(cell % desc.decoTiles.size());
	}

	public int getDecoTileForTerrain(int cell, int terrain) {
		TileDesc desc = tilemapConfiguration.get(terrain);
		return desc.decoTiles.get(cell % desc.decoTiles.size());
	}
}
