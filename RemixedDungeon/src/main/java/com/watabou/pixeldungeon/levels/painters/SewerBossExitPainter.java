
package com.watabou.pixeldungeon.levels.painters;

import com.watabou.pixeldungeon.levels.Level;
import com.watabou.pixeldungeon.levels.Room;
import com.watabou.pixeldungeon.levels.Terrain;

public class SewerBossExitPainter extends ExitPainter {

	public static void paint(Level level, Room room) {

		fill(level, room, 1, Terrain.EMPTY);

		for (Room.Door door : room.connected.values()) {
			door.set(Room.Door.Type.REGULAR);
		}

		int centerX = (room.left + room.right) / 2;
		int centerY = (room.top  + room.bottom) / 2;

		set(level,centerX-1,centerY,Terrain.WALL_DECO);
		set(level,centerX+1,centerY,Terrain.WALL_DECO);

		set(level,centerX-1, centerY-1,Terrain.WALL);
		set(level,centerX,   centerY-1,Terrain.WALL);
		set(level,centerX+1, centerY-1,Terrain.WALL);

		set(level,centerX-1, centerY+1,Terrain.WATER);
		set(level,centerX,   centerY+1,Terrain.WATER);
		set(level,centerX+1, centerY+1,Terrain.WATER);

		level.setExit(level.cell(centerX,centerY), 0);
		set(level, level.getExit(0), Terrain.LOCKED_EXIT);
	}
}
