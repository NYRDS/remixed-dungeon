
package com.watabou.pixeldungeon.effects.particles;

import com.nyrds.pixeldungeon.game.GameLoop;
import com.watabou.noosa.Group;
import com.watabou.noosa.particles.Emitter;
import com.watabou.noosa.particles.Emitter.Factory;
import com.watabou.noosa.particles.PixelParticle;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.DungeonTilemap;
import com.watabou.utils.PointF;
import com.watabou.utils.Random;

public class FlowParticle extends PixelParticle {

	public static final Emitter.Factory FACTORY = new Factory() {	
		@Override
		public void emit( Emitter emitter, int index, float x, float y ) {
			((FlowParticle)emitter.recycle( FlowParticle.class )).reset( x, y );
		}
	};
	
	public FlowParticle() {
		lifespan = 0.6f;
		acc.set( 0, 32 );
		angularSpeed = Random.Float( -360, +360 );
	}
	
	public void reset( float x, float y ) {
		revive();
		
		left = lifespan;
		
		this.setX(x);
		this.setY(y);
		
		am = 0;
		size( 0 );
		speed.set( 0 );
	}
	
	@Override
	public void update() {
		super.update();
		
		float p = left / lifespan;
		am = (p < 0.5f ? p : 1 - p) * 0.6f;
		size( (1 - p) * 4 );
	}
	
	public static class Flow extends Group {
		
		private static final float DELAY	= 0.1f;
		
		private final int pos;
		
		private final float x;
		private final float y;
		
		private float delay;
		
		public Flow( int pos ) {
			this.pos = pos;
			
			PointF p = DungeonTilemap.tileToWorld( pos );
			x = p.x;
			y = p.y + DungeonTilemap.SIZE - 1;
			
			delay = Random.Float( DELAY );
		}
		
		@Override
		public void update() {
			
			if (setVisible(Dungeon.isCellVisible(pos))) {
				
				super.update();
				
				if ((delay -= GameLoop.elapsed) <= 0) {
					
					delay = Random.Float( DELAY );
					
					((FlowParticle)recycle( FlowParticle.class )).reset( 
						x + Random.Float( DungeonTilemap.SIZE ), y );
				}
			}
		}
	}
}