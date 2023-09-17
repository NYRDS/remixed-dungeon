
package com.watabou.pixeldungeon.levels.painters;


import com.nyrds.pixeldungeon.items.Treasury;
import com.watabou.pixeldungeon.items.Heap.Type;
import com.watabou.pixeldungeon.items.Item;
import com.watabou.pixeldungeon.items.keys.IronKey;
import com.watabou.pixeldungeon.levels.Level;
import com.watabou.pixeldungeon.levels.Room;
import com.watabou.pixeldungeon.levels.Terrain;
import com.watabou.utils.Point;
import com.watabou.utils.Random;

public class PitPainter extends Painter {

	public static void paint( Level level, Room room ) {

		fill( level, room, Terrain.WALL );
		fill( level, room, 1, Terrain.EMPTY );
		
		Room.Door entrance = room.entrance();
		entrance.set( Room.Door.Type.LOCKED );
		
		Point well = null;
		if (entrance.x == room.left) {
			well = new Point( room.right-1, Random.Int( 2 ) == 0 ? room.top + 1 : room.bottom - 1 );
		} else if (entrance.x == room.right) {
			well = new Point( room.left+1, Random.Int( 2 ) == 0 ? room.top + 1 : room.bottom - 1 );
		} else if (entrance.y == room.top) {
			well = new Point( Random.Int( 2 ) == 0 ? room.left + 1 : room.right - 1, room.bottom-1 );
		} else if (entrance.y == room.bottom) {
			well = new Point( Random.Int( 2 ) == 0 ? room.left + 1 : room.right - 1, room.top+1 );
		}
		set( level, well, Terrain.EMPTY_WELL );
		
		int remains = room.random(level);
		while (level.map[remains] == Terrain.EMPTY_WELL) {
			remains = room.random(level);
		}
		
		level.drop( new IronKey(), remains, Type.SKELETON);
		
		if (Random.Int( 5 ) == 0) {
			level.drop( Treasury.getLevelTreasury().random( Treasury.Category.RING ), remains, Type.HEAP );
		} else {
			level.drop( Treasury.getLevelTreasury().random( Random.oneOf(
					Treasury.Category.WEAPON,
					Treasury.Category.ARMOR
			) ), remains, Type.HEAP );
		}
		
		int n = Random.IntRange( 1, 2 );
		for (int i=0; i < n; i++) {
			level.drop( prize( level ), remains, Type.HEAP );
		}
	}
	
	private static Item prize( Level level ) {
		
		Item prize = level.itemToSpanAsPrize();
		if (prize != null) {
			return prize;
		}
		
		return Treasury.getLevelTreasury().random( Random.oneOf(
				Treasury.Category.POTION,
				Treasury.Category.SCROLL,
				Treasury.Category.FOOD,
				Treasury.Category.GOLD
		) );
	}
}
