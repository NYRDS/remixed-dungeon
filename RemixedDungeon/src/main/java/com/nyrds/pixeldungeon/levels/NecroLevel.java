package com.nyrds.pixeldungeon.levels;

import com.nyrds.pixeldungeon.effects.emitters.Candle;
import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.pixeldungeon.mobs.common.MobSpawner;
import com.nyrds.platform.util.StringsManager;
import com.watabou.noosa.Scene;
import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.actors.Actor;
import com.watabou.pixeldungeon.levels.Level;
import com.watabou.pixeldungeon.levels.Patch;
import com.watabou.pixeldungeon.levels.RegularLevel;
import com.watabou.pixeldungeon.levels.Room;
import com.watabou.pixeldungeon.levels.Terrain;
import com.watabou.utils.Random;

public class NecroLevel extends RegularLevel {

	public NecroLevel() {
		color1 = 0x534f3e;
		color2 = 0xb9d661;

		viewDistance = 6;
	}

	@Override
	protected String tilesTexXyz() {
		return Assets.TILES_NECRO_XYZ;
	}

	@Override
	public String tilesTex() {
		return Assets.TILES_NECRO;
	}

	@Override
	public String waterTex() {
		return Assets.WATER_NECRO;
	}

	protected boolean[] water() {
		return Patch.generate(this, getFeeling() == Feeling.WATER ? 0.65f : 0.45f, 4 );
	}

	protected boolean[] grass() {
		return Patch.generate(this, getFeeling() == Feeling.GRASS ? 0.60f : 0.40f, 3 );
	}

	@Override
	protected void assignRoomType() {
		super.assignRoomType();

		for (Room r : rooms) {
			if (r.type == Room.Type.TUNNEL) {
				r.type = Room.Type.PASSAGE;
			}
		}
	}

	@Override
	protected void createMobs() {
		int pos = randomRespawnCell();
			while(Actor.findChar(pos) != null) {
				pos = randomRespawnCell();
			}
			if(cellValid(pos)) {
				MobSpawner.spawnJarOfSouls(this, pos);
			}
		super.createMobs();
	}

	@Override
	protected void decorate() {

		for (int i=getWidth() + 1; i < getLength() - getWidth() - 1; i++) {
			if (map[i] == Terrain.EMPTY) {

				float c = 0.05f;
				if (map[i + 1] == Terrain.WALL && map[i + getWidth()] == Terrain.WALL) {
					c += 0.2f;
				}
				if (map[i - 1] == Terrain.WALL && map[i + getWidth()] == Terrain.WALL) {
					c += 0.2f;
				}
				if (map[i + 1] == Terrain.WALL && map[i - getWidth()] == Terrain.WALL) {
					c += 0.2f;
				}
				if (map[i - 1] == Terrain.WALL && map[i - getWidth()] == Terrain.WALL) {
					c += 0.2f;
				}

				if (Random.Float() < c) {
					map[i] = Terrain.EMPTY_DECO;
				}
			}
		}

		LevelTools.northWallDecorate(this, 10, 4);
	}

	@Override
	public String tileName( int tile ) {
		switch (tile) {
			case Terrain.WATER:
                return StringsManager.getVar(R.string.Prison_TileWater);
            default:
				return super.tileName( tile );
		}
	}

	@Override
	public String tileDesc(int tile) {
		switch (tile) {
			case Terrain.EMPTY_DECO:
                return StringsManager.getVar(R.string.Prison_TileDescDeco);
            case Terrain.BOOKSHELF:
                return StringsManager.getVar(R.string.Halls_TileDescBookshelf);
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
				scene.add( new Candle( i ) );
			}
		}
	}

	@Override
	public int objectsKind() {
		return 7;
	}
}