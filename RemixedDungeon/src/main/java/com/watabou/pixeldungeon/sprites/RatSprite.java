
package com.watabou.pixeldungeon.sprites;

import com.watabou.gltextures.TextureCache;
import com.watabou.noosa.Animation;
import com.watabou.noosa.TextureFilm;
import com.watabou.pixeldungeon.Assets;

public class RatSprite extends MobSprite {
	
	public RatSprite() {
		super();
		
		texture( Assets.RAT );
		
		TextureFilm frames = TextureCache.getFilm( texture, 16, 15 );
		
		idle = new Animation( 2, true );
		idle.frames( frames, 0, 0, 0, 1 );
		
		run = new Animation( 10, true );
		run.frames( frames, 6, 7, 8, 9, 10 );
		
		attack = new Animation( 15, false );
		attack.frames( frames, 2, 3, 4, 5, 0 );
		
		die = new Animation( 10, false );
		die.frames( frames, 11, 12, 13, 14 );
		
		play( idle );
	}
}
