
package com.watabou.pixeldungeon.effects;

import android.opengl.GLES20;

import com.nyrds.pixeldungeon.game.GameLoop;
import com.watabou.pixeldungeon.sprites.CharSprite;

import javax.microedition.khronos.opengles.GL10;

public class TorchHalo extends Halo {

	private CharSprite target;
	
	private float phase = 0;
	
	public TorchHalo( CharSprite sprite ) {
		super( 24, 0xFFDDCC, 0.15f );
		target = sprite;
		am = 0;
	}
	
	@Override
	public void update() {
		super.update();

		final float elapsed = GameLoop.elapsed;
		if (phase < 0) {
			if ((phase += elapsed) >= 0) {
				killAndErase();
			} else {
				setScale( (2 + phase) * radius / RADIUS );
				am = -phase * brightness;
			}
		} else if (phase < 1) {
			if ((phase += elapsed) >= 1) {
				phase = 1;
			}
			setScale( phase * radius / RADIUS );
			am = phase * brightness;
		}

		point( target.getX() + target.width / 2, target.getY() + target.height / 2 );
	}
	
	@Override
	public void draw() {
		GLES20.glBlendFunc( GL10.GL_SRC_ALPHA, GL10.GL_ONE );
		super.draw();
		GLES20.glBlendFunc( GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA );
	}
	
	public void putOut() {
		phase = -1;
	}
}
