
package com.watabou.pixeldungeon.effects.particles;

import com.watabou.noosa.particles.Emitter;
import com.watabou.noosa.particles.Emitter.Factory;
import com.watabou.noosa.particles.PixelParticle;
import com.watabou.utils.PointF;
import com.watabou.utils.Random;

public class EnergyParticle extends PixelParticle {
	
	public static final Emitter.Factory FACTORY = new Factory() {	
		@Override
		public void emit( Emitter emitter, int index, float x, float y ) {
			((EnergyParticle)emitter.recycle( EnergyParticle.class )).reset( x, y );
		}
		@Override
		public boolean lightMode() {
			return true;
		}
	};
	
	public EnergyParticle() {
		lifespan = 1f;
		color( 0xFFFFAA );
		
		speed.polar( Random.Float( 2 * PointF.PI ), Random.Float( 24, 32 ) );
	}
	
	public void reset( float x, float y ) {
		revive();
		
		left = lifespan;
		
		this.setX(x - speed.x * lifespan);
		this.setY(y - speed.y * lifespan);
	}
	
	@Override
	public void update() {
		super.update();
		
		float p = left / lifespan;
		am = p < 0.5f ? p * p * 4 : (1 - p) * 2; 
		size( Random.Float( 5 * left / lifespan ) );
	}
}