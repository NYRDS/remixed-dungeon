package com.watabou.pixeldungeon.sprites;

import javax.microedition.khronos.opengles.GL10;

import android.opengl.GLES20;

import com.watabou.noosa.TextureFilm;
import com.watabou.pixeldungeon.Assets;

public class ShadowSprite extends MobSprite {
	
	public ShadowSprite() {
		super();
		
		texture( Assets.SHADOW );
		
		TextureFilm frames = new TextureFilm( texture, 14, 15 );
		
		idle = new Animation( 5, true );
		idle.frames( frames, 0, 1 );
		
		run = new Animation( 10, true );
		run.frames( frames, 0, 1 );
		
		attack = new Animation( 10, false );
		attack.frames( frames, 0, 2, 3 );
		
		die = new Animation( 8, false );
		die.frames( frames, 0, 4, 5, 6, 7 );
		
		play( idle );
	}
/*	
	@Override
	public void draw() {
		GLES20.glBlendFunc( GL10.GL_SRC_ALPHA, GL10.GL_ONE );
		super.draw();
		GLES20.glBlendFunc( GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA );
	}
*/	
	@Override
	public int blood() {
		return 0x88000000;
	}
}
