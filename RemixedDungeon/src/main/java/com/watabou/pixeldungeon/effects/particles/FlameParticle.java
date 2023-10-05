
package com.watabou.pixeldungeon.effects.particles;

import com.watabou.noosa.particles.Emitter;
import com.watabou.noosa.particles.Emitter.Factory;
import com.watabou.noosa.particles.PixelParticle;

public class FlameParticle extends PixelParticle.Shrinking {
	
	public static final Emitter.Factory FACTORY = new Factory() {	
		@Override
		public void emit( Emitter emitter, int index, float x, float y ) {
			((FlameParticle)emitter.recycle( FlameParticle.class )).reset( x, y );
		}
		@Override
		public boolean lightMode() {
			return true;
		}
	};
	
	public FlameParticle() {
		color( 0xEE7722 );
		lifespan = 0.6f;
		
		acc.set( 0, -80 );
	}
	
	public void reset( float x, float y ) {
		revive();
		
		this.setX(x);
		this.setY(y);
		
		left = lifespan;
		
		size = 4;
		speed.set( 0 );
	}
	
	@Override
	public void update() {
		super.update();
		float p = left / lifespan;
		am = p > 0.8f ? (1 - p) * 5 : 1;
	}
}