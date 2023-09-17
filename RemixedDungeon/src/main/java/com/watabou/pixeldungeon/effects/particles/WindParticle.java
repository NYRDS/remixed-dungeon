
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

public class WindParticle extends PixelParticle {

	public static final Emitter.Factory FACTORY = new Factory() {	
		@Override
		public void emit( Emitter emitter, int index, float x, float y ) {
			((WindParticle)emitter.recycle( WindParticle.class )).reset( x, y );
		}
	};
	
	private static float angle = Random.Float( PointF.PI * 2 );
	private static PointF speed = new PointF().polar( angle, 5 );
	
	private float size;
	
	public WindParticle() {
		lifespan = Random.Float( 1, 2 );
		setScale( size = Random.Float( 3 ) );
	}
	
	public void reset( float x, float y ) {
		revive();
		
		left = lifespan;
		
		super.speed.set( WindParticle.speed );
		super.speed.scale( size );
		
		this.setX(x - super.speed.x * lifespan / 2);
		this.setY(y - super.speed.y * lifespan / 2);
		
		angle += Random.Float( -0.1f, +0.1f );
		speed = new PointF().polar( angle, 5 );
		
		am = 0;
	}
	
	@Override
	public void update() {
		super.update();
		
		float p = left / lifespan;
		am = (p < 0.5f ? p : 1 - p) * size * 0.2f;
	}
	
	public static class Wind extends Group {
		
		private int pos;
		
		private float x;
		private float y;
		
		private float delay;
		
		public Wind( int pos ) {
			this.pos = pos;
			PointF p = DungeonTilemap.tileToWorld( pos );
			x = p.x;
			y = p.y;
			
			delay = Random.Float( 5 );
		}
		
		@Override
		public void update() {
			
			if (setVisible(Dungeon.isCellVisible(pos))) {
				
				super.update();
				
				if ((delay -= GameLoop.elapsed) <= 0) {
					
					delay = Random.Float( 5 );
					
					((WindParticle)recycle( WindParticle.class )).reset( 
						x + Random.Float( DungeonTilemap.SIZE ), 
						y + Random.Float( DungeonTilemap.SIZE ) );
				}
			}
		}
	}
}