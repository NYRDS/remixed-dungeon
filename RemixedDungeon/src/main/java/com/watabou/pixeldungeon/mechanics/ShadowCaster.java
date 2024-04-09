
package com.watabou.pixeldungeon.mechanics;

import com.nyrds.Packable;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.levels.Level;

import java.util.Arrays;

@Packable
public final class ShadowCaster {

	public static final int MAX_DISTANCE = 8;
	
	private static int distance;
	private static int[] limits;
	
	private static boolean[] losBlocking;
	private static boolean[] fieldOfView;
	
	private static final int[][] rounding;
	static {
		rounding = new int[MAX_DISTANCE+1][];
		for (int i=1; i <= MAX_DISTANCE; i++) {
			rounding[i] = new int[i+1];
			for (int j=1; j <= i; j++) {
				rounding[i][j] = (int)Math.min( j, Math.round( i * Math.cos( Math.asin( j / (i + 0.5) ))));
			}
		}
	}
	
	private static final Obstacles obs = new Obstacles();
	
	public static void castShadow( int x, int y, boolean[] fieldOfView, int distance ) {

		final Level level = Dungeon.level;

		losBlocking = level.losBlocking;
		
		distance = ShadowCaster.distance = Math.min(distance, MAX_DISTANCE);
		limits = rounding[distance];
		
		ShadowCaster.fieldOfView = fieldOfView;
		Arrays.fill( fieldOfView, false );
		fieldOfView[y * level.getWidth() + x] = true;
		
		scanSector( x, y, +1, +1, 0, 0 );
		scanSector( x, y, -1, +1, 0, 0 );
		scanSector( x, y, +1, -1, 0, 0 );
		scanSector( x, y, -1, -1, 0, 0 );
		scanSector( x, y, 0, 0, +1, +1 );
		scanSector( x, y, 0, 0, -1, +1 );
		scanSector( x, y, 0, 0, +1, -1 );
		scanSector( x, y, 0, 0, -1, -1 );
	}
	
	private static void scanSector( int cx, int cy, int m1, int m2, int m3, int m4 ) {
		
		obs.reset();
		int w = Dungeon.level.getWidth();
		int h = Dungeon.level.getHeight();

		for (int p=1; p <= distance; p++) {

			float dq2 = 0.5f / p;
			
			int pp = limits[p];
			for (int q=0; q <= pp; q++) {
				
				int x = cx + q * m1 + p * m3;
				int y = cy + p * m2 + q * m4;
				
				if (y >= 0 && y < h && x >= 0 && x < w) {
					
					float a0 = (float)q / p;
					float a1 = a0 - dq2;
					float a2 = a0 + dq2;
					
					int pos = y * w + x;

					if (!obs.isBlocked(a0) || !obs.isBlocked(a1) || !obs.isBlocked(a2)) {
						fieldOfView[pos] = true;
					}

					if (losBlocking[pos]) {
						obs.add( a1, a2 );
					}

				}
			}
			
			obs.nextRow();
		}
	}
	
	private static final class Obstacles {
		
		private static final int SIZE = (MAX_DISTANCE+1) * (MAX_DISTANCE+1) / 2;
		private static final float[] a1 = new float[SIZE];
		private static final float[] a2 = new float[SIZE];
		
		private int length;
		private int limit;
		
		public void reset() {
			length = 0;
			limit = 0;
		}
		
		public void add( float o1, float o2 ) {
			
			if (length > limit && o1 <= a2[length-1]) {
				a2[length-1] = o2;
			} else {
				a1[length] = o1;
				a2[length++] = o2;
			}
		}
		
		public boolean isBlocked( float a ) {
			for (int i=0; i < limit; i++) {
				if (a >= a1[i] && a <= a2[i]) {
					return true;
				}
			}
			return false;
		}
		
		public void nextRow() {
			limit = length;
		}
	}
}
