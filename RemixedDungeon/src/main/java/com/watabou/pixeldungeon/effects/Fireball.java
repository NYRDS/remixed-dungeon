
package com.watabou.pixeldungeon.effects;

import android.opengl.GLES20;

import com.nyrds.pixeldungeon.game.GameLoop;
import com.nyrds.platform.compatibility.RectF;
import com.nyrds.platform.gl.Texture;
import com.watabou.noosa.Group;
import com.watabou.noosa.Image;
import com.watabou.noosa.particles.Emitter;
import com.watabou.noosa.particles.PixelParticle;
import com.watabou.noosa.ui.Component;
import com.watabou.pixeldungeon.Assets;
import com.nyrds.util.ColorMath;
import com.nyrds.util.Random;

import javax.microedition.khronos.opengles.GL10;

public class Fireball extends Component {

	private static final RectF BLIGHT = new RectF( 0, 0, 0.25f, 1 );
	private static final RectF FLIGHT = new RectF( 0.25f, 0, 0.5f, 1 );
	private static final RectF FLAME1 = new RectF( 0.50f, 0, 0.75f, 1 );
	private static final RectF FLAME2 = new RectF( 0.75f, 0, 1.00f, 1 );
	
	private static final int COLOR = 0xFF66FF;
	
	private Image bLight;
	private Image fLight;
	private Emitter emitter;
	private Group sparks;

	@Override
	protected void createChildren() {
		
		sparks = new Group();
		add( sparks );
		
		bLight = new Image( Assets.FIREBALL );
		bLight.frame( BLIGHT );
        bLight.setOrigin( bLight.width / 2 );
		bLight.angularSpeed = -90;
		add( bLight );
		
		emitter = new Emitter();
		emitter.pour( new Emitter.Factory() {
			@Override
			public void emit(Emitter emitter, int index, float x, float y) {
				Flame p = (Flame)emitter.recycle( Flame.class );
				p.reset();
                p.setX(x - p.width / 2);
				p.setY(y - p.height / 2);
			}
		}, 0.1f );
		add( emitter );
		
		fLight = new Image( Assets.FIREBALL );
		fLight.frame( FLIGHT );
        fLight.setOrigin( fLight.width / 2 );
		fLight.angularSpeed = 360;
		add( fLight );
		
		bLight.texture.filter( Texture.LINEAR, Texture.LINEAR );
	}
	
	@Override
	protected void layout() {

        bLight.setX(x - bLight.width / 2);
		bLight.setY(y - bLight.height / 2);

		emitter.pos(
			x - bLight.width / 4,
			y - bLight.height / 4,
			bLight.width / 2,
			bLight.height / 2 );

        fLight.setX(x - fLight.width / 2);
		fLight.setY(y - fLight.height / 2);
	}
	
	@Override
	public void update() {
		
		super.update();
		
		if (Random.Float() < GameLoop.elapsed) {
			PixelParticle spark = (PixelParticle)sparks.recycle( PixelParticle.Shrinking.class );
			spark.reset( x, y, ColorMath.random( COLOR, 0x66FF66 ), 2, Random.Float( 0.5f, 1.0f ) );
			spark.speed.set( 
				Random.Float( -40, +40 ), 
				Random.Float( -60, +20 ) );
			spark.acc.set( 0, +80 );
			sparks.add( spark );
		}
	}
	
	@Override
	public void draw() {
		GLES20.glBlendFunc( GL10.GL_SRC_ALPHA, GL10.GL_ONE );
		super.draw();
		GLES20.glBlendFunc( GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA );
	}
	
	public static class Flame extends Image {
		
		private static final float LIFESPAN	= 1f;
		
		private static final float SPEED	= -40f;
		private static final float ACC	= -20f;
		
		private float timeLeft;
		
		public Flame() {
			
			super( Assets.FIREBALL );
			
			frame( Random.Int( 2 ) == 0 ? FLAME1 : FLAME2 );
			setOrigin( width / 2, height / 2 );
			acc.set( 0, ACC );
		}
		
		public void reset() {
			revive();
			timeLeft = LIFESPAN;
			speed.set( 0, SPEED );
		}
		
		@Override
		public void update() {
			
			super.update();
			
			if ((timeLeft -= GameLoop.elapsed) <= 0) {
				
				kill();
				
			} else {
				
				float p = timeLeft / LIFESPAN;
				setScale( p );
				alpha( p > 0.8f ? (1 - p) * 5f : p * 1.25f );
				
			}
		}
	}
}
