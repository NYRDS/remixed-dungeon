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

import android.graphics.RectF;

import com.nyrds.android.util.TrackedRuntimeException;
import com.nyrds.pixeldungeon.levels.XTilemapConfiguration;
import com.watabou.noosa.CompositeImage;
import com.watabou.noosa.Image;
import com.watabou.noosa.TextureFilm;
import com.watabou.noosa.Tilemap;
import com.watabou.noosa.tweeners.AlphaTweener;
import com.watabou.utils.Point;
import com.watabou.utils.PointF;

import org.json.JSONException;

public class DungeonTilemap extends Tilemap {

	public static final int SIZE = 16;

	private static DungeonTilemap instance;

	private Tilemap mGroundLayer;
	private Tilemap mDecoLayer;

	private static XTilemapConfiguration xTilemapConfiguration;

	private int[] mGroundMap;
	private int[] mDecoMap;

	public DungeonTilemap(String tiles) {
		super(tiles, new TextureFilm(tiles, SIZE, SIZE));
		instance = this;
		int levelWidth = Dungeon.level.getWidth();
		map(Dungeon.level.map, levelWidth);

		int mSize = Dungeon.level.getWidth() * Dungeon.level.getHeight();

		if (getTileset().size() == 16 * 16) {
			try {
				xTilemapConfiguration = XTilemapConfiguration.readConfig("tilemapDesc/tiles_x_default.json");
			} catch (JSONException e) {
				throw new TrackedRuntimeException(e);
			}

			mGroundLayer = new Tilemap(tiles, new TextureFilm(tiles, SIZE, SIZE));
			mGroundMap = new int[mSize];
			mGroundLayer.map(buildGroundMap(), levelWidth);

			mDecoLayer = new Tilemap(tiles, new TextureFilm(tiles, SIZE, SIZE));
			mDecoMap = new int[mSize];
			mDecoLayer.map(buildDecoMap(), levelWidth);
		}
	}

	private static boolean useExTiles() {
		return instance.mGroundLayer != null && instance.mDecoLayer != null;
	}

	private static int currentDecoCell(int cell) {
		if(!Dungeon.level.customTiles()) {
			return xTilemapConfiguration.decoTile(Dungeon.level.map[cell], Dungeon.level.decoTileVariant[cell]);
		} else {
			return Dungeon.level.decoTileVariant[cell];
		}
	}

	private int[] buildDecoMap() {
		for (int i = 0; i < mDecoMap.length; i++) {
			mDecoMap[i] = currentDecoCell(i);
		}

		return mDecoMap;
	}

	private static int currentGroundCell(int cell) {
		if(!Dungeon.level.customTiles()){
			return xTilemapConfiguration.baseTile(Dungeon.level.map[cell], Dungeon.level.baseTileVariant[cell]);
		} else {
			return Dungeon.level.baseTileVariant[cell];
		}
	}

	private int[] buildGroundMap() {
		for (int i = 0; i < mGroundMap.length; i++) {
			mGroundMap[i] = currentGroundCell(i);
		}

		return mGroundMap;
	}

	public int screenToTile(int x, int y) {
		Point p = camera().screenToCamera(x, y).offset(this.point().negate()).invScale(SIZE).floor();
		return Dungeon.level.cell(p.x, p.y);
	}

	@Override
	public boolean overlapsPoint(float x, float y) {
		return true;
	}

	public void discover(int pos, int oldValue) {

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
	}

	public static PointF tileToWorld(int pos) {
		return new PointF(pos % Dungeon.level.getWidth(), pos / Dungeon.level.getWidth()).scale(SIZE);
	}

	public static PointF tileCenterToWorld(int pos) {
		return new PointF((pos % Dungeon.level.getWidth() + 0.5f) * SIZE,
				(pos / Dungeon.level.getWidth() + 0.5f) * SIZE);
	}

	public static CompositeImage tile(int cell) {
		return tile(cell, -1);
	}

	private static CompositeImage createTileImage(int groundCell, int decoCell) {
		CompositeImage img = new CompositeImage(instance.mGroundLayer.getTexture());

		RectF frame = instance.mGroundLayer.getTileset().get(groundCell);
		img.frame(frame);

		frame = instance.mDecoLayer.getTileset().get(decoCell);
		Image deco = new Image(instance.mDecoLayer.getTexture());
		deco.frame(frame);
		img.addLayer(deco);
		return img;

	}

	public static CompositeImage tile(int cell, int tileType) {

		if (tileType == -1) {
			if (useExTiles()) {
				return createTileImage(currentGroundCell(cell), currentDecoCell(cell));
			}

			CompositeImage img = new CompositeImage(instance.getTexture());
			img.frame(instance.getTileset().get(Dungeon.level.map[cell]));
			return img;
		} else {
			if (useExTiles()) {
				if(!Dungeon.level.customTiles()) {
					return createTileImage(
							xTilemapConfiguration.baseTile(tileType, Dungeon.level.baseTileVariant[cell]),
							xTilemapConfiguration.decoTile(tileType, Dungeon.level.decoTileVariant[cell]));
				} else {
					return createTileImage(Dungeon.level.baseTileVariant[cell], Dungeon.level.decoTileVariant[cell]);
				}
			}

			CompositeImage img = new CompositeImage(instance.getTexture());
			img.frame(instance.getTileset().get(tileType));
			return img;
		}
	}

	@Override
	public boolean overlapsScreenPoint(int x, int y) {
		return true;
	}

	@Override
	public void draw() {
		if (useExTiles()) {
			mGroundLayer.draw();
			mDecoLayer.draw();
		} else {
			super.draw();
		}
	}

	public void updateAll() {
		if (useExTiles()) {
			buildGroundMap();
			buildDecoMap();
			mGroundLayer.updateRegion().set(0, 0, Dungeon.level.getWidth(), Dungeon.level.getHeight());
			mDecoLayer.updateRegion().set(0, 0, Dungeon.level.getWidth(), Dungeon.level.getHeight());
		} else {
			updated.set(0, 0, Dungeon.level.getWidth(), Dungeon.level.getHeight());
		}

	}

	public void updateCell(int cell) {
		if (useExTiles()) {
			mGroundMap[cell] = currentGroundCell(cell);
			mDecoMap[cell] = currentDecoCell(cell);
			mGroundLayer.updateRegion().union(cell % Dungeon.level.getWidth(), cell / Dungeon.level.getWidth());
			mDecoLayer.updateRegion().union(cell % Dungeon.level.getWidth(), cell / Dungeon.level.getWidth());
		} else {
			updated.union(cell % Dungeon.level.getWidth(), cell / Dungeon.level.getWidth());
		}
	}
}
