package com.watabou.pixeldungeon.effects.particles;

import com.watabou.noosa.particles.Emitter;
import com.watabou.noosa.particles.Emitter.Factory;
import com.watabou.noosa.particles.PixelParticle;
import com.watabou.utils.ColorMath;
import com.watabou.utils.Random;

public class AlchemyParticle extends PixelParticle {

	public static final Emitter.Factory FACTORY = new Factory() {
		@Override
		public void emit( Emitter emitter, int index, float x, float y ) {
			((AlchemyParticle)emitter.recycle( AlchemyParticle.class )).reset( x, y );
		}

		@Override
		public boolean lightMode() {
			return true;
		}
	};

	public AlchemyParticle() {
		super();

		// Use colors that represent alchemy/magic - gold, green, blue
		color(ColorMath.random(0xAAAA00, 0xDDDD00)); // Gold/yellow range
		lifespan = 1.0f;

		acc.set(0, -20);
	}

	public void reset( float x, float y ) {
		revive();

		this.setX(x);
		this.setY(y);

		left = lifespan;

		// Random size for variety
		size = Random.Float(1.5f, 3.0f);

		// Random speed and direction with more dynamic movement
		speed.set(
			Random.Float(-20, 20),
			Random.Float(-10, 10)
		);
	}

	@Override
	public void update() {
		super.update();

		float p = left / lifespan;

		// Fade out as particle ages
		am = p < 0.5f ? p * 2 : (1 - p) * 2;

		// Slowly shrink as it ages
		if (left < lifespan * 0.8f) {
			size((size * left / lifespan) * 0.8f);
		}
	}
}