
package com.watabou.pixeldungeon.effects.particles;

import com.watabou.noosa.particles.Emitter;
import com.watabou.noosa.particles.Emitter.Factory;
import com.watabou.noosa.particles.PixelParticle;
import com.watabou.utils.Random;

public class WebParticle extends PixelParticle {
	
	public static final Emitter.Factory FACTORY = new Factory() {	
		@Override
		public void emit( Emitter emitter, int index, float x, float y ) {
			for (int i=0; i < 3; i++) {
				((WebParticle)emitter.recycle( WebParticle.class )).reset( x, y );
			}
		}
	};
	
	public WebParticle() {
		color( 0xCCCCCC );
		lifespan = 2f;
	}
	
	public void reset( float x, float y ) {
		revive();
		
		this.setX(x);
		this.setY(y);
		
		left = lifespan;
		setAngle(Random.Float( 360 ));
	}
	
	@Override
	public void update() {
		super.update();
		
		float p = left / lifespan;
		am = p < 0.5f ? p : 1 - p;
		setScaleY( 16 + p * 8 );
	}
}