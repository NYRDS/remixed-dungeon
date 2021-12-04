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

import com.nyrds.pixeldungeon.levels.LevelTools;
import com.nyrds.pixeldungeon.levels.objects.LevelObjectsFactory;
import com.nyrds.pixeldungeon.levels.objects.Trap;
import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.platform.util.StringsManager;
import com.watabou.noosa.Scene;
import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.items.Heap;
import com.watabou.pixeldungeon.items.keys.IronKey;
import com.watabou.pixeldungeon.levels.Room.Type;
import com.watabou.pixeldungeon.levels.painters.Painter;
import com.watabou.utils.Graph;
import com.watabou.utils.Point;
import com.watabou.utils.Random;

import java.util.List;

public class PrisonBossLevel extends BossLevel {

	{
		color1 = 0x6a723d;
		color2 = 0x88924c;
	}
	
	private Room anteroom;

	@Override
	protected String tilesTexXyz() {
		return Assets.TILES_PRISON_XYZ;
	}

	@Override
	public String tilesTex() {
		return Assets.TILES_PRISON_BOSS;
	}
	
	@Override
	public String waterTex() {
		return Assets.WATER_PRISON;
	}

	@Override
	protected boolean build() {
		
		initRooms();
	
		int distance;
		int retry = 0;

		do {
			
			if (retry++ > 10) {
				return false;
			}
			
			int innerRetry = 0;
			do {
				if (innerRetry++ > 10) {
					return false;
				}
				roomEntrance = Random.element( rooms );
			} while (roomEntrance.width() < 4 || roomEntrance.height() < 4);
			
			innerRetry = 0;
			do {
				if (innerRetry++ > 10) {
					return false;
				}
				setRoomExit(Random.element( rooms ));
			} while (
				getRoomExit() == roomEntrance ||
				getRoomExit().width() < 7 ||
				getRoomExit().height() < 7 ||
				getRoomExit().top == 0);
	
			Graph.buildDistanceMap( rooms, getRoomExit());
			distance = Graph.buildPath(roomEntrance, getRoomExit()).size();
			
		} while (distance < 3);
		
		roomEntrance.type = Type.ENTRANCE;
		getRoomExit().type = Type.PRISON_BOSS_EXIT;
		
		List<Room> path = Graph.buildPath(roomEntrance, getRoomExit());
		Graph.setPrice( path, roomEntrance.distance );
		
		Graph.buildDistanceMap( rooms, getRoomExit());
		path = Graph.buildPath(roomEntrance, getRoomExit());
		
		anteroom = path.get( path.size() - 2 );
		anteroom.type = Type.STANDARD;
		
		Room room = roomEntrance;
		for (Room next : path) {
			room.connect( next );
			room = next;
		}
		
		for (Room r : rooms) {
			if (r.type == Type.NULL && r.connected.size() > 0) {
				r.type = Type.PASSAGE; 
			}
		}
		
		paint();
		
		Room r = (Room) getRoomExit().connected.keySet().toArray()[0];
		if (getRoomExit().connected.get( r ).y == getRoomExit().top) {
			return false;
		}
		
		paintWater();
		paintGrass();
		
		placeTraps();
		
		return true;
	}
		
	protected boolean[] water() {
		return Patch.generate(this, 0.45f, 5 );
	}
	
	protected boolean[] grass() {
		return Patch.generate(this, 0.30f, 4 );
	}
	
	protected void paintDoors( Room r ) {
		
		for (Room n : r.connected.keySet()) {
			
			if (r.type == Type.NULL) {
				continue;
			}
			
			Point door = r.connected.get( n );
			
			if (r.type == Room.Type.PASSAGE && n.type == Room.Type.PASSAGE) {
				
				Painter.set( this, door, Terrain.EMPTY );
				
			} else {
				
				Painter.set( this, door, Terrain.DOOR );
				
			}
			
		}
	}
	
	@Override
	protected void placeTraps() {
		
		int nTraps = nTraps();

		for (int i=0; i < nTraps; i++) {
			
			int trapPos = Random.Int( getLength() );
			
			if (map[trapPos] == Terrain.EMPTY) {
				addLevelObject(Trap.makeSimpleTrap(trapPos, LevelObjectsFactory.POISON_TRAP, true));
			}
		}
	}
	
	@Override
	protected void decorate() {	
		
		for (int i=getWidth() + 1; i < getLength() - getWidth() - 1; i++) {
			if (map[i] == Terrain.EMPTY) { 
				
				float c = 0.15f;
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

		LevelTools.northWallDecorate(this, 10, 2);

		placeEntranceSign();
		
		Point door = getRoomExit().entrance();
		arenaDoor = door.x + door.y * getWidth();
		Painter.set( this, arenaDoor, Terrain.LOCKED_DOOR );
		
		Painter.fill( this, 
			getRoomExit().left + 2,
			getRoomExit().top + 2,
			getRoomExit().width() - 3,
			getRoomExit().height() - 3,
			Terrain.INACTIVE_TRAP );
	}
	
	@Override
	protected void createMobs() {
	}
	
	@Override
	protected void createItems() {

		int keyPos = anteroom.random(this);
		while (!passable[keyPos]) {
			keyPos = anteroom.random(this);
		}
		drop( new IronKey(), keyPos, Heap.Type.CHEST);
		
		dropBones();
	}

	@Override
	protected void pressHero(int cell, Hero ch) {
		
		super.pressHero( cell, ch );
		
		if (!enteredArena && getRoomExit().inside( cell )) {
			enteredArena = true;
			spawnBoss(getEmptyCellFromRoom(getRoomExit()));
		}
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
            default:
			return super.tileDesc( tile );
		}
	}
	
	@Override
	public void addVisuals( Scene scene ) {
		PrisonLevel.addVisuals( this, scene );
	}

	@Override
	public int objectsKind() {
		return 1;
	}
}
