
package com.watabou.pixeldungeon.levels.painters;

import static com.nyrds.pixeldungeon.levels.objects.LevelObjectsFactory.STATUE;

import com.nyrds.pixeldungeon.mobs.common.ArmoredStatue;
import com.watabou.pixeldungeon.Challenges;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.Actor;
import com.watabou.pixeldungeon.actors.mobs.Mob;
import com.watabou.pixeldungeon.actors.mobs.Statue;
import com.watabou.pixeldungeon.items.keys.IronKey;
import com.watabou.pixeldungeon.levels.Level;
import com.watabou.pixeldungeon.levels.Room;
import com.watabou.pixeldungeon.levels.Terrain;
import com.watabou.utils.Point;
import com.watabou.utils.Random;

public class StatuePainter extends Painter {

	public static void paint( Level level, Room room ) {

		fill( level, room, Terrain.WALL );
		fill( level, room, 1, Terrain.EMPTY );

		Point c = room.center();
		int cx = c.x;
		int cy = c.y;
		
		Room.Door door = room.entrance();
		
		door.set( Room.Door.Type.LOCKED );
		level.addItemToSpawn( new IronKey() );
		
		if (door.x == room.left) {
			
			fill( level, room.right - 1, room.top + 1, 1, room.height() - 1 , STATUE);
			cx = room.right - 2;
			
		} else if (door.x == room.right) {
			
			fill( level, room.left + 1, room.top + 1, 1, room.height() - 1 , STATUE );
			cx = room.left + 2;
			
		} else if (door.y == room.top) {
			
			fill( level, room.left + 1, room.bottom - 1, room.width() - 1, 1 , STATUE );
			cy = room.bottom - 2;
			
		} else if (door.y == room.bottom) {
			
			fill( level, room.left + 1, room.top + 1, room.width() - 1, 1 , STATUE );
			cy = room.top + 2;
			
		}

		Mob statue = null;

		if(Dungeon.isChallenged(Challenges.NO_ARMOR) && Dungeon.isChallenged(Challenges.NO_WEAPON)) {
			return;
		}

		if(Dungeon.isChallenged(Challenges.NO_WEAPON)) {
			statue = new ArmoredStatue();
		}

		if(Dungeon.isChallenged(Challenges.NO_ARMOR)) {
			statue = new Statue();
		}

		if(statue == null) {
			if(Random.Float()>0.5) {
				statue = new Statue();
			} else {
				statue = new ArmoredStatue();
			}
		}

		statue.setPos(cx + cy * level.getWidth());
		level.mobs.add(statue);
		Actor.occupyCell(statue);
	}
}
