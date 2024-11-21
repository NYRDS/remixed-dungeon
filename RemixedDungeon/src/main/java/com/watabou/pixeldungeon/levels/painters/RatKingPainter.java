
package com.watabou.pixeldungeon.levels.painters;

import com.nyrds.pixeldungeon.items.Treasury;
import com.watabou.pixeldungeon.actors.mobs.npcs.RatKing;
import com.watabou.pixeldungeon.items.Gold;
import com.watabou.pixeldungeon.items.Heap;
import com.watabou.pixeldungeon.items.Item;
import com.watabou.pixeldungeon.items.weapon.missiles.MissileWeapon;
import com.watabou.pixeldungeon.levels.Level;
import com.watabou.pixeldungeon.levels.Room;
import com.watabou.pixeldungeon.levels.Terrain;
import com.nyrds.util.Random;

public class RatKingPainter extends Painter {

	public static void paint( Level level, Room room ) {

		fill( level, room, Terrain.WALL );
		fill( level, room, 1, Terrain.EMPTY_SP );
		
		Room.Door entrance = room.entrance();
		entrance.set( Room.Door.Type.HIDDEN );
		int door = entrance.x + entrance.y * level.getWidth();
		
		for (int i=room.left + 1; i < room.right; i++) {
			addChest( level, (room.top + 1) * level.getWidth() + i, door );
			addChest( level, (room.bottom - 1) * level.getWidth() + i, door );
		}
		
		for (int i=room.top + 2; i < room.bottom - 1; i++) {
			addChest( level, i * level.getWidth() + room.left + 1, door );
			addChest( level, i * level.getWidth() + room.right - 1, door );
		}
		
		while (true) {
			Heap chest = level.getHeap( room.random(level) );
			if (chest != null) {
				chest.type = Heap.Type.MIMIC;
				break;
			}
		}
		
		RatKing king = new RatKing();
		king.setPos(room.random(level, 1 ));
		level.mobs.add( king );
	}
	
	private static void addChest( Level level, int pos, int door ) {
		
		if (pos == door - 1 || 
			pos == door + 1 || 
			pos == door - level.getWidth() || 
			pos == door + level.getWidth()) {
			return;
		}
		
		Item prize;
		switch (Random.Int( 10 )) {
		case 0:
			prize = Treasury.getLevelTreasury().random( Treasury.Category.WEAPON );
			if (prize instanceof MissileWeapon) {
				prize.quantity( 1 );
			} else {
				prize.degrade( Random.Int( 3 ) );
			}
			break;
		case 1:
			prize = Treasury.getLevelTreasury().random( Treasury.Category.ARMOR ).degrade( Random.Int( 3 ) );
			break;
		default:
			prize = new Gold( Random.IntRange( 1, 5 ) );
			break;
		}
		
		level.drop( prize, pos,  Heap.Type.CHEST);
	}
}
