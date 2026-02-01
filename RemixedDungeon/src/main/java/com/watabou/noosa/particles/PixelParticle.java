

package com.watabou.noosa.particles;

import com.nyrds.pixeldungeon.game.GameLoop;
import com.watabou.noosa.PseudoPixel;

public class PixelParticle extends PseudoPixel {

	protected float size;
	
	protected float lifespan;
	protected float left;
	
	public PixelParticle() {
		super();
		setOrigin( +0.5f );
	}
	
	public void reset( float x, float y, int color, float size, float lifespan ) {
		revive();
		
		this.setX(x);
		this.setY(y);

		color( color );
		size( this.size = size );
			
		this.left = this.lifespan = lifespan;
	}
	
	@Override
	public void update() {
		super.update();

		if ((left -= GameLoop.elapsed) <= 0) {
			kill();
		}
	}
	
	public static class Shrinking extends PixelParticle {
		@Override
		public void update() {
			super.update();
			size( size * left / lifespan );
		}
	}
}
