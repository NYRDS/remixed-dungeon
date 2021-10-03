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

import com.watabou.noosa.Image;
import com.watabou.noosa.TextureFilm;
import com.watabou.noosa.Tilemap;
import com.watabou.noosa.tweeners.AlphaTweener;
import com.watabou.pixeldungeon.levels.Level;
import com.watabou.utils.Point;
import com.watabou.utils.PointF;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class DungeonTilemap extends Tilemap {

	public static final int SIZE = 16;
	
	static protected Level level;


	public DungeonTilemap(@NotNull Level level, String tiles ) {
		super(tiles, new TextureFilm(tiles, SIZE, SIZE));
		DungeonTilemap.level = level;

		map(level.map, level.getWidth());
	}

	@Contract("_, _ -> new")
	static public @NotNull DungeonTilemap factory(Level level, String tiles) {
		TextureFilm probe = new TextureFilm(tiles, SIZE, SIZE);

		if(tiles.contains("_xyz")) {
			return new XyzDungeonTilemap(level, tiles);
		}

		if(probe.size() == 256) {
			return new VariativeDungeonTilemap(level, tiles);
		}

		return new ClassicDungeonTilemap(level, tiles);
	}

	public int screenToTile(int x, int y) {
		Point p = camera().screenToCamera(x, y).offset(this.point().negate()).invScale(SIZE).floor();
		return level.cell(p.x, p.y);
	}

	@Override
	public boolean overlapsPoint(float x, float y) {
		return true;
	}

	public void discover(int pos) {

		final Image tile = tile(pos);

		if(tile==null) {
			return;
		}

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

	@Nullable
	public abstract Image tile(int pos);

	public static PointF tileToWorld(int pos) {
		return new PointF(level.cellX(pos), level.cellY(pos)).scale(SIZE);
	}

	@Contract("_ -> new")
	public static @NotNull PointF tileCenterToWorld(int pos) {
		return new PointF((level.cellX(pos) + 0.5f) * SIZE,
				(level.cellY(pos) + 0.5f) * SIZE + Image.isometricModeShift );
	}

	@Override
	public boolean overlapsScreenPoint(int x, int y) {
		return true;
	}

	public abstract void updateCell(int cell, Level level);

	public abstract void updateAll();
}
