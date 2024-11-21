package com.nyrds.util;

public class ColorMath {
	
	public static int interpolate( int A, int B, float p ) {
		
		if (p <= 0) {
			return A;
		} else if (p >= 1) {
			return B;
		}
		
		int ra = A >> 16;
		int ga = (A >> 8) & 0xFF;
		int ba = A & 0xFF;
		
		int rb = B >> 16;
		int gb = (B >> 8) & 0xFF;
		int bb = B & 0xFF;
		
		float p1 = 1 - p;
		
		int r = (int)(p1 * ra + p * rb);
		int g = (int)(p1 * ga + p * gb);
		int b = (int)(p1 * ba + p * bb);
		
		return (r << 16) + (g << 8) + b;
	}
	
	public static int interpolate( float p,  int... colors ) {
		if (p <= 0) {
			return colors[0];
		} else if (p >= 1) {
			return colors[colors.length-1];
		}
		int segment = (int)(colors.length * p);
		return interpolate( colors[segment], colors[segment+1], (p * (colors.length - 1)) % 1 );
	}
	
	public static int random( int a, int b ) {
		return interpolate( a, b, Random.Float() );
	}

}
