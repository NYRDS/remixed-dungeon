
package com.watabou.pixeldungeon.effects;

import android.opengl.GLES20;

import com.nyrds.pixeldungeon.game.GameLoop;
import com.nyrds.platform.audio.Sample;
import com.watabou.noosa.Image;
import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.DungeonTilemap;
import com.watabou.utils.PointF;

import javax.microedition.khronos.opengles.GL10;

public class DeathRay extends Image {

	private static final double A = 180 / Math.PI;
	
	private static final float DURATION	= 0.5f;
	
	private float timeLeft;

	public DeathRay(int from, int to) {
		super( Effects.get( Effects.Type.RAY ) );

		PointF s = DungeonTilemap.tileCenterToWorld(from);
		PointF e = DungeonTilemap.tileCenterToWorld(to);

		setOrigin( 0, height / 2 );

		setX(s.x - origin.x);
		setY(s.y - origin.y);
		
		float dx = e.x - s.x;
		float dy = e.y - s.y;
		setAngle((float)(Math.atan2( dy, dx ) * A));
        setScaleX((float)Math.sqrt( dx * dx + dy * dy ) / width);
		
		Sample.INSTANCE.play( Assets.SND_RAY );
		
		timeLeft = DURATION;
	}
	
	@Override
	public void update() {
		super.update();
		
		float p = timeLeft / DURATION;
		alpha( p );
		setScaleY( p );
		
		if ((timeLeft -= GameLoop.elapsed) <= 0) {
			killAndErase();
		}
	}
	
	@Override
	public void draw() {
		GLES20.glBlendFunc( GL10.GL_SRC_ALPHA, GL10.GL_ONE );
		super.draw();
		GLES20.glBlendFunc( GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA );
	}
}
