
package com.watabou.pixeldungeon.effects.particles;

import com.watabou.noosa.particles.Emitter;
import com.watabou.noosa.particles.Emitter.Factory;
import com.watabou.noosa.particles.PixelParticle;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.utils.ColorMath;
import com.watabou.utils.Random;

public class LeafParticle extends PixelParticle.Shrinking {
	
	public static int color1;
	public static int color2;
	
	
	public static final Emitter.Factory GENERAL = new Factory() {	
		@Override
		public void emit( Emitter emitter, int index, float x, float y ) {
			LeafParticle p = ((LeafParticle)emitter.recycle( LeafParticle.class ));
			p.color( ColorMath.random( 0x004400, 0x88CC44 ) );
			p.reset( x, y );
		}
	};
	
	public static final Emitter.Factory LEVEL_SPECIFIC = new Factory() {	
		@Override
		public void emit( Emitter emitter, int index, float x, float y ) {
			LeafParticle p = ((LeafParticle)emitter.recycle( LeafParticle.class ));
			p.color( ColorMath.random( Dungeon.level.color1, Dungeon.level.color2 ) );
			p.reset( x, y );
		}
	};
	
	public LeafParticle() {
		lifespan = 1.2f;
		acc.set( 0, 25 );
	}
	
	public void reset( float x, float y ) {
		revive();
		
		this.setX(x);
		this.setY(y);
		
		speed.set( Random.Float( -8, +8 ), -20 );
		
		left = lifespan;
		size = Random.Float( 2, 3 );
	}
}