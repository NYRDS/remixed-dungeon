
package com.watabou.pixeldungeon.levels;

import com.nyrds.pixeldungeon.effects.emitters.Smoke;
import com.nyrds.pixeldungeon.levels.LevelTools;
import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.platform.util.StringsManager;
import com.watabou.noosa.Scene;
import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.actors.mobs.npcs.Imp;
import com.watabou.pixeldungeon.levels.Room.Type;
import com.watabou.utils.Random;

public class CityLevel extends RegularLevel {

	{
		color1 = 0x4b6636;
		color2 = 0xf2f2f2;
		_objectsKind = 3;
	}

	@Override
	protected String tilesTexXyz() {
		return Assets.TILES_CITY_XYZ;
	}

	@Override
	public String tilesTex() {
		return Assets.TILES_CITY;
	}
	
	@Override
	public String waterTex() {
		return Assets.WATER_CITY;
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
			if (r.type == Type.TUNNEL) {
				r.type = Type.PASSAGE;
			}
		}
	}
	
	@Override
	protected void decorate() {

		LevelTools.northWallDecorate(this, 8, 6);

		placeEntranceSign();
		placeBarrels(Random.Int(10));
	}
	
	@Override
	protected void createItems() {
		super.createItems();
		
		Imp.Quest.spawn( this, roomEntrance );
	}
	
	@Override
	public String tileName( int tile ) {
		switch (tile) {
		case Terrain.WATER:
            return StringsManager.getVar(R.string.City_TileWater);
            case Terrain.HIGH_GRASS:
                return StringsManager.getVar(R.string.City_TileHighGrass);
            default:
			return super.tileName( tile );
		}
	}
	
	@Override
	public String tileDesc(int tile) {
		switch (tile) {
		case Terrain.ENTRANCE:
            return StringsManager.getVar(R.string.City_TileDescEntrance);
            case Terrain.EXIT:
                return StringsManager.getVar(R.string.City_TileDescExit);
            case Terrain.WALL_DECO:
		case Terrain.EMPTY_DECO:
            return StringsManager.getVar(R.string.City_TileDescDeco);
            case Terrain.EMPTY_SP:
                return StringsManager.getVar(R.string.City_TileDescEmptySP);
            case Terrain.STATUE:
		case Terrain.STATUE_SP:
            return StringsManager.getVar(R.string.City_TileDescStatue);
            case Terrain.BOOKSHELF:
                return StringsManager.getVar(R.string.City_TileDescBookshelf);
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
				scene.add( new Smoke( i ) );
			}
		}
	}
}