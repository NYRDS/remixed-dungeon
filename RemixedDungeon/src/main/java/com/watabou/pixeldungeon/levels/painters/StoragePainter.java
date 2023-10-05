
package com.watabou.pixeldungeon.levels.painters;

import com.nyrds.pixeldungeon.items.Treasury;
import com.watabou.pixeldungeon.items.Heap;
import com.watabou.pixeldungeon.items.Item;
import com.watabou.pixeldungeon.items.potions.PotionOfLiquidFlame;
import com.watabou.pixeldungeon.levels.Level;
import com.watabou.pixeldungeon.levels.Room;
import com.watabou.pixeldungeon.levels.Terrain;
import com.watabou.utils.Random;

public class StoragePainter extends Painter {

	public static void paint( Level level, Room room ) {
		
		final int floor = Terrain.EMPTY_SP;
		
		fill( level, room, Terrain.WALL );
		fill( level, room, 1, floor );
		
		int n = Random.IntRange( 3, 4 );
		for (int i=0; i < n; i++) { 
			int pos;
			do {
				pos = room.random(level);
			} while (level.map[pos] != floor);
			level.drop( prize( level ), pos, Heap.Type.HEAP );
		}
		
		room.entrance().set( Room.Door.Type.BARRICADE );
		level.addItemToSpawn( new PotionOfLiquidFlame() );
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
