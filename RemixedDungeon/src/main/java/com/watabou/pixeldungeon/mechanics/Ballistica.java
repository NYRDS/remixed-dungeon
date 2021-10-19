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
package com.watabou.pixeldungeon.mechanics;

import com.nyrds.LuaInterface;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.Actor;
import com.watabou.pixeldungeon.levels.Level;

import lombok.val;

public class Ballistica {

	public static int[] trace = new int[32];
	public static int distance;

	public static int cast( int from, int to, boolean magic, boolean hitChars) {
		return cast(from, to, magic, hitChars, false);
	}

	@LuaInterface
	public static int getBacktraceCell(int distFromEnd) {
		distFromEnd = Math.min(distFromEnd, distance - 1);
		return trace[distance - 1 - distFromEnd];
	}

	@LuaInterface
	public static int cast( int from, int to, boolean magic, boolean hitChars, boolean hitObjects ) {

		Level level = Dungeon.level;
		int w = level.getWidth();
		int h = level.getHeight();

		int lSize = (int) Math.ceil(Math.sqrt( w*w + h*h));

		if(trace.length < lSize) {
			trace = new int[lSize]; 
		}

		int x0 = from % w;
		int x1 = to % w;
		int y0 = from / w;
		int y1 = to / w;
		
		int dx = x1 - x0;
		int dy = y1 - y0;
		
		int stepX = dx > 0 ? +1 : -1;
		int stepY = dy > 0 ? +1 : -1;
		
		dx = Math.abs( dx );
		dy = Math.abs( dy );
		
		int stepA;
		int stepB;
		int dA;
		int dB;
		
		if (dx > dy) {
			
			stepA = stepX;
			stepB = stepY * w;
			dA = dx;
			dB = dy;

		} else {
			
			stepA = stepY * w;
			stepB = stepX;
			dA = dy;
			dB = dx;

		}

		distance = 1;
		trace[0] = from;
		
		int cell = from;
		
		int err = dA / 2;
		while (cell != to || magic) {
			
			cell += stepA;
			
			err += dB;
			if (err >= dA) {
				err = err - dA;
				cell = cell + stepB;
			}
			
			trace[distance++] = cell;
			
			if (!level.passable[cell] && !level.avoid[cell]) {
				return trace[--distance - 1];
			}



			if (level.losBlocking[cell]
					|| (hitChars && Actor.findChar( cell ) != null)
					|| (hitObjects && level.getLevelObject(cell) != null)) {
				return cell;
			}

			val obj = level.getTopLevelObject(cell);
			if(obj != null && obj.losBlocker()) {
				return cell;
			}
		}
		
		trace[distance++] = cell;
		
		return to;
	}
}
