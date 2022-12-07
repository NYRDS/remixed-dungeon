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

import com.nyrds.retrodungeon.levels.objects.Barrel;
import com.watabou.pixeldungeon.items.Generator;
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
					Item prize = Random.oneOf(Generator.random(Generator.Category.BULLETS),
							Generator.random(Generator.Category.THROWABLE));
					level.drop(prize, level.cell(i,j));
				}
			}
		}
	}
	
}
