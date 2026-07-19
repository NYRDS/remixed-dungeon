
package com.watabou.pixeldungeon.levels.painters;

import com.nyrds.pixeldungeon.items.Treasury;
import com.nyrds.pixeldungeon.levels.objects.LevelObjectsFactory;
import com.nyrds.platform.EventCollector;
import com.watabou.pixeldungeon.items.Heap.Type;
import com.watabou.pixeldungeon.items.Item;
import com.watabou.pixeldungeon.items.keys.GoldenKey;
import com.watabou.pixeldungeon.items.keys.IronKey;
import com.watabou.pixeldungeon.levels.Level;
import com.watabou.pixeldungeon.levels.Room;
import com.watabou.pixeldungeon.levels.Terrain;
import com.watabou.utils.Random;

public class VaultPainter extends Painter {

	public static void paint( Level level, Room room ) {

		fill( level, room, Terrain.WALL );
		fill( level, room, 1, Terrain.EMPTY );
		
		int cx = (room.left + room.right) / 2;
		int cy = (room.top + room.bottom) / 2;
		int c = cx + cy * level.getWidth();
		
		switch (Random.Int( 3 )) {
		
		case 0:
			level.drop( prize(level), c, Type.LOCKED_CHEST);
			level.addItemToSpawn( new GoldenKey() );
			break;
			
		case 1:
			Item i1 = prize(level);
			Item i2 = prize(level);
			int count = 0;
			while (i1.getClass() == i2.getClass()) {
				if (count++ > 100) {
					// Couldn't draw two distinct prize classes (wand/ring pool collapsed to one
					// type, or Treasury returned its Gold fallback). Stop retrying — accept this
					// pair rather than spinning forever and freezing level generation.
					EventCollector.logException(new Exception("VaultPainter: too many attempts to get distinct prizes"));
					break;
				}
				i2 = prize(level);
			}
			level.drop( i1, c, Type.CRYSTAL_CHEST);
			level.drop( i2, c + Level.NEIGHBOURS8[Random.Int( 8 )], Type.CRYSTAL_CHEST);
			level.addItemToSpawn( new GoldenKey() );
			break;
			
		case 2:
			level.drop( prize(level), c, Type.HEAP );
			level.putLevelObject( LevelObjectsFactory.PEDESTAL, c);
			break;
		}
		
		room.entrance().set( Room.Door.Type.LOCKED );
		level.addItemToSpawn( new IronKey() );
	}
	
	private static Item prize(Level level) {
		return Treasury.getLevelTreasury().random( Random.oneOf(
			Treasury.Category.WAND,
			Treasury.Category.RING
		) );
	}
}
