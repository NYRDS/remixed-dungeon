package com.watabou.gltextures;

public class Gradient extends SmartTexture {

	public Gradient( int[] colors ) {
		super();
		filter( LINEAR, LINEAR );
		wrap( CLAMP, CLAMP );

		pixels(colors.length,1, colors);
	}
}