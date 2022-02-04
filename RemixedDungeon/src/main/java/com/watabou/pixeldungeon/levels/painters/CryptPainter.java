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

import com.nyrds.pixeldungeon.items.Treasury;
import com.nyrds.pixeldungeon.levels.objects.LevelObjectsFactory;
import com.watabou.pixeldungeon.items.Heap.Type;
import com.watabou.pixeldungeon.items.Item;
import com.watabou.pixeldungeon.items.keys.IronKey;
import com.watabou.pixeldungeon.levels.Level;
import com.watabou.pixeldungeon.levels.Room;
import com.watabou.pixeldungeon.levels.Terrain;
import com.watabou.utils.Point;

public class CryptPainter extends Painter {

	public static void paint( Level level, Room room ) {

		fill( level, room, Terrain.WALL );
		fill( level, room, 1, Terrain.EMPTY );

		Point c = room.center();
		int cx = c.x;
		int cy = c.y;
		
		Room.Door entrance = room.entrance();
		
		entrance.set( Room.Door.Type.LOCKED );
		level.addItemToSpawn( new IronKey() );
		
		if (entrance.x == room.left) {
			level.putLevelObject(LevelObjectsFactory.STATUE, level.cell(room.right-1, room.top+1 ));
			level.putLevelObject(LevelObjectsFactory.STATUE, level.cell(room.right-1, room.bottom-1 ));
			cx = room.right - 2;
		} else if (entrance.x == room.right) {
			level.putLevelObject(LevelObjectsFactory.STATUE, level.cell(room.left+1, room.top+1 ));
			level.putLevelObject(LevelObjectsFactory.STATUE, level.cell(room.left+1, room.bottom-1 ));
			cx = room.left + 2;
		} else if (entrance.y == room.top) {
			level.putLevelObject(LevelObjectsFactory.STATUE, level.cell(room.left+1, room.bottom-1 ));
			level.putLevelObject(LevelObjectsFactory.STATUE, level.cell(room.right-1, room.bottom-1 ));
			cy = room.bottom - 2;
		} else if (entrance.y == room.bottom) {
			level.putLevelObject(LevelObjectsFactory.STATUE, level.cell(room.left+1, room.top+1 ));
			level.putLevelObject(LevelObjectsFactory.STATUE, level.cell(room.right-1, room.top+1));
			cy = room.top + 2;
		}
		
		level.drop( prize(level), cx + cy * level.getWidth(), Type.TOMB);
	}
	
	private static Item prize(Level level) {
		return Treasury.getLevelTreasury().bestOf( Treasury.Category.ARMOR,4 );
	}
}
