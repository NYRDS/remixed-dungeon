
package com.watabou.pixeldungeon.levels.painters;

import com.watabou.pixeldungeon.levels.Level;
import com.watabou.pixeldungeon.levels.Room;
import com.watabou.pixeldungeon.levels.Terrain;

public class ExitPainter extends Painter {

	protected static int counter;

	public static void resetCounter() {
		counter = 0;
	}

	public static void paint(Level level, Room room) {

		fill(level, room, Terrain.WALL);
		fill(level, room, 1, Terrain.EMPTY);

		for (Room.Door door : room.connected.values()) {
			door.set(Room.Door.Type.REGULAR);
		}

		int exitIndex = level.isBossLevel()?counter+1:counter;

		level.setExit(room.random(level, 1), exitIndex);

		int exitType = Terrain.EXIT;

		set(level, level.getExit(exitIndex), exitType);

		counter++;
	}

}
