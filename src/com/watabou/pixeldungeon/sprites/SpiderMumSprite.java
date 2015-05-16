package com.watabou.pixeldungeon.sprites;

import com.watabou.noosa.TextureFilm;
import com.watabou.pixeldungeon.Assets;

public class SpiderMumSprite extends MobSprite {
	
	static final int framesInRow = 16;
	
	protected void selectKind(int kind) {
		int shift = framesInRow * kind;
		
		texture( Assets.SPIDER_MUM );
		
		TextureFilm frames = new TextureFilm( texture, 16, 16 );
		
		idle = new Animation( 5, true );
		idle.frames(shift, frames, 0, 1, 0, 2 );
		
		run = new Animation( 15, true );
		run.frames(shift, frames, 3, 4, 5, 6 );
		
		attack = new Animation( 12, false );
		attack.frames(shift, frames, 7, 8, 9 );
		
		die = new Animation( 12, false );
		die.frames(shift, frames, 10, 11, 12, 13 );
		
		play( idle );
	}
	
	public SpiderMumSprite() {
		super();
	}
	
	@Override
	public int blood() {
		return 0xFFEAFF80;
	}	
}


