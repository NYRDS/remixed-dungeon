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
package com.watabou.pixeldungeon.levels;

import com.watabou.utils.Random;

public class Patch {
	
	private static boolean[] cur = new boolean[32];
	private static boolean[] off = new boolean[32];
	
	public static boolean[] generate(Level level, float seed, int nGen ) {
		
		int w = level.getWidth();
		int h = level.getHeight();
		
		int len = level.getLength();
		
		if(cur.length < len) {
			cur = new boolean[len];
			off = new boolean[len];
		}
		
		for (int i=0; i < len; i++) {
			off[i] = Random.Float() < seed;
		}
		
		for (int i=0; i < nGen; i++) {
			
			for (int y=1; y < h-1; y++) {
				for (int x=1; x < w-1; x++) {
					
					int pos = x + y * w;
					int count = 0;
					if (off[pos-w-1]) {
						count++;
					}
					if (off[pos-w]) {
						count++;
					}
					if (off[pos-w+1]) {
						count++;
					}
					if (off[pos-1]) {
						count++;
					}
					if (off[pos+1]) {
						count++;
					}
					if (off[pos+w-1]) {
						count++;
					}
					if (off[pos+w]) {
						count++;
					}
					if (off[pos+w+1]) {
						count++;
					}
					
					if (!off[pos] && count >= 5) {
						cur[pos] = true;
					} else cur[pos] = off[pos] && count >= 4;
				}
			}
			
			boolean[] tmp = cur;
			cur = off;
			off = tmp;
		}
		
		return off;
	}
}
