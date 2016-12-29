package com.nyrds.pixeldungeon.levels;

import com.watabou.pixeldungeon.levels.Terrain;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by mike on 26.12.2016.
 * This file is part of Remixed Pixel Dungeon.
 */

public class TilemapConfiguration {

	Map<Terrain,ArrayList<tileVisualisation>> tilemapConfiguration = new HashMap<>();

	static TilemapConfiguration defaultConfig() {
		TilemapConfiguration ret = new TilemapConfiguration();
		ret.tilemapConfiguration.put(Terrain.)
	}

	public class tileVisualisation {
		public int groundTileIndex;
		public int decoTileIndex;
	}
}
