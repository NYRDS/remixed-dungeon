package com.nyrds.pixeldungeon.levels;

import com.nyrds.pixeldungeon.levels.objects.LevelObjectsFactory;
import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.platform.util.StringsManager;
import com.watabou.noosa.Scene;
import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.levels.BossLevel;
import com.watabou.pixeldungeon.levels.CityLevel;
import com.watabou.pixeldungeon.levels.Terrain;
import com.watabou.pixeldungeon.levels.painters.Painter;
import com.watabou.pixeldungeon.utils.Utils;
import com.watabou.utils.Random;

public class NecroBossLevel extends BossLevel {
	
	{
		color1 = 0x4b6636;
		color2 = 0xf2f2f2;
	}
	
	private static final int TOP			= 2;
	private static final int HALL_WIDTH		= 9;
	private static final int HALL_HEIGHT	= 9;
	private static final int CHAMBER_HEIGHT	= 4;

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

	@Override
	protected boolean build() {

		Painter.fill( this, _Left(), TOP, HALL_WIDTH, HALL_HEIGHT, Terrain.WATER );
		
		int y = TOP + 1;
		while (y < TOP + HALL_HEIGHT) {
			putLevelObject(LevelObjectsFactory.createCustomObject(this, LevelObjectsFactory.STATUE, y * getWidth() + _Center() - 3));
			putLevelObject(LevelObjectsFactory.createCustomObject(this, LevelObjectsFactory.STATUE, y * getWidth() + _Center() + 3));
			y += 2;
		}

		int pedestal_1 = (TOP + HALL_HEIGHT / 4) * getWidth() + _Center() - 2;
		int pedestal_2 = (TOP + HALL_HEIGHT / 2 + HALL_HEIGHT / 4) * getWidth() + _Center() - 2;
		int pedestal_3 = (TOP + HALL_HEIGHT / 4) * getWidth() + _Center() + 2;
		int pedestal_4 = (TOP + HALL_HEIGHT / 2 + HALL_HEIGHT / 4) * getWidth() + _Center() + 2;

		putLevelObject(LevelObjectsFactory.createCustomObject(this, LevelObjectsFactory.PEDESTAL, pedestal_1));
		putLevelObject(LevelObjectsFactory.createCustomObject(this, LevelObjectsFactory.PEDESTAL, pedestal_2));
		putLevelObject(LevelObjectsFactory.createCustomObject(this, LevelObjectsFactory.PEDESTAL, pedestal_3));
		putLevelObject(LevelObjectsFactory.createCustomObject(this, LevelObjectsFactory.PEDESTAL, pedestal_4));

		//map[pedestal_1] = map[pedestal_2] = map[pedestal_3] = map[pedestal_4] = Terrain.PEDESTAL;
		
		setExit((TOP - 1) * getWidth() + _Center(),0);
		
		arenaDoor = (TOP + HALL_HEIGHT) * getWidth() + _Center();
		map[arenaDoor] = Terrain.DOOR;
		
		Painter.fill( this, _Left(), TOP + HALL_HEIGHT + 1, HALL_WIDTH, CHAMBER_HEIGHT, Terrain.WATER );
		Painter.fill( this, _Left(), TOP + HALL_HEIGHT + 1, 1, CHAMBER_HEIGHT, Terrain.BOOKSHELF );
		Painter.fill( this, _Left() + HALL_WIDTH - 1, TOP + HALL_HEIGHT + 1, 1, CHAMBER_HEIGHT, Terrain.BOOKSHELF );
		
		entrance = (TOP + HALL_HEIGHT + 2 + Random.Int( CHAMBER_HEIGHT - 1 )) * getWidth() + _Left() + (/*1 +*/ Random.Int( HALL_WIDTH-2 )); 
		map[entrance] = Terrain.ENTRANCE;
		map[getExit(0)] = Terrain.LOCKED_EXIT;
		return true;
	}
	
	@Override
	protected void decorate() {

		for (int i=0; i < getLength(); i++) {
			if (map[i] == Terrain.WALL && Random.Int( 8 ) == 0) {
				map[i] = Terrain.WALL_DECO;
			}
		}
	}

	@Override
	protected void pressHero(int cell, Hero hero) {

		super.pressHero( cell, hero );

		if (!enteredArena && outsideEntranceRoom( cell ) ) {
			
			enteredArena = true;

			spawnBoss((TOP + HALL_HEIGHT / 2) * getWidth() + _Center());
		}
	}

	private boolean outsideEntranceRoom(int cell ) {
		return cell / getWidth() < arenaDoor / getWidth();
	}
	
	@Override
	public String tileName( int tile ) {
		switch (tile) {
		case Terrain.WATER:
            return StringsManager.getVar(R.string.Prison_TileWater);
            case Terrain.HIGH_GRASS:
                return StringsManager.getVar(R.string.City_TileHighGrass);
            case Terrain.UNLOCKED_EXIT:
		case Terrain.LOCKED_EXIT:
            return StringsManager.getVar(R.string.PortalGate_Name);
            default:
			return super.tileName( tile );
		}
	}
	
	@Override
	public String tileDesc(int tile) {
		switch (tile) {
		case Terrain.BOOKSHELF:
            return StringsManager.getVar(R.string.Halls_TileDescBookshelf);
            case Terrain.UNLOCKED_EXIT:
                return Utils.format(StringsManager.getVar(R.string.PortalExit_Desc), StringsManager.getVar(R.string.PortalExit_Desc_Necropolis));
		default:
			return super.tileDesc( tile );
		}
	}
	
	@Override
	public void addVisuals( Scene scene ) {
		CityLevel.addVisuals( this, scene );
	}

	private int _Left() {
		return (getWidth() - HALL_WIDTH) / 2;
	}

	private int _Center() {
		return _Left() + HALL_WIDTH / 2;
	}
}
