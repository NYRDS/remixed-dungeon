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
package com.watabou.pixeldungeon.levels;

import com.nyrds.pixeldungeon.levels.objects.LevelObjectsFactory;
import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.platform.util.StringsManager;
import com.watabou.noosa.Scene;
import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.items.Amulet;
import com.watabou.pixeldungeon.items.Heap;
import com.watabou.pixeldungeon.levels.painters.Painter;
import com.watabou.utils.Random;

import java.util.Arrays;

public class LastLevel extends Level {

	private static final int SIZE = 7;
	
	{
		color1 = 0x801500;
		color2 = 0xa68521;
	}
	
	private int pedestal;
	
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
		Painter.fill( this, 1, 1, SIZE, SIZE, Terrain.WATER );
		Painter.fill( this, 2, 2, SIZE-2, SIZE-2, Terrain.EMPTY );
		Painter.fill( this, SIZE/2, SIZE/2, 3, 3, Terrain.EMPTY_SP );
		
		entrance = SIZE * getWidth() + SIZE / 2 + 1;
		map[entrance] = Terrain.ENTRANCE;

		//setExit(entrance - getWidth() * SIZE,0);
		//map[getExit(0)] = Terrain.LOCKED_EXIT;

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
		drop( new Amulet(), pedestal, Heap.Type.HEAP );
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
			return super.tileDesc( tile );
		}
	}
	
	@Override
	public void addVisuals( Scene scene ) {
		HallsLevel.addVisuals( this, scene );
	}
}
