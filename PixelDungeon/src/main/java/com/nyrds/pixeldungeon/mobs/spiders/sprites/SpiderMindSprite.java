package com.nyrds.pixeldungeon.mobs.spiders.sprites;

import com.watabou.noosa.Animation;
import com.watabou.noosa.TextureFilm;
import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.sprites.MobSprite;

public class SpiderMindSprite extends MobSprite {
	
	public SpiderMindSprite() {
		texture( Assets.SPIDER_MIND );
		
		TextureFilm frames = new TextureFilm( texture, 16, 16 );
		
		idle = new Animation( 4, true );
		idle.frames( frames, 0, 1, 3, 1 );
		
		run = new Animation( 12, true );
		run.frames( frames, 2, 3, 4, 2, 3 );
		
		attack = new Animation( 10, false );
		attack.frames( frames, 0, 1, 5, 6, 7 );
		
		die = new Animation( 15, false );
		die.frames( frames, 0, 8, 9, 10, 11);
		
		play( idle );
	}
	
	@Override
	public int blood() {
		return 0xFF80706c;
	}
}
