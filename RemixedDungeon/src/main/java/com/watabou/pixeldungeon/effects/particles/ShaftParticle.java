
package com.watabou.pixeldungeon.effects.particles;

import com.nyrds.LuaInterface;
import com.watabou.noosa.particles.Emitter;
import com.watabou.noosa.particles.Emitter.Factory;
import com.watabou.noosa.particles.PixelParticle;
import com.watabou.utils.Random;

@LuaInterface
public class ShaftParticle extends PixelParticle {
	
	// the white factory pins the color on every emit: emitters recycle pooled
	// instances, an unpinned color would leak a gold tint from a GOLD_FACTORY
	// emitter sharing the same ShaftParticle instances
	public static final Emitter.Factory FACTORY = new Factory() {
		@Override
		public void emit( Emitter emitter, int index, float x, float y ) {
			ShaftParticle particle = (ShaftParticle)emitter.recycle( ShaftParticle.class );
			particle.reset( x, y );
			particle.color( 0xFFFFFF );
		}
		@Override
		public boolean lightMode() {
			return true;
		}
	};

	// gold of the priest spell-icon family (spellsIcons/priest.png)
	public static final int GOLD = 0xFFE200;

	public static final Emitter.Factory GOLD_FACTORY = new Factory() {
		@Override
		public void emit( Emitter emitter, int index, float x, float y ) {
			ShaftParticle particle = (ShaftParticle)emitter.recycle( ShaftParticle.class );
			particle.reset( x, y );
			particle.color( GOLD );
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