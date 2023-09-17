
package com.watabou.pixeldungeon.effects.particles;

import com.watabou.noosa.particles.Emitter;
import com.watabou.noosa.particles.Emitter.Factory;
import com.watabou.noosa.particles.PixelParticle;
import com.watabou.utils.Random;

public class SparkParticle extends PixelParticle {

	public static final Emitter.Factory FACTORY = new Factory() {	
		@Override
		public void emit( Emitter emitter, int index, float x, float y ) {
			((SparkParticle)emitter.recycle( SparkParticle.class )).reset( x, y );
		}
		@Override
		public boolean lightMode() {
			return true;
		}
	};
	
	public SparkParticle() {
		size( 2 );
		
		acc.set( 0, +50 );
	}
	
	public void reset( float x, float y ) {
		revive();
		
		this.setX(x);
		this.setY(y);
		
		left = lifespan = Random.Float( 0.5f, 1.0f );
		
		speed.polar( Random.Float( 3.1415926f ), Random.Float( 20, 40 ) );
	}
	
	@Override
	public void update() {
		super.update();
		size( Random.Float( 5 * left / lifespan ) );
	}
}