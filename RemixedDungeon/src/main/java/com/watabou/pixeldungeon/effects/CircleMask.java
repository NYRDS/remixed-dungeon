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
			/*
			Canvas canvas = new Canvas( bmp );
			Paint paint = new Paint();
			canvas.drawColor(Color.WHITE, PorterDuff.Mode.SRC);

			paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC));
			paint.setColor( 0xf7ffffff);
			canvas.drawCircle( RADIUS, RADIUS, RADIUS, paint );

			paint.setColor( 0x77ffffff);
			canvas.drawCircle( RADIUS, RADIUS, RADIUS*0.75f, paint );

			paint.setColor( 0x00ffffff);
			canvas.drawCircle( RADIUS, RADIUS, RADIUS*0.5f, paint );
			 */
			TextureCache.add( CACHE_KEY, new SmartTexture( bmp ) );
		}
	}

	public CircleMask() {
		ensureTexture();

		texture( CACHE_KEY );

		setOrigin( RADIUS );
	}

	public CircleMask(float radius) {
		
		this();
		radius( radius );
	}
	
	public CircleMask point(float x, float y ) {
		this.setX(x - RADIUS);
		this.setY(y - RADIUS);
		return this;
	}
	
	public void radius( float value ) {
		setScale(  (this.radius = value) / RADIUS );
	}
}
