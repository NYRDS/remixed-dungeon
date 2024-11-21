
package com.watabou.pixeldungeon.levels;

import com.nyrds.util.Random;

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
