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

import com.nyrds.pixeldungeon.levels.objects.Sign;
import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.platform.audio.Sample;
import com.nyrds.platform.util.StringsManager;
import com.watabou.noosa.Camera;
import com.watabou.noosa.Scene;
import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.effects.CellEmitter;
import com.watabou.pixeldungeon.effects.Speck;
import com.watabou.pixeldungeon.levels.painters.Painter;
import com.watabou.pixeldungeon.scenes.GameScene;
import com.watabou.utils.Random;

public class CavesBossLevel extends BossLevel {
	
	{
		color1 = 0x534f3e;
		color2 = 0xb9d661;
		
		viewDistance = 6;
	}

	@Override
	protected String tilesTexXyz() {
		return Assets.TILES_CAVES_XYZ;
	}

	@Override
	public String tilesTex() {
		return Assets.TILES_CAVES;
	}

	@Override
	public String tilesTexEx() {
		return Assets.TILES_CAVES_X;
	}

	@Override
	public String waterTex() {
		return Assets.WATER_CAVES;
	}

	@Override
	protected boolean build() {
		
		int topMost = Integer.MAX_VALUE;
		
		for (int i=0; i < 8; i++) {
			int left, right, top, bottom;
			if (Random.Int( 2 ) == 0) {
				left = Random.Int( 1, _RoomLeft() - 3 );
				right = _RoomRight() + 3;
			} else {
				left = _RoomLeft() - 3;
				right = Random.Int( _RoomRight() + 3, getWidth() - 1 );
			}
			if (Random.Int( 2 ) == 0) {
				top = Random.Int( 2, _RoomTop() - 3 );
				bottom = _RoomBottom() + 3;
			} else {
				top = _RoomLeft() - 3;
				bottom = Random.Int( _RoomTop() + 3, getHeight() - 1 );
			}
			
			Painter.fill( this, left, top, right - left + 1, bottom - top + 1, Terrain.EMPTY );
			
			if (top < topMost) {
				topMost = top;
				setExit(Random.Int( left, right ) + (top - 1) * getWidth(),0);
			}
		}
		
		map[getExit(0)] = Terrain.LOCKED_EXIT;
		
		for (int i=0; i < getLength(); i++) {
			if (map[i] == Terrain.EMPTY && Random.Int( 6 ) == 0) {
				map[i] = Terrain.INACTIVE_TRAP;
			}
		}
		
		Painter.fill( this, _RoomLeft() - 1, _RoomTop() - 1, 
			_RoomRight() - _RoomLeft() + 3, _RoomBottom() - _RoomTop() + 3, Terrain.WALL );
		Painter.fill( this, _RoomLeft(), _RoomTop() + 1, 
			_RoomRight() - _RoomLeft() + 1, _RoomBottom() - _RoomTop(), Terrain.EMPTY );
		
		Painter.fill( this, _RoomLeft(), _RoomTop(), 
			_RoomRight() - _RoomLeft() + 1, 1, Terrain.TOXIC_TRAP );
		
		arenaDoor = Random.Int( _RoomLeft(), _RoomRight() ) + (_RoomBottom() + 1) * getWidth();
		map[arenaDoor] = Terrain.DOOR;
		
		entrance = Random.Int( _RoomLeft() + 1, _RoomRight() - 1 ) + 
			Random.Int( _RoomTop() + 1, _RoomBottom() - 1 ) * getWidth();
		map[entrance] = Terrain.ENTRANCE;
		
		boolean[] patch = Patch.generate(this, 0.45f, 6 );
		for (int i=0; i < getLength(); i++) {
			if (map[i] == Terrain.EMPTY && patch[i]) {
				map[i] = Terrain.WATER;
			}
		}
		
		return true;
	}
	
	@Override
	protected void decorate() {	
		
		for (int i=getWidth() + 1; i < getLength() - getWidth(); i++) {
			if (map[i] == Terrain.EMPTY) {
				int n = 0;
				if (map[i+1] == Terrain.WALL) {
					n++;
				}
				if (map[i-1] == Terrain.WALL) {
					n++;
				}
				if (map[i+getWidth()] == Terrain.WALL) {
					n++;
				}
				if (map[i-getWidth()] == Terrain.WALL) {
					n++;
				}
				if (Random.Int( 8 ) <= n) {
					map[i] = Terrain.EMPTY_DECO;
				}
			}
		}
		
		for (int i=0; i < getLength(); i++) {
			if (map[i] == Terrain.WALL && Random.Int( 8 ) == 0) {
				map[i] = Terrain.WALL_DECO;
			}
		}
		
		int sign;
		do {
			sign = Random.Int( _RoomLeft(), _RoomRight() ) + Random.Int( _RoomTop(), _RoomBottom() ) * getWidth();
		} while (sign == entrance);
		addLevelObject(new Sign(sign,Dungeon.tip(this)));
	}

	@Override
	protected void pressHero(int cell, Hero hero) {
		
		super.pressHero( cell, hero );
		
		if (!enteredArena && outsideEntranceRoom( cell ) && hero == Dungeon.hero) {
			
			enteredArena = true;

			int pos;

			do {
				pos = Random.Int(getLength());
			} while (
					!passable[pos] ||
					!outsideEntranceRoom(pos ) ||
					Dungeon.isCellVisible(pos));

			spawnBoss(pos);

			set( arenaDoor, Terrain.WALL );
			GameScene.updateMap( arenaDoor );
			Dungeon.observe();
			
			CellEmitter.get( arenaDoor ).start( Speck.factory( Speck.ROCK ), 0.07f, 10 );
			Camera.main.shake( 3, 0.7f );
			Sample.INSTANCE.play( Assets.SND_ROCKS );
		}
	}
	
	private boolean outsideEntranceRoom(int cell ) {
		int cx = cell % getWidth();
		int cy = cell / getWidth();
		return cx < _RoomLeft()-1 || cx > _RoomRight()+1 || cy < _RoomTop()-1 || cy > _RoomBottom()+1;
	}
	
	@Override
	public String tileName( int tile ) {
		switch (tile) {
		case Terrain.GRASS:
            return StringsManager.getVar(R.string.Caves_TileGrass);
            case Terrain.HIGH_GRASS:
                return StringsManager.getVar(R.string.Caves_TileHighGrass);
            case Terrain.WATER:
                return StringsManager.getVar(R.string.Caves_TileWater);
            default:
			return super.tileName( tile );
		}
	}
	
	@Override
	public String tileDesc( int tile ) {
		switch (tile) {
		case Terrain.ENTRANCE:
            return StringsManager.getVar(R.string.Caves_TileDescEntrance);
            case Terrain.EXIT:
                return StringsManager.getVar(R.string.Caves_TileDescExit);
            case Terrain.HIGH_GRASS:
                return StringsManager.getVar(R.string.Caves_TileDescHighGrass);
            case Terrain.WALL_DECO:
                return StringsManager.getVar(R.string.Caves_TileDescDeco);
            default:
			return super.tileDesc( tile );
		}
	}
	
	@Override
	public void addVisuals( Scene scene ) {
		CavesLevel.addVisuals( this, scene );
	}

	private int _RoomLeft() {
		return getWidth() / 2 - 2;
	}

	private int _RoomRight() {
		return getWidth() / 2 + 2;
	}

	private int _RoomTop() {
		return getHeight() / 2 - 2;
	}

	private int _RoomBottom() {
		return getHeight() / 2 + 2;
	}
}
