
package com.watabou.pixeldungeon.effects.particles;

import com.watabou.noosa.particles.Emitter;
import com.watabou.noosa.particles.Emitter.Factory;
import com.watabou.noosa.particles.PixelParticle;
import com.watabou.utils.ColorMath;
import com.watabou.utils.Random;

public class WoolParticle extends PixelParticle.Shrinking {
	
	public static final Emitter.Factory FACTORY = new Factory() {	
		@Override
		public void emit( Emitter emitter, int index, float x, float y ) {
			((WoolParticle)emitter.recycle( WoolParticle.class )).reset( x, y );
		}
	};
	
	public WoolParticle() {
		color( ColorMath.random( 0x999999, 0xEEEEE0 ) );
		
		acc.set( 0, -40 );
	}
	
	public void reset( float x, float y ) {
		revive();
		
		this.setX(x);
		this.setY(y);
		
		left = lifespan = Random.Float( 0.6f, 1f );
		size = 5;
		
		speed.set( Random.Float( -10, +10 ), Random.Float( -10, +10 ) );
	}
}