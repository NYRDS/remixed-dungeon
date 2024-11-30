package com.watabou.pixeldungeon.effects;

import com.nyrds.platform.gfx.BitmapData;
import com.watabou.gltextures.SmartTexture;
import com.watabou.gltextures.TextureCache;
import com.watabou.noosa.Image;

public class CircleMask extends Image {

	private static final Object CACHE_KEY = CircleMask.class;

	protected static final int RADIUS	= 64;

	protected float radius = RADIUS;
	protected float brightness = 1;

	public static void ensureTexture() {
		if (!TextureCache.contains( CACHE_KEY )) {

			BitmapData bmp = BitmapData.createBitmap( RADIUS * 2, RADIUS * 2 );
			bmp.makeCircleMask(RADIUS, 0x00ffffff, 0x77ffffff, 0xf7ffffff);
			TextureCache.add( CACHE_KEY, new SmartTexture( bmp ) );
		}
	}

	public CircleMask() {
		ensureTexture();

		texture( CACHE_KEY );

		origin.set( RADIUS );
	}

	public CircleMask(float radius) {

		this();
		radius( radius );
	}

	public CircleMask point(float x, float y ) {
		this.x = x - RADIUS;
		this.y = y - RADIUS;
		return this;
	}

	public void radius( float value ) {
		scale.set(  (this.radius = value) / RADIUS );
	}
}
