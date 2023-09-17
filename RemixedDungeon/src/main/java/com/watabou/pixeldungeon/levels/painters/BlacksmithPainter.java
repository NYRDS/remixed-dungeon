
package com.watabou.pixeldungeon.levels.painters;

import com.nyrds.pixeldungeon.items.Treasury;
import com.watabou.pixeldungeon.actors.Actor;
import com.watabou.pixeldungeon.actors.mobs.npcs.Blacksmith;
import com.watabou.pixeldungeon.items.Heap;
import com.watabou.pixeldungeon.levels.Level;
import com.watabou.pixeldungeon.levels.Room;
import com.watabou.pixeldungeon.levels.Terrain;
import com.watabou.utils.Random;

public class BlacksmithPainter extends Painter {

	public static void paint( Level level, Room room ) {

		fill( level, room, Terrain.WALL );
		fill( level, room, 1, Terrain.FIRE_TRAP );
		fill( level, room, 2, Terrain.EMPTY_SP );
		
		for (int i=0; i < 2; i++) {
			int pos;
			do {
				pos = room.random(level);
			} while (level.map[pos] != Terrain.EMPTY_SP);
			level.drop( 
				Treasury.getLevelTreasury().random( Random.oneOf(
					Treasury.Category.ARMOR,
					Treasury.Category.WEAPON
				) ), pos, Heap.Type.HEAP );
		}
		
		for (Room.Door door : room.connected.values()) {
			door.set( Room.Door.Type.UNLOCKED );
			drawInside( level, room, door, 1, Terrain.EMPTY );
		}
		
		Blacksmith npc = new Blacksmith();
		do {
			npc.setPos(room.random(level, 1 ));
		} while (level.getHeap( npc.getPos() ) != null);
		level.mobs.add( npc );
		Actor.occupyCell( npc );
	}
}
