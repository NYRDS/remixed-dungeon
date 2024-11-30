package com.watabou.gltextures;

import com.nyrds.platform.gfx.BitmapData;

public class Gradient extends SmartTexture {

	public Gradient( int[] colors ) {

		super( BitmapData.createBitmap( colors.length, 1) );

		for (int i=0; i < colors.length; i++) {
			bitmap.setPixel( i, 0, colors[i] );
		}
		bitmap( bitmap );

		filter( LINEAR, LINEAR );
		wrap( CLAMP, CLAMP );
	}
}