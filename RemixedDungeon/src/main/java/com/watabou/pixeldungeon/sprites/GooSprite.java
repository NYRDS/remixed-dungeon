
package com.watabou.pixeldungeon.sprites;

import com.watabou.gltextures.TextureCache;
import com.watabou.noosa.Animation;
import com.watabou.noosa.TextureFilm;
import com.watabou.pixeldungeon.Assets;

public class GooSprite extends MobSprite {
	
	private final Animation pump;
	
	public GooSprite() {
		super();
		
		texture( Assets.GOO );
		
		TextureFilm frames = TextureCache.getFilm( texture, 20, 14 );
		
		idle = new Animation( 10, true );
		idle.frames( frames, 0, 1 );
		
		run = new Animation( 10, true );
		run.frames( frames, 0, 1 );
		
		pump = new Animation( 20, true );
		pump.frames( frames, 0, 1 );
		
		attack = new Animation( 10, false );
		attack.frames( frames, 5, 0, 6 );

		zap = attack.clone();

		die = new Animation( 10, false );
		die.frames( frames, 2, 3, 4 );
		
		play( idle );
	}
	
	public void pumpUp() {
		play( pump );
	}
	
	@Override
	public int blood() {
		return 0xFF000000;
	}
}
