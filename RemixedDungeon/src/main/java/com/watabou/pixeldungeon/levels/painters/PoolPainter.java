
package com.watabou.pixeldungeon.levels.painters;

import com.nyrds.pixeldungeon.items.DummyItem;
import com.nyrds.pixeldungeon.items.Treasury;
import com.nyrds.pixeldungeon.levels.objects.LevelObjectsFactory;
import com.watabou.pixeldungeon.actors.Actor;
import com.watabou.pixeldungeon.actors.mobs.Piranha;
import com.watabou.pixeldungeon.items.Heap;
import com.watabou.pixeldungeon.items.Item;
import com.watabou.pixeldungeon.items.potions.PotionOfInvisibility;
import com.watabou.pixeldungeon.levels.Level;
import com.watabou.pixeldungeon.levels.Room;
import com.watabou.pixeldungeon.levels.Terrain;
import com.watabou.utils.Random;

public class PoolPainter extends Painter {

	private static final int NPIRANHAS	= 3;
	
	public static void paint( Level level, Room room ) {
		
		fill( level, room, Terrain.WALL );
		fill( level, room, 1, Terrain.WATER );
		
		Room.Door door = room.entrance(); 
		door.set( Room.Door.Type.REGULAR );

		int x = -1;
		int y = -1;
		if (door.x == room.left) {
			
			x = room.right - 1;
			y = room.top + room.height() / 2;
			
		} else if (door.x == room.right) {
			
			x = room.left + 1;
			y = room.top + room.height() / 2;
			
		} else if (door.y == room.top) {
			
			x = room.left + room.width() / 2;
			y = room.bottom - 1;
			
		} else if (door.y == room.bottom) {
			
			x = room.left + room.width() / 2;
			y = room.top + 1;
			
		}
		
		int pos = x + y * level.getWidth();
		level.drop( prize( level ), pos, Random.Int( 3 ) == 0 ? Heap.Type.CHEST : Heap.Type.HEAP);

		set( level, pos, Terrain.EMPTY );
		level.putLevelObject( LevelObjectsFactory.PEDESTAL, pos);

		
		level.addItemToSpawn( new PotionOfInvisibility() );
		
		for (int i=0; i < NPIRANHAS; i++) {
			Piranha piranha = new Piranha();
			int newPos;
			do {
				newPos = room.random(level);
			} while (level.map[newPos] != Terrain.WATER|| Actor.findChar( newPos ) != null);

			piranha.setPos(newPos);
			level.mobs.add( piranha );
			Actor.occupyCell( piranha );
		}
	}
	
	private static Item prize( Level level ) {
		
		Item prize = level.itemToSpanAsPrize();
		if (! (prize instanceof DummyItem)) {
			return prize;
		}
		
		prize = Treasury.weaponOrArmorPrize(4);
		
		return prize;
	}
}
