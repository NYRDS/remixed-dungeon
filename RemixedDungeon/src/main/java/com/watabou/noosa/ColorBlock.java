

package com.watabou.noosa;

import com.watabou.gltextures.TextureCache;

public class ColorBlock extends Image {
	
	public ColorBlock( float width, float height, int color ) {
		super( TextureCache.createSolid( color ) );
		setScaleXY( width, height );
		setOrigin( 0, 0 );
	}

	public void size( float width, float height ) {
		setScaleXY( width, height );
	}
	
	public float width() {
		return scale.x;
	}
	
	public float height() {
		return scale.y;
	}
}
