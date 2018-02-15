/*
 * Pixel Dungeon
 * Copyright (C) 2012-2014  Oleg Dolya
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */
package com.watabou.pixeldungeon;

import com.nyrds.android.util.ModdingMode;
import com.nyrds.android.util.TrackedRuntimeException;
import com.nyrds.pixeldungeon.levels.XTilemapConfiguration;
import com.watabou.noosa.TextureFilm;
import com.watabou.noosa.Tilemap;
import com.watabou.pixeldungeon.levels.Level;
import com.watabou.utils.Point;
import com.watabou.utils.PointF;

import org.json.JSONException;

public class DungeonTilemap extends Tilemap {

	public static final int SIZE = 16;

	private Tilemap mBaseLayer;
	private Tilemap mDecoLayer;

	private XTilemapConfiguration xTilemapConfiguration;

	private Level level;

	private int[] mBaseMap;
	private int[] mDecoMap;

	public DungeonTilemap(Level level, String tiles, int []baseMap, int []decoMap ) {
		super(tiles, new TextureFilm(tiles, SIZE, SIZE));
		this.level = level;

		map(level.map, level.getWidth());

		int mSize = level.getWidth() * level.getHeight();

		if (getTileset().size() == 16 * 16) {
			try {
				String tilemapConfig = "tilemapDesc/" + tiles.replace(".png", ".json");
				if (!ModdingMode.isResourceExist(tilemapConfig)) {
					tilemapConfig = "tilemapDesc/tiles_x_default.json";
				}
				xTilemapConfiguration = XTilemapConfiguration.readConfig(tilemapConfig);
			} catch (JSONException e) {
				throw new TrackedRuntimeException(e);
			}

			mBaseLayer = new Tilemap(tiles, new TextureFilm(tiles, SIZE, SIZE));

			if(baseMap != null) {
				mBaseMap = baseMap;
			} else {
				mBaseMap = new int[mSize];
				mBaseLayer.map(buildGroundMap(), level.getWidth());
			}

			mDecoLayer = new Tilemap(tiles, new TextureFilm(tiles, SIZE, SIZE));
			if(decoMap != null) {
				mDecoMap = decoMap;
			} else {
				mDecoMap = new int[mSize];
				mDecoLayer.map(buildDecoMap(), level.getWidth());
			}
		}
	}

	private boolean useExTiles() {
		return mBaseLayer != null && mDecoLayer != null;
	}

	private int currentDecoCell(int cell) {
		return xTilemapConfiguration.decoTile(level, cell);
	}

	private int[] buildDecoMap() {
		for (int i = 0; i < mDecoMap.length; i++) {
			mDecoMap[i] = currentDecoCell(i);
		}

		return mDecoMap;
	}

	private int currentBaseCell(int cell) {
		return xTilemapConfiguration.baseTile(level, cell);
	}

	private int[] buildGroundMap() {
		for (int i = 0; i < mBaseMap.length; i++) {
			mBaseMap[i] = currentBaseCell(i);
		}

		return mBaseMap;
	}

	public int screenToTile(int x, int y) {
		Point p = camera().screenToCamera(x, y).offset(this.point().negate()).invScale(SIZE).floor();
		return level.cell(p.x, p.y);
	}

	@Override
	public boolean overlapsPoint(float x, float y) {
		return true;
	}

	public void discover(int pos, int oldValue) {
/*
		final Image tile = tile(pos, oldValue);
		tile.point(tileToWorld(pos));

		// For bright mode
		tile.rm = tile.gm = tile.bm = rm;
		tile.ra = tile.ga = tile.ba = ra;
		getParent().add(tile);

		getParent().add(new AlphaTweener(tile, 0, 0.6f) {
			protected void onComplete() {
				tile.killAndErase();
				killAndErase();
			}
		});
*/
	}

	public static PointF tileToWorld(int pos) {
		return new PointF(pos % Dungeon.level.getWidth(), pos / Dungeon.level.getWidth()).scale(SIZE);
	}

	public static PointF tileCenterToWorld(int pos) {
		return new PointF((pos % Dungeon.level.getWidth() + 0.5f) * SIZE,
				(pos / Dungeon.level.getWidth() + 0.5f) * SIZE);
	}

	@Override
	public boolean overlapsScreenPoint(int x, int y) {
		return true;
	}

	@Override
	public void draw() {
		if (useExTiles()) {
			mBaseLayer.draw();
			mDecoLayer.draw();
		} else {
			super.draw();
		}
	}

	public void updateAll() {
		if (useExTiles()) {
			buildGroundMap();
			buildDecoMap();
			mBaseLayer.updateRegion().set(0, 0, level.getWidth(), level.getHeight());
			mDecoLayer.updateRegion().set(0, 0, level.getWidth(), level.getHeight());
		} else {
			updated.set(0, 0, level.getWidth(), level.getHeight());
		}
	}

	public void updateCell(int cell, Level level) {
		int x = level.cellX(cell);
		int y = level.cellY(cell);

		if (useExTiles()) {
			mBaseMap[cell] = currentBaseCell(cell);
			mDecoMap[cell] = currentDecoCell(cell);
			mBaseLayer.updateRegion().union(x, y);
			mDecoLayer.updateRegion().union(x, y);
		} else {
			updated.union(x, y);
		}
	}
}
