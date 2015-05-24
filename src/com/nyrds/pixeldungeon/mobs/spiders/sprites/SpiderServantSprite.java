package com.nyrds.pixeldungeon.mobs.spiders.sprites;

import com.watabou.noosa.TextureFilm;
import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.sprites.MobSprite;

public class SpiderServantSprite extends MobSprite {
	
	static final int framesInRow = 16;
	
	@Override
	public void selectKind(int kind) {
		int shift = framesInRow * kind;
		
		texture( Assets.SPIDER_SERVANT );
		
		TextureFilm frames = new TextureFilm( texture, 16, 16 );
		
		idle = new Animation( 5, true );
		idle.frames(shift, frames, 0, 1, 0, 2 );
		
		run = new Animation( 10, true );
		run.frames(shift, frames, 3, 4, 5, 4 );
		
		attack = new Animation( 12, false );
		attack.frames(shift, frames, 6, 7, 8 );
		
		die = new Animation( 15, false );
		die.frames(shift, frames, 0, 9, 10, 11, 12, 13 );
		
		play( idle );
	}
	
	public SpiderServantSprite() {
		super();
		selectKind(0);
	}
	
	@Override
	public int blood() {
		return 0xFFEAFF80;
	}	
}


