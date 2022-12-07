package com.nyrds.retrodungeon.mobs.spiders.sprites;

import com.watabou.noosa.Animation;
import com.watabou.noosa.TextureFilm;
import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.sprites.MobSprite;

public class SpiderNestSprite extends MobSprite {
	
	public SpiderNestSprite() {
		texture( Assets.SPIDER_NEST );
		
		TextureFilm frames = new TextureFilm( texture, 16, 18 );
		
		idle = new Animation( 5, true );
		idle.frames( frames, 0, 1, 2, 1, 1, 0, 1 );
		
		run = new Animation( 1, true );
		run.frames( frames, 0 );
		
		attack = new Animation( 1, false );
		attack.frames( frames, 0 );
		
		die = new Animation( 5, false );
		die.frames( frames, 2, 3, 4, 5 );
		
		play( idle );
	}
}
