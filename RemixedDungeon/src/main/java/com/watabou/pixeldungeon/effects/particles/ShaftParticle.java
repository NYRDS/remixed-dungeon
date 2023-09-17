
package com.watabou.pixeldungeon.effects.particles;

import com.watabou.noosa.particles.Emitter;
import com.watabou.noosa.particles.Emitter.Factory;
import com.watabou.noosa.particles.PixelParticle;
import com.watabou.utils.Random;

public class ShaftParticle extends PixelParticle {
	
	public static final Emitter.Factory FACTORY = new Factory() {	
		@Override
		public void emit( Emitter emitter, int index, float x, float y ) {
			((ShaftParticle)emitter.recycle( ShaftParticle.class )).reset( x, y );
		}
		@Override
		public boolean lightMode() {
			return true;
		}
	};
	
	public ShaftParticle() {
		lifespan = 1.2f;
		speed.set( 0, -6 );
	}

	public void reset( float x, float y ) {
		revive();
		
		this.setX(x);
		this.setY(y);

		float offs = -Random.Float(lifespan);
		left = lifespan - offs;
	}
	
	@Override
	public void update() {
		super.update();
		
		float p = left / lifespan;
		am = p < 0.5f ? p : 1 - p;
		setScaleXY( (1 - p) * 4, 16 + (1 - p) * 16);
	}
}