
package com.watabou.pixeldungeon.effects.particles;

import com.watabou.noosa.particles.Emitter;
import com.watabou.noosa.particles.Emitter.Factory;
import com.watabou.noosa.particles.PixelParticle;
import com.watabou.utils.ColorMath;
import com.watabou.utils.Random;

public class EarthParticle extends PixelParticle {	
	
	public static final Emitter.Factory FACTORY = new Factory() {	
		@Override
		public void emit( Emitter emitter, int index, float x, float y ) {
			((EarthParticle)emitter.recycle( EarthParticle.class )).reset( x,  y );
		}
	};
	
	public EarthParticle() {
		color( ColorMath.random( 0x444444, 0x777766 ) );
		setAngle(Random.Float( -30, 30 ));
		
		lifespan = 0.5f;
	}
	
	public void reset( float x, float y ) {
		revive();
		
		this.setX(x);
		this.setY(y);

		left = lifespan;
	}
	
	@Override
	public void update() {
		super.update();
		
		float p = left / lifespan;
		size( (p < 0.5f ? p : 1 - p) * 16 );
	}
}