
package com.watabou.pixeldungeon;

import com.nyrds.LuaInterface;
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
	public static final String XYZ = "xyz";

	static protected Level level;


	public DungeonTilemap(@NotNull Level level, String tiles ) {
		super(tiles, new TextureFilm(tiles, SIZE, SIZE));
		DungeonTilemap.level = level;

		map(level.map, level.getWidth());
	}


	static public @NotNull DungeonTilemap factory(Level level) {
		String tiles = level.getTilesTex();

		TextureFilm probe = new TextureFilm(tiles, SIZE, SIZE);


		if (tiles.contains("_xyz")) {
			return new XyzDungeonTilemap(level, tiles);
		}


		if(probe.size() == 256) {
			return new VariativeDungeonTilemap(level, tiles);
		}

		return new ClassicDungeonTilemap(level, tiles);
	}


	@LuaInterface
	static public int getDecoTileForTerrain(Level level, int cell, int terrain) {
		String tiles = level.getTilesTex();

		TextureFilm probe = new TextureFilm(tiles, SIZE, SIZE);

		if(probe.size() == 256) {
			return VariativeDungeonTilemap.getDecoTileForTerrain(level, cell, terrain);
		}

		return terrain;
	}

	@LuaInterface
	static public @NotNull String kind(Level level) {
		String tiles = level.getTilesTex();

		TextureFilm probe = new TextureFilm(tiles, SIZE, SIZE);

		if (tiles.contains("_xyz")) {
			return XYZ;
		}

		if(probe.size() == 256) {
			return "x";
		}

		return "classic";
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
				(level.cellY(pos) + 0.5f) * SIZE);
	}

	@Override
	public boolean overlapsScreenPoint(int x, int y) {
		return true;
	}

	public abstract void updateCell(int cell, Level level);

	public abstract void updateAll();

	public void updateFow(@NotNull FogOfWar fog) {
		fog.updateVisibility(Dungeon.visible, level.visited, level.mapped, false);
	}
}
