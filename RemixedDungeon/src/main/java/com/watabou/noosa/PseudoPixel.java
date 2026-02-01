

package com.watabou.noosa;

import com.watabou.gltextures.TextureCache;

public class PseudoPixel extends Image {
	
	public PseudoPixel() {
		super( TextureCache.createSolid( 0xFFFFFFFF ) );
	}
	
	public PseudoPixel( float x, float y, int color ) {

		this();
		
		this.setX(x);
		this.setY(y);
		color( color );
	}
	
	public void size( float w, float h ) {
		setScaleXY( w, h );
	}
	
	public void size( float value ) {
		setScale( value );
	}
}
