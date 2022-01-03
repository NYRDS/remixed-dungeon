package com.nyrds.pixeldungeon.levels;

import com.nyrds.pixeldungeon.effects.emitters.BloodSink;
import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.platform.util.StringsManager;
import com.watabou.noosa.Scene;
import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.levels.Level;
import com.watabou.pixeldungeon.levels.Patch;
import com.watabou.pixeldungeon.levels.RegularLevel;
import com.watabou.pixeldungeon.levels.Terrain;
import com.watabou.utils.Random;

public class GutsLevel extends RegularLevel {

	public GutsLevel() {
		color1 = 0x534f3e;
		color2 = 0xb9d661;

		viewDistance = 6;
	}

	@Override
	protected String tilesTexXyz() {
		return Assets.TILES_GUTS_XYZ;
	}

	@Override
	public String tilesTexEx() {
		return Assets.TILES_GUTS;
	}

	@Override
	public String waterTex() {
		return Assets.WATER_GUTS;
	}

	protected boolean[] water() {
		return Patch.generate(this, getFeeling() == Feeling.WATER ? 0.60f : 0.45f, 6 );
	}

	protected boolean[] grass() {
		return Patch.generate(this, getFeeling() == Feeling.GRASS ? 0.55f : 0.35f, 3 );
	}

	@Override
	protected void decorate() {

		for (int i=0; i < getWidth(); i++) {
			if (map[i] == Terrain.WALL &&
					map[i + getWidth()] == Terrain.WATER &&
					Random.Int( 4 ) == 0) {

				map[i] = Terrain.WALL_DECO;
			}
		}

		for (int i=getWidth(); i < getLength() - getWidth(); i++) {
			if (map[i] == Terrain.WALL &&
					map[i - getWidth()] == Terrain.WALL &&
					map[i + getWidth()] == Terrain.WATER &&
					Random.Int( 2 ) == 0) {

				map[i] = Terrain.WALL_DECO;
			}
		}

		for (int i=getWidth() + 1; i < getLength() - getWidth() - 1; i++) {
			if (map[i] == Terrain.EMPTY) {

				int count =
						(map[i + 1] == Terrain.WALL ? 1 : 0) +
								(map[i - 1] == Terrain.WALL ? 1 : 0) +
								(map[i + getWidth()] == Terrain.WALL ? 1 : 0) +
								(map[i - getWidth()] == Terrain.WALL ? 1 : 0);

				if (Random.Int( 16 ) < count * count) {
					map[i] = Terrain.EMPTY_DECO;
				}
			}
		}
		placeEntranceSign();
	}

	@Override
	public int nMobs() {
		return 12 + Dungeon.depth % 5 + Random.Int( 4 );
	}


	@Override
	public String tileName( int tile ) {
		switch (tile) {
		case Terrain.GRASS:
            return StringsManager.getVar(R.string.Guts_TileGrass);
            case Terrain.HIGH_GRASS:
                return StringsManager.getVar(R.string.Guts_TileHighGrass);
            case Terrain.WATER:
                return StringsManager.getVar(R.string.Guts_TileWater);
            default:
			return super.tileName( tile );
		}
	}
	
	@Override
	public String tileDesc( int tile ) {
		switch (tile) {
		case Terrain.ENTRANCE:
            return StringsManager.getVar(R.string.Guts_TileDescEntrance);
            case Terrain.EXIT:
                return StringsManager.getVar(R.string.Guts_TileDescExit);
            case Terrain.HIGH_GRASS:
                return StringsManager.getVar(R.string.Guts_TileHighGrass);
            case Terrain.WALL_DECO:
                return StringsManager.getVar(R.string.Guts_TileDescDeco);
            case Terrain.BOOKSHELF:
                return StringsManager.getVar(R.string.Guts_TileDescBookshelf);
            default:
			return super.tileDesc( tile );
		}
	}

	@Override
	public void addVisuals( Scene scene ) {
		super.addVisuals( scene );
		addVisuals( this, scene );
	}

	public static void addVisuals( Level level, Scene scene ) {
		for (int i=0; i < level.getLength(); i++) {
			if (level.map[i] == Terrain.WALL_DECO) {
				scene.add( new BloodSink( i ) );
			}
		}
	}

	@Override
	public int objectsKind() {
		return 5;
	}
}