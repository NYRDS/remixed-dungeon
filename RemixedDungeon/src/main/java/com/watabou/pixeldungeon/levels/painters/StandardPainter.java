
package com.watabou.pixeldungeon.levels.painters;

import com.nyrds.pixeldungeon.items.Treasury;
import com.nyrds.pixeldungeon.levels.objects.LevelObjectsFactory;
import com.nyrds.pixeldungeon.levels.objects.Trap;
import com.nyrds.pixeldungeon.mobs.common.Crystal;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.items.Gold;
import com.watabou.pixeldungeon.items.Heap;
import com.watabou.pixeldungeon.levels.Level;
import com.watabou.pixeldungeon.levels.Room;
import com.watabou.pixeldungeon.levels.Terrain;
import com.nyrds.util.Point;
import com.nyrds.util.Random;

public class StandardPainter extends Painter {

	public static void paint( Level level, Room room ) {
		
		fill( level, room, Terrain.WALL );
		for (Room.Door door : room.connected.values()) {
			door.set( Room.Door.Type.REGULAR );
		}
		
		if (!level.isBossLevel() && Random.Int( 5 ) == 0) {
			switch (Random.Int( 6 )) {
			case 0:
				if (level.getFeeling() != Level.Feeling.GRASS) {
					if (Math.min( room.width(), room.height() ) >= 4 && Math.max( room.width(), room.height() ) >= 6) {
						paintGraveyard( level, room );
						return;
					}
					break;
				} else {
					// Burned room
				}
			case 1:
				if (Dungeon.depth > 1) {
					paintBurned( level, room );
					return;
				}
				break;
			case 2:
				if (Math.max( room.width(), room.height() ) >= 4) {
					paintStriped( level, room );
					return;
				}
				break;
			case 3:
				if (room.width() >= 6 && room.height() >= 6) {
					paintStudy( level, room );
					return;
				}
				break;
			case 4:
				if (level.getFeeling() != Level.Feeling.WATER) {
					if (room.connected.size() == 2 && room.width() >= 4 && room.height() >= 4) {
						paintBridge( level, room );
						return;
					}
					break;
				} else {
					// Fissure
				}
			case 5:
				if (!level.isBossLevel() && Math.min( room.width(), room.height() ) >= 5) {
					paintFissure( level, room );
					return;
				}
				break;
			}
		}
		
		fill( level, room, 1, Terrain.EMPTY );
	}
	
	private static void paintBurned( Level level, Room room ) {
		for (int i=room.top + 1; i < room.bottom; i++) {
			for (int j=room.left + 1; j < room.right; j++) {
				int t = Terrain.EMBERS;
				final int pos = i * level.getWidth() + j;
				level.map[pos] = t;
				switch (Random.Int( 5 )) {
				case 1:
					level.putLevelObject(Trap.makeSimpleTrap(pos, LevelObjectsFactory.FIRE_TRAP, false));
					break;
				case 2:
					level.putLevelObject(Trap.makeSimpleTrap(pos, LevelObjectsFactory.FIRE_TRAP, true));
					break;
				case 3:
					t = Terrain.INACTIVE_TRAP;
					break;
				}
				level.map[pos] = t;
			}
		}
	}
	
	private static void paintGraveyard( Level level, Room room ) {
		fill( level, room.left + 1, room.top + 1, room.width() - 1, room.height() - 1 , Terrain.GRASS );
		
		int w = room.width() - 1;
		int h = room.height() - 1;
		int nGraves = Math.max( w, h ) / 2;
		
		int index = Random.Int( nGraves );
		
		int shift = Random.Int( 2 );
		for (int i=0; i < nGraves; i++) {
			int pos = w > h ?
				room.left + 1 + shift + i * 2 + (room.top + 2 + Random.Int( h-2 )) * level.getWidth() :
				(room.left + 2 + Random.Int( w-2 )) + (room.top + 1 + shift + i * 2) * level.getWidth();	
			level.drop( i == index ? Treasury.getLevelTreasury().random() : new Gold(), pos, Heap.Type.TOMB);
		}
	}
	
	private static void paintStriped( Level level, Room room ) {
		fill( level, room.left + 1, room.top + 1, room.width() - 1, room.height() - 1 , Terrain.EMPTY_SP );

		if (room.width() > room.height()) {
			for (int i=room.left + 2; i < room.right; i += 2) {
				fill( level, i, room.top + 1, 1, room.height() - 1, Terrain.HIGH_GRASS );
			}
		} else {
			for (int i=room.top + 2; i < room.bottom; i += 2) {
				fill( level, room.left + 1, i, room.width() - 1, 1, Terrain.HIGH_GRASS );
			}
		}
	}
	
	private static void paintStudy( Level level, Room room ) {
		fill( level, room.left + 1, room.top + 1, room.width() - 1, room.height() - 1 , Terrain.BOOKSHELF );
		fill( level, room.left + 2, room.top + 2, room.width() - 3, room.height() - 3 , Terrain.EMPTY_SP );
		
		for (Point door : room.connected.values()) {
			if (door.x == room.left) {
				set( level, door.x + 1, door.y, Terrain.EMPTY );
			} else if (door.x == room.right) {
				set( level, door.x - 1, door.y, Terrain.EMPTY );
			} else if (door.y == room.top) {
				set( level, door.x, door.y + 1, Terrain.EMPTY );
			} else if (door.y == room.bottom) {
				set( level, door.x , door.y - 1, Terrain.EMPTY );
			}	
		}
		
		Point roomCenter = room.center();
		level.putLevelObject(LevelObjectsFactory.createCustomObject(level, LevelObjectsFactory.PEDESTAL, level.cell(roomCenter.x, roomCenter.y)));

		if(Random.Float(1) < 0.25f) {
			Crystal crystal = new Crystal();
			crystal.setPos(level.cell(roomCenter.x, roomCenter.y));
			level.mobs.add( crystal );
		}
	}
	
	private static void paintBridge( Level level, Room room ) {
		
		fill( level, room.left + 1, room.top + 1, room.width() - 1, room.height() - 1 ,  
			!level.isBossLevel()  && Random.Int( 3 ) == 0 ? 
				Terrain.CHASM : 
				Terrain.WATER );
		
		Point door1 = null;
		Point door2 = null;
		for (Point p : room.connected.values()) {
			if (door1 == null) {
				door1 = p;
			} else {
				door2 = p;
			}
		}
		
		if ((door1.x == room.left && door2.x == room.right) || 
			(door1.x == room.right && door2.x == room.left)) {
			
			int s = room.width() / 2;
			
			drawInside( level, room, door1, s, Terrain.EMPTY_SP );
			drawInside( level, room, door2, s, Terrain.EMPTY_SP );
			fill( level, room.center().x, Math.min( door1.y, door2.y ), 1, Math.abs( door1.y - door2.y ) + 1, Terrain.EMPTY_SP );
			
		} else 
		if ((door1.y == room.top && door2.y == room.bottom) || 
			(door1.y == room.bottom && door2.y == room.top)) {
			
			int s = room.height() / 2;
			
			drawInside( level, room, door1, s, Terrain.EMPTY_SP );
			drawInside( level, room, door2, s, Terrain.EMPTY_SP );
			fill( level, Math.min( door1.x, door2.x ), room.center().y, Math.abs( door1.x - door2.x ) + 1, 1, Terrain.EMPTY_SP );
			
		} else 
		if (door1.x == door2.x) {
			
			fill( level, door1.x == room.left ? room.left + 1 : room.right - 1, Math.min( door1.y, door2.y ), 1, Math.abs( door1.y - door2.y ) + 1, Terrain.EMPTY_SP );
			
		} else
		if (door1.y == door2.y) {
			
			fill( level, Math.min( door1.x, door2.x ), door1.y == room.top ? room.top + 1 : room.bottom - 1, Math.abs( door1.x - door2.x ) + 1, 1, Terrain.EMPTY_SP );
			
		} else
		if (door1.y == room.top || door1.y == room.bottom) {
			
			drawInside( level, room, door1, Math.abs( door1.y - door2.y ), Terrain.EMPTY_SP );
			drawInside( level, room, door2, Math.abs( door1.x - door2.x ), Terrain.EMPTY_SP );
			
		} else 
		if (door1.x == room.left || door1.x == room.right) {
			
			drawInside( level, room, door1, Math.abs( door1.x - door2.x ), Terrain.EMPTY_SP );
			drawInside( level, room, door2, Math.abs( door1.y - door2.y ), Terrain.EMPTY_SP );
			
		}
	}
	
	private static void paintFissure( Level level, Room room ) {
		fill( level, room.left + 1, room.top + 1, room.width() - 1, room.height() - 1 ,Terrain.EMPTY );
		
		for (int i=room.top + 2; i < room.bottom - 1; i++) {
			for (int j=room.left + 2; j < room.right - 1; j++) {
				int v = Math.min( i - room.top, room.bottom - i );
				int h = Math.min( j - room.left, room.right - j );
				if (Math.min( v, h ) > 2 || Random.Int( 2 ) == 0) {
					set( level, j, i, Terrain.CHASM );
				}
			}
		}
	}
}
