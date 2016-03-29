package com.watabou.pixeldungeon.sprites;

import com.watabou.noosa.Animation;
import com.watabou.noosa.TextureFilm;
import com.watabou.pixeldungeon.Assets;


public class HedgehogSprite extends MobSprite {
	public HedgehogSprite() {
		super();
		
		texture( Assets.HEDGEHOG );
		
		TextureFilm frames = new TextureFilm( texture, 16, 16 );
		
		idle = new Animation( 5, true );
		idle.frames( frames, 0, 0 );
		
		run = new Animation( 5, true );
		run.frames( frames, 0, 1, 2, 3 );
		
		die = new Animation( 20, false );
		die.frames( frames, 0 );
		
		play( idle );
	}
}
