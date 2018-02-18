package com.watabou.pixeldungeon.effects;

import android.opengl.GLES20;

import com.watabou.pixeldungeon.sprites.CharSprite;

import javax.microedition.khronos.opengles.GL10;

public class RoofMask extends CircleMask {

	private CharSprite target;

	public RoofMask(CharSprite sprite ) {
		super( 24);
		target = sprite;
	}
	
	@Override
	public void update() {
		super.update();

		point( target.x + target.width / 2, target.y + target.height / 2 );
	}
	
	@Override
	public void draw() {
		//GLES20.glBlendFuncSeparate(GLES20.GL_ZERO, GLES20.GL_ONE, GLES20.GL_ONE_MINUS_SRC_ALPHA, GLES20.GL_ZERO);
		GLES20.glBlendFuncSeparate(GLES20.GL_ZERO, GLES20.GL_ONE, GLES20.GL_ONE, GLES20.GL_ZERO);
		//GLES20.glColorMask(false,false,false,true);
		super.draw();
		//GLES20.glColorMask(true,true,true,true);
		GLES20.glBlendFunc( GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA );
		//super.draw();
	}
}
