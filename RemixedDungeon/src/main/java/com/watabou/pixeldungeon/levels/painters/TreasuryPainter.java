
package com.watabou.pixeldungeon.levels.painters;

import com.nyrds.pixeldungeon.levels.objects.LevelObjectsFactory;
import com.watabou.pixeldungeon.items.Gold;
import com.watabou.pixeldungeon.items.Heap;
import com.watabou.pixeldungeon.items.keys.IronKey;
import com.watabou.pixeldungeon.levels.Level;
import com.watabou.pixeldungeon.levels.Room;
import com.watabou.pixeldungeon.levels.Terrain;
import com.watabou.utils.Random;

import lombok.val;

public class TreasuryPainter extends Painter {

	public static void paint( Level level, Room room ) {

		fill( level, room, Terrain.WALL );
		fill( level, room, 1, Terrain.EMPTY );

		val c = room.center();
		level.putLevelObject( LevelObjectsFactory.STATUE, level.cell(c.x,c.y));
		
		Heap.Type heapType = Random.Int( 2 ) == 0 ? Heap.Type.CHEST : Heap.Type.HEAP;
		
		int n = Random.IntRange( 2, 3 );
		for (int i=0; i < n; i++) {
			int pos;
			do {
				pos = room.random(level);
			} while (!level.isCellSafeForPrize(pos));
			level.drop( new Gold().random(), pos, i == 0 && heapType == Heap.Type.CHEST ? Heap.Type.MIMIC : heapType);
		}
		
		if (heapType == Heap.Type.HEAP) {
			for (int i=0; i < 6; i++) {
				int pos;
				do {
					pos = room.random(level);
				} while (!level.isCellSafeForPrize(pos));
				level.drop( new Gold( Random.IntRange( 1, 3 ) ), pos, Heap.Type.HEAP );
			}
		}
		
		room.entrance().set( Room.Door.Type.LOCKED );
		level.addItemToSpawn( new IronKey() );
	}
}
