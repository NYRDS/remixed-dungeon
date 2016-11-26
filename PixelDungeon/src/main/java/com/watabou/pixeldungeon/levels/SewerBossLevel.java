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

import com.nyrds.pixeldungeon.ml.R;
import com.watabou.noosa.Game;
import com.watabou.noosa.Scene;
import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.Bones;
import com.watabou.pixeldungeon.actors.mobs.Bestiary;
import com.watabou.pixeldungeon.actors.mobs.Mob;
import com.watabou.pixeldungeon.items.Heap;
import com.watabou.pixeldungeon.items.Item;
import com.watabou.pixeldungeon.levels.Room.Type;
import com.watabou.pixeldungeon.scenes.GameScene;
import com.watabou.utils.Bundle;
import com.watabou.utils.Graph;
import com.watabou.utils.Random;

import java.util.ArrayList;
import java.util.List;

public class SewerBossLevel extends RegularLevel {

	{
		color1 = 0x48763c;
		color2 = 0x59994a;
	}
	
	private int stairs = 0;
	
	@Override
	public String tilesTex() {
		return Assets.TILES_SEWERS;
	}
	
	@Override
	public String tilesTexEx() {
		return Assets.TILES_SEWERS_X;
	}
	
	@Override
	public String waterTex() {
		return Assets.WATER_SEWERS;
	}
	
	@Override
	protected boolean build() {
		
		initRooms();
	
		int distance;
		int retry = 0;
		int minDistance = (int)Math.sqrt( rooms.size() );
		do {
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
			} while (getRoomExit() == roomEntrance || getRoomExit().width() < 6 || getRoomExit().height() < 6 || getRoomExit().top == 0);
	
			Graph.buildDistanceMap( rooms, getRoomExit());
			distance = roomEntrance.distance();
			
			if (retry++ > 10) {
				return false;
			}
			
		} while (distance < minDistance);
		
		roomEntrance.type = Type.ENTRANCE;
		getRoomExit().type = Type.SEWER_BOSS_EXIT;

		placeSecondaryExits();

		Graph.buildDistanceMap( rooms, getRoomExit());
		List<Room> path = Graph.buildPath(roomEntrance, getRoomExit());
		
		Graph.setPrice( path, roomEntrance.distance );
		
		Graph.buildDistanceMap( rooms, getRoomExit());
		path = Graph.buildPath(roomEntrance, getRoomExit());
		
		Room room = roomEntrance;
		for (Room next : path) {
			room.connect( next );
			room = next;
		}
		
		room = (Room) getRoomExit().connected.keySet().toArray()[0];
		if (getRoomExit().top == room.bottom) {
			return false;
		}
		
		for (Room r : rooms) {
			if (r.type == Type.NULL && r.connected.size() > 0) {
				r.type = Type.TUNNEL; 
			}
		}
		
		ArrayList<Room> candidates = new ArrayList<>();
		for (Room r : getRoomExit().neighbours) {
			if (!getRoomExit().connected.containsKey( r ) &&
				(getRoomExit().left == r.right || getRoomExit().right == r.left || getRoomExit().bottom == r.top) &&
				!(r.type == Type.EXIT)
					) {
				candidates.add( r );
			}
		}
		if (!candidates.isEmpty()) {
			Room kingsRoom = Random.element( candidates );
			kingsRoom.connect(getRoomExit());
			kingsRoom.type = Room.Type.RAT_KING;
		}

		Graph.setPrice( path, roomEntrance.distance );

		paint();
		
		paintWater();
		
		paintGrass();
		
		placeTraps();
		
		return true;
	}
		
	protected boolean[] water() {
		return Patch.generate(this, 0.5f, 5 );
	}
	
	protected boolean[] grass() {
		return Patch.generate(this, 0.40f, 4 );
	}
	
	@Override
	protected void decorate() {	
		int start = getRoomExit().top * getWidth() + getRoomExit().left + 1;
		int end = start + getRoomExit().width() - 1;
		for (int i=start; i < end; i++) {
			if (map[i]==Terrain.WALL) {
				map[i] = Terrain.WALL_DECO;
				map[i + getWidth()] = Terrain.WATER;
			} else {
				map[i + getWidth()] = Terrain.EMPTY;
			}
		}

		placeEntranceSign();
	}
	
	@Override
	public void addVisuals( Scene scene ) {
		SewerLevel.addVisuals( this, scene );
	}
	
	
	@Override
	protected void createMobs() {
		Mob mob = Bestiary.mob(  );

		mob.setPos(getEmptyCellNextTo(getRoomExit().random(this)));
		mobs.add( mob );
	}
	
	@Override
	protected void createItems() {
		Item item = Bones.get();
		if (item != null) {
			int pos;
			do {
				pos = roomEntrance.random(this);
			} while (pos == entrance || map[pos] == Terrain.SIGN);
			drop( item, pos ).type = Heap.Type.SKELETON;
		}
	}
	
	public void seal() {
		if (entrance != 0) {
			
			set( entrance, Terrain.WATER_TILES );
			GameScene.updateMap( entrance );
			GameScene.ripple( entrance );
			
			stairs = entrance;
			entrance = 0;
		}
	}
	
	public void unseal() {
		if (stairs != 0) {
			
			entrance = stairs;
			stairs = 0;
			
			set( entrance, Terrain.ENTRANCE );
			GameScene.updateMap( entrance );
		}
	}
	
	private static final String STAIRS	= "stairs";
	
	@Override
	public void storeInBundle( Bundle bundle ) {
		super.storeInBundle( bundle );
		bundle.put( STAIRS, stairs );
	}
	
	@Override
	public void restoreFromBundle( Bundle bundle ) {
		super.restoreFromBundle( bundle );
		stairs = bundle.getInt( STAIRS );
	}
	
	@Override
	public String tileName( int tile ) {
		switch (tile) {
		case Terrain.WATER:
			return Game.getVar(R.string.Sewer_TileWater);
		default:
			return super.tileName( tile );
		}
	}
	
	@Override
	public String tileDesc(int tile) {
		switch (tile) {
		case Terrain.EMPTY_DECO:
			return Game.getVar(R.string.Sewer_TileDescDeco);
		default:
			return super.tileDesc( tile );
		}
	}
	
	@Override
	public boolean isBossLevel() {
		return true;
	}
}
