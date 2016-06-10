/*
 * Pixel Dungeon
 * Copyright (C) 2012-2014  Oleg Dolya
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */
package com.watabou.pixeldungeon.levels.painters;

import com.watabou.pixeldungeon.levels.Level;
import com.watabou.pixeldungeon.levels.Room;
import com.watabou.pixeldungeon.levels.Terrain;

public class ExitPainter extends Painter {

	private static int counter;

	public static void resetCounter() {
		counter = 0;
	}

	public static void paint(Level level, Room room) {

		fill(level, room, Terrain.WALL);
		fill(level, room, 1, Terrain.EMPTY);

		for (Room.Door door : room.connected.values()) {
			door.set(Room.Door.Type.REGULAR);
		}

		level.setExit(room.random(level, 1), counter);
		set(level, level.getExit(counter), Terrain.EXIT);

		counter++;
		/*
		int exitCount = DungeonGenerator.exitCount(level.levelId);

		for (int index = 1; index < exitCount; ++index) {
			int exitCandidate = -1;
			do {
				exitCandidate = room.random(level, 0);
			} while (level.map[exitCandidate] == Terrain.EXIT ||
					level.map[exitCandidate] == Terrain.LOCKED_EXIT ||
					level.map[exitCandidate] == Terrain.UNLOCKED_EXIT);

			level.setExit(exitCandidate, index);
			set(level, exitCandidate, Terrain.EXIT);
		}
		*/
	}

}
