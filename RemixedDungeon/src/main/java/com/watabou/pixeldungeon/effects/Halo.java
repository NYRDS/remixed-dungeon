
package com.watabou.pixeldungeon.effects;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;

import com.watabou.gltextures.SmartTexture;
import com.watabou.gltextures.TextureCache;
import com.watabou.noosa.Image;

public class Halo extends Image {
	
	private static final Object CACHE_KEY = Halo.class;
	
	protected static final int RADIUS	= 64;
	
	protected float radius = RADIUS;
	protected float brightness = 1;

	public Halo() {
		if (!TextureCache.contains( CACHE_KEY )) {
			Bitmap bmp = Bitmap.createBitmap( RADIUS * 2, RADIUS * 2, Bitmap.Config.ARGB_8888 );
			Canvas canvas = new Canvas( bmp );
			Paint paint = new Paint();
			paint.setColor( 0xFFFFFFFF );
			canvas.drawCircle( RADIUS, RADIUS, RADIUS * 0.75f, paint );
			paint.setColor( 0x88FFFFFF );
			canvas.drawCircle( RADIUS, RADIUS, RADIUS, paint );
			TextureCache.add( CACHE_KEY, new SmartTexture( bmp ) );
		}
		
		texture( CACHE_KEY );
		
		setOrigin( RADIUS );
	}
	
	public Halo( float radius, int color, float brightness ) {
		
		this();
		
		hardlight( color );
		alpha( this.brightness = brightness );
		radius( radius );
	}
	
	public Halo point( float x, float y ) {
		this.setX(x - RADIUS);
		this.setY(y - RADIUS);
		return this;
	}
	
	public void radius( float value ) {
		setScale(  (this.radius = value) / RADIUS );
	}
}
