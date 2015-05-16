package com.watabou.pixeldungeon.sprites;

import com.watabou.noosa.TextureFilm;
import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.effects.particles.ElmoParticle;

public class SpiderMindSprite extends MobSprite {
	
	public SpiderMindSprite() {
		super();
		
		texture( Assets.SPIDER_MIND );
		
		TextureFilm frames = new TextureFilm( texture, 16, 16 );
		
		idle = new Animation( 4, true );
		idle.frames( frames, 0, 1 );
		
		run = new Animation( 12, true );
		run.frames( frames, 2, 3, 4, 5 );
		
		attack = new Animation( 10, false );
		attack.frames( frames, 6, 7, 8 );
		
		die = new Animation( 15, false );
		die.frames( frames, 9, 10, 11, 12, 13 );
		
		play( idle );
	}
	
	@Override
	public int blood() {
		return 0xFF80706c;
	}
}
