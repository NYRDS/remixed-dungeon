
package com.watabou.pixeldungeon.levels.painters;

import com.nyrds.pixeldungeon.items.Treasury;
import com.nyrds.pixeldungeon.levels.objects.Barrel;
import com.watabou.pixeldungeon.items.Heap;
import com.watabou.pixeldungeon.items.Item;
import com.watabou.pixeldungeon.levels.Level;
import com.watabou.pixeldungeon.levels.Room;
import com.watabou.pixeldungeon.levels.Terrain;
import com.watabou.utils.Random;

public class WarehousePainter extends Painter {

	public static void paint( Level level, Room room ) {

		fill( level, room, Terrain.WALL );
		fill( level, room, 1, Terrain.EMPTY_SP );
		
		Room.Door entrance = room.entrance();
		entrance.set( Room.Door.Type.HIDDEN );
		
		for (int i=room.left + 1; i < room.right; i++) {
			for (int j=room.top + 1; j < room.bottom; j++) {
				if(Math.random() < 0.5) {
					level.addLevelObject(new Barrel(level.cell(i, j)));
				} else {
					Item prize = Random.oneOf(Treasury.getLevelTreasury().random(Treasury.Category.BULLETS),
							Treasury.getLevelTreasury().random(Treasury.Category.THROWABLE));
					level.drop(prize, level.cell(i,j), Heap.Type.HEAP);
				}
			}
		}
	}
	
}
