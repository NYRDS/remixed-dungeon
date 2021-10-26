package com.nyrds.pixeldungeon.levels;

import com.nyrds.pixeldungeon.items.guts.PseudoAmulet;
import com.nyrds.pixeldungeon.levels.objects.LevelObjectsFactory;
import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.platform.util.StringsManager;
import com.watabou.noosa.Scene;
import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.levels.HallsLevel;
import com.watabou.pixeldungeon.levels.Level;
import com.watabou.pixeldungeon.levels.Terrain;
import com.watabou.pixeldungeon.levels.painters.Painter;
import com.watabou.utils.Random;

import java.util.Arrays;

public class FakeLastLevel extends Level {

	private static final int SIZE = 9;

	{
		color1 = 0x801500;
		color2 = 0xa68521;
	}
	
	private int pedestal;

	@Override
	protected String tilesTexXyz() {
		return Assets.TILES_HALLS_XYZ;
	}

	@Override
	public String tilesTex() {
		return Assets.TILES_HALLS;
	}
	
	@Override
	public String waterTex() {
		return Assets.WATER_HALLS;
	}
	
	@Override
	protected boolean build() {

		Arrays.fill( map, Terrain.WALL );
		Painter.fill( this, 2, 2, SIZE-2, SIZE-2, Terrain.WATER );
		Painter.fill( this, 3, 3, SIZE-4, SIZE-4, Terrain.EMPTY );
		Painter.fill( this, SIZE/2, SIZE/2, 3, 3, Terrain.EMPTY_SP );
		
		entrance = SIZE * getWidth() + SIZE / 2 + 1;
		map[entrance] = Terrain.ENTRANCE;

		setExit(entrance - getWidth() * ( SIZE - 1 ),0);
		map[getExit(0)] = Terrain.LOCKED_EXIT;

		pedestal = (SIZE / 2 + 1) * (getWidth() + 1);

		putLevelObject(LevelObjectsFactory.createCustomObject(this, LevelObjectsFactory.PEDESTAL, pedestal));
		putLevelObject(LevelObjectsFactory.createCustomObject(this, LevelObjectsFactory.STATUE, pedestal-1));
		putLevelObject(LevelObjectsFactory.createCustomObject(this, LevelObjectsFactory.STATUE, pedestal+1));

		setFeeling(Feeling.NONE);
		
		return true;
	}

	@Override
	protected void decorate() {
		for (int i=0; i < getLength(); i++) {
			if (map[i] == Terrain.EMPTY && Random.Int( 10 ) == 0) { 
				map[i] = Terrain.EMPTY_DECO;
			}
		}
	}

	@Override
	protected void createMobs() {

	}

	@Override
	protected void createItems() {
		drop( new PseudoAmulet(), pedestal );
	}
	
	@Override
	public int randomRespawnCell() {
		return -1;
	}

	@Override
	public String tileName( int tile ) {
		switch (tile) {
		case Terrain.WATER:
            return StringsManager.getVar(R.string.LastLevel_TileWater);
            case Terrain.GRASS:
                return StringsManager.getVar(R.string.LastLevel_TileGrass);
            case Terrain.HIGH_GRASS:
                return StringsManager.getVar(R.string.LastLevel_TileHighGrass);
            case Terrain.STATUE:
		case Terrain.STATUE_SP:
            return StringsManager.getVar(R.string.LastLevel_TileStatue);
            default:
			return super.tileName( tile );
		}
	}
	
	@Override
	public String tileDesc(int tile) {
		switch (tile) {
		case Terrain.WATER:
            return StringsManager.getVar(R.string.LastLevel_TileDescWater);
            case Terrain.STATUE:
		case Terrain.STATUE_SP:
            return StringsManager.getVar(R.string.LastLevel_TileDescStatue);
            default:
			return super.tileDesc(tile);
		}
	}
	
	@Override
	public void addVisuals( Scene scene ) {
		HallsLevel.addVisuals( this, scene );
	}

	@Override
	public boolean isBossLevel() {
		return true;
	}
}
